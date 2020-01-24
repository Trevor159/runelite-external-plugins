package trevor.raidpointsoverlay;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetHiddenChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@PluginDescriptor(
	name = "Raid Points Overlay"
)
public class RaidPointsOverlayPlugin extends Plugin
{
	private static final Pattern LEVEL_COMPLETE_REGEX = Pattern.compile("(.+) level complete! Duration: ([0-9:]+)");
	private static final Pattern RAID_COMPLETE_REGEX = Pattern.compile("Congratulations - your raid is complete! Duration: ([0-9:]+)");

	private static final int RAID_TIMER_VARBIT = 6386;
	private static final int RAID_STATE_VARBIT = 5425;
	private static final int RAID_BANK_REGION = 4919;

	@Inject
	private Client client;

	@Inject
	private RaidsPointsOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClientThread clientThread;

	@Getter
	private boolean inRaidChambers;

	private int raidState;
	private int timerVarb;
	private int upperTime = -1;
	private int middleTime = -1;
	private int lowerTime = -1;
	private int raidTime = -1;

	@Getter
	private String tooltip;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);

		clientThread.invoke(() -> setHidden(true));
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);

		clientThread.invoke(() -> setHidden(false));
		reset();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		inRaidChambers = client.getVar(Varbits.IN_RAID) == 1;
		raidState = client.getVarbitValue(RAID_STATE_VARBIT);
		timerVarb = client.getVarbitValue(RAID_TIMER_VARBIT);
	}

	@Subscribe
	public void onWidgetHiddenChanged(WidgetHiddenChanged event)
	{
		if (!inRaidChambers || event.isHidden())
		{
			return;
		}

		Widget widget = event.getWidget();

		if (widget == client.getWidget(WidgetInfo.RAIDS_POINTS_INFOBOX))
		{
			widget.setHidden(true);
		}
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (timerVarb > 0 && raidState < 5)
		{
			//mimic the script when the widget is hidden
			client.runScript(2289);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (inRaidChambers && event.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION)
		{
			String message = Text.removeTags(event.getMessage());
			Matcher matcher;

			matcher = LEVEL_COMPLETE_REGEX.matcher(message);
			if (matcher.find())
			{
				String floor = matcher.group(1);
				int time = timeToSeconds(matcher.group(2));
				if (floor.equals("Upper"))
				{
					upperTime = time;
				}
				else if (floor.equals("Middle"))
				{
					middleTime = time;
				}
				else if (floor.equals("Lower"))
				{
					lowerTime = time;
				}
				updateTooltip();
			}

			matcher = RAID_COMPLETE_REGEX.matcher(message);
			if (matcher.find())
			{
				raidTime = timeToSeconds(matcher.group(1));
				updateTooltip();
			}
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		// lazy way to reset
		if (event.getGameState() == GameState.LOGGED_IN
			&& client.getLocalPlayer() != null
			&& client.getLocalPlayer().getWorldLocation().getRegionID() == RAID_BANK_REGION)
		{
			reset();
		}
	}

	private void setHidden(boolean shouldHide)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		inRaidChambers = client.getVar(Varbits.IN_RAID) == 1;

		if (!inRaidChambers)
		{
			return;
		}

		final Widget widget = client.getWidget(WidgetInfo.RAIDS_POINTS_INFOBOX);
		if (widget != null)
		{
			widget.setHidden(shouldHide);
		}
	}

	public void reset()
	{
		upperTime = -1;
		middleTime = -1;
		lowerTime = -1;
		raidTime = -1;
		tooltip = null;
	}

	private int timeToSeconds(String s)
	{
		int seconds = -1;
		String[] split = s.split(":");
		if (split.length == 2)
		{
			seconds = Integer.parseInt(split[0]) * 60 + Integer.parseInt(split[1]);
		}
		if (split.length == 3)
		{
			seconds = Integer.parseInt(split[0]) * 3600 + Integer.parseInt(split[1]) * 60 + Integer.parseInt(split[2]);
		}
		return seconds;
	}

	private String secondsToTime(int seconds)
	{
		StringBuilder builder = new StringBuilder();
		if (seconds >= 3600)
		{
			builder.append((int)Math.floor(seconds / 3600) + ":");
		}
		seconds %= 3600;
		if (builder.toString().equals(""))
		{
			builder.append((int)Math.floor(seconds / 60));
		}
		else
		{
			builder.append(StringUtils.leftPad(String.valueOf((int)Math.floor(seconds / 60)), 2, '0'));
		}
		builder.append(":");
		seconds %= 60;
		builder.append(StringUtils.leftPad(String.valueOf(seconds), 2, '0'));
		return builder.toString();
	}

	private void updateTooltip()
	{
		StringBuilder builder = new StringBuilder();
		if (upperTime == -1)
		{
			tooltip = null;
			return;
		}
		builder.append("Upper level: " + secondsToTime(upperTime));
		if (middleTime == -1)
		{
			if (lowerTime == -1)
			{
				tooltip = builder.toString();
				return;
			}
			else
			{
				builder.append("</br>Lower level: " + secondsToTime(lowerTime - upperTime));
			}
		}
		else
		{
			builder.append("</br>Middle level: " + secondsToTime(middleTime - upperTime));
			if (lowerTime == -1)
			{
				tooltip = builder.toString();
				return;
			}
			else
			{
				builder.append("</br>Lower level: " + secondsToTime(lowerTime - middleTime));
			}
		}
		if (raidTime == -1)
		{
			tooltip = builder.toString();
			return;
		}
		builder.append("</br>Olm: " + secondsToTime(raidTime - lowerTime));
		tooltip = builder.toString();
	}

	String getTime()
	{
		int seconds = (int) Math.floor(client.getVarbitValue(RAID_TIMER_VARBIT) * .6);
		return secondsToTime(seconds);
	}
}
