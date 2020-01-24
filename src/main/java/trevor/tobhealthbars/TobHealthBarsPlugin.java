package trevor.tobhealthbars;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Varbits;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetHiddenChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Tob Health Bars"
)
public class TobHealthBarsPlugin extends Plugin
{
	private static final int TOB_ORB_GROUP_ID = 28;
	private static final int TOB_ORB_CHILD_ID = 10;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TobHealthBarsOverlay overlay;

	@Inject
	private ClientThread clientThread;

	@Data
	class TobPlayer
	{
		/**
		 * varb to render the orb
		 * 0 means that the orb should not be rendered
		 * 1-27 is the % the heath is at, 27 is 100% health 1 is 0% health
		 * 30 means the player is respawning outside tob?
		 */
		final private int healthVarb;

		/**
		 * the names of the people at the orb
		 */
		final private int nameVarc;
	}

	@Getter
	private TobPlayer[] players = new TobPlayer[]{
		new TobPlayer(6442, 330),
		new TobPlayer(6443, 331),
		new TobPlayer(6444, 332),
		new TobPlayer(6445, 333),
		new TobPlayer(6446, 334)
	};

	@Getter
	private boolean inTob;

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
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		inTob = client.getVar(Varbits.THEATRE_OF_BLOOD) > 1;
	}

	@Subscribe
	public void onWidgetHiddenChanged(WidgetHiddenChanged event)
	{
		if (!inTob || event.isHidden())
		{
			return;
		}

		Widget widget = event.getWidget();

		if (widget == client.getWidget(TOB_ORB_GROUP_ID, TOB_ORB_CHILD_ID))
		{
			widget.setHidden(true);
		}
	}

	private void setHidden(boolean shouldHide)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		inTob = client.getVar(Varbits.THEATRE_OF_BLOOD) > 1;

		if (!inTob)
		{
			return;
		}

		final Widget widget = client.getWidget(TOB_ORB_GROUP_ID, TOB_ORB_CHILD_ID);
		if (widget != null)
		{
			widget.setHidden(shouldHide);
		}
	}


	@Provides
	TobHealthBarsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TobHealthBarsConfig.class);
	}
}
