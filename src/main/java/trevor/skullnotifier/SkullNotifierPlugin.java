package trevor.skullnotifier;

import com.google.inject.Provides;
import java.util.EnumSet;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;
import net.runelite.api.WorldType;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Skull Notifier"
)
public class SkullNotifierPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private SkullNotifierConfig config;

	@Inject
	private Notifier notifier;

	private SkullIcon lastTickSkull;
	private boolean isFirstTick;

	@Override
	protected void startUp() throws Exception
	{
		isFirstTick = true;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState state = gameStateChanged.getGameState();
		switch (state)
		{
			case HOPPING:
			case LOGIN_SCREEN:
				isFirstTick = true;
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		final Player local = client.getLocalPlayer();
		SkullIcon currentTickSkull = local.getSkullIcon();
		EnumSet worldTypes = client.getWorldType();
		if (!worldTypes.contains(WorldType.DEADMAN))
		{
			if (!isFirstTick)
			{
				if (config.showSkullNotification() && lastTickSkull == null && currentTickSkull == SkullIcon.SKULL)
				{
					notifier.notify("[" + local.getName() + "] is now skulled!");
				}
				else if (config.showUnskullNotification() && lastTickSkull == SkullIcon.SKULL && currentTickSkull == null)
				{
					notifier.notify("[" + local.getName() + "] is now unskulled!");
				}
			}
			else
			{
				isFirstTick = false;
			}

			lastTickSkull = currentTickSkull;
		}
	}

	@Provides
	SkullNotifierConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkullNotifierConfig.class);
	}
}
