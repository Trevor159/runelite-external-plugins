package trevor.chatboxopacity;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Chatbox Opacity",
	description = "Change the opacity on your transparent chatboxes"
)
public class ChatboxOpacityPlugin extends Plugin
{
	private static final int BUILD_CHATBOX_SCRIPT = 923;

	@Inject
	private Client client;

	@Inject
	private ChatboxOpacityConfig config;

	@Inject
	private ClientThread clientThread;

	@Provides
	ChatboxOpacityConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ChatboxOpacityConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invoke(() -> writeChatboxOpacity(config.opacity()));
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invoke(() -> client.runScript(923));
	}

	// I was going to only set it when the wi``dget is loaded but it likes to reset itself a lot
	// I didn't feel like fixing it in scripts so here is the lazy way
	@Subscribe
	private void onGameTick(GameTick event)
	{
		writeChatboxOpacity(config.opacity());
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("chatboxgradient"))
		{
			clientThread.invoke(() -> writeChatboxOpacity(config.opacity()));
		}
	}

	private void writeChatboxOpacity(int opacity)
	{
		if (client.getGameState() != GameState.LOGGED_IN
			|| client.getVar(Varbits.TRANSPARENT_CHATBOX) == 0)
		{
			return;
		}

		Widget widget = client.getWidget(WidgetInfo.CHATBOX_TRANSPARENT_BACKGROUND);
		Widget[] children = widget.getChildren();

		for (Widget child : children)
		{
			child.setOpacity(opacity);
		}
	}
}
