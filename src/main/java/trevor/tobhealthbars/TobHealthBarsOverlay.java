package trevor.tobhealthbars;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.ProgressBarComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class TobHealthBarsOverlay extends Overlay
{
	// value of 1 to 5 based on which orb belongs to the local player
	private static final int LOCAL_TOB_ORB_VARB = 6441;

	private static final Color HP_GREEN = new Color(0, 146, 54, 230);
	private static final Color HP_RED = new Color(102, 15, 16, 230);

	private Client client;
	private TobHealthBarsPlugin plugin;
	private TobHealthBarsConfig config;

	private final PanelComponent panel = new PanelComponent();

	@Inject
	private TobHealthBarsOverlay(Client client, TobHealthBarsPlugin plugin, TobHealthBarsConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isInTob())
		{
			return null;
		}

		panel.getChildren().clear();

		int localPlayerIndex = client.getVarbitValue(LOCAL_TOB_ORB_VARB) -1;

		for (int i = 0; i < 5; i++)
		{
			TobHealthBarsPlugin.TobPlayer player = plugin.getPlayers()[i];
			int healthVarb = client.getVarbitValue(player.getHealthVarb());
			String playerName = client.getVarcStrValue(player.getNameVarc());

			if (healthVarb == 0)
			{
				break;
			}
			else if (healthVarb == 30) // how the player as 0 hp when thy are dead
			{
				healthVarb = 1;
			}

//			if (i > 0)
//			{
//				panel.getChildren().add(LineComponent.builder().build());
//			}

			if (localPlayerIndex == i && !config.showLocalName())
			{
				playerName = "Me";
			}

			panel.getChildren().add(TitleComponent.builder()
				.text(playerName)
				.build());

			final ProgressBarComponent progressBarComponent = new ProgressBarComponent();
			progressBarComponent.setBackgroundColor(HP_RED);
			progressBarComponent.setForegroundColor(HP_GREEN);

			float floatRatio = (float) (healthVarb - 1) / 26f;
			progressBarComponent.setValue(floatRatio * 100d);

			panel.getChildren().add(progressBarComponent);
		}

		return panel.render(graphics);
	}
}
