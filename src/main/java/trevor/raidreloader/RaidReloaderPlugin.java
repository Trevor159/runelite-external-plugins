package trevor.raidreloader;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "RaidReloader",
	description = "Reloads the raid instance"
)
public class RaidReloaderPlugin extends Plugin
{
	private static final int RAIDS_LOBBY_REGION = 4919;

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	private RaidReloaderPanel panel;
	private NavigationButton navButton;
	private boolean buttonAttatched;


	@Override
	protected void startUp() throws Exception
	{
		panel = injector.getInstance(RaidReloaderPanel.class);
		panel.init();

		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "/util/panel_icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Raid Reloader")
			.icon(icon)
			.priority(6)
			.panel(panel)
			.build();
	}

	@Override
	protected void shutDown() throws Exception
	{
		buttonAttatched = false;
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		boolean isInRaid = client.getVar(Varbits.IN_RAID) == 1;
		boolean inRaidLobby = (client.getLocalPlayer().getWorldLocation().getRegionID() == RAIDS_LOBBY_REGION);
		boolean inParty = client.getVar(VarPlayer.IN_RAID_PARTY) != -1;
		boolean shouldShow = isInRaid | inRaidLobby | inParty;
		if (shouldShow != buttonAttatched)
		{
			SwingUtilities.invokeLater(() ->
			{
				if (shouldShow)
				{
					clientToolbar.addNavigation(navButton);
				}
				else
				{
					clientToolbar.removeNavigation(navButton);
				}
			});
			buttonAttatched = shouldShow;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			SwingUtilities.invokeLater(() ->
			{
				clientToolbar.removeNavigation(navButton);
				buttonAttatched = false;
			});
		}
	}

	@Provides
	RaidReloaderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RaidReloaderConfig.class);
	}
}
