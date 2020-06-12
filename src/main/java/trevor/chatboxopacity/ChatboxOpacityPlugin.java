package trevor.chatboxopacity;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Varbits;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import static net.runelite.api.widgets.WidgetID.CHATBOX_GROUP_ID;
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
	private static final int CHATBOX_GROUP = CHATBOX_GROUP_ID;
	private static final int CHATBOX_BUTTON_BACKGROUND = 3;
	private static final int ORIGINAL_BUTTON_BACKGROUND_TYPE = 5;
	private static final int NEW_BUTTON_BACKGROUND_TYPE = 3;

	@Inject
	private Client client;

	@Inject
	private ChatboxOpacityConfig config;

	@Inject
	private ClientThread clientThread;

	private int chatboxButtonBackgroundSprite = -1;
	private int previousChatboxOpacity = -1;

	@Provides
	ChatboxOpacityConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ChatboxOpacityConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invoke(() -> writeChatboxOpacity());
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invoke(() ->
		{
			if (chatboxButtonBackgroundSprite != -1)
			{
				Widget widget = client.getWidget(CHATBOX_GROUP, CHATBOX_BUTTON_BACKGROUND);
				if (widget != null)
				{
					widget.setSpriteId(chatboxButtonBackgroundSprite);
					widget.setFilled(false);
					widget.setType(ORIGINAL_BUTTON_BACKGROUND_TYPE);
					chatboxButtonBackgroundSprite = -1;
				}
			}
			client.runScript(BUILD_CHATBOX_SCRIPT);

		});
	}

	@Subscribe
	private void onScriptPostFired(ScriptPostFired ev)
	{
		if (ev.getScriptId() != BUILD_CHATBOX_SCRIPT)
		{
			return;
		}

		writeChatboxOpacity();
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("chatboxopacity"))
		{
			clientThread.invokeLater(() -> writeChatboxOpacity());
		}
	}

	private void writeChatboxOpacity()
	{
		if (client.getGameState() != GameState.LOGGED_IN
			|| !client.isResized()
			|| client.getVar(Varbits.TRANSPARENT_CHATBOX) == 0)
		{
			return;
		}

		Widget widget = client.getWidget(WidgetInfo.CHATBOX_MESSAGES);

		if (widget == null || widget.isHidden())
		{
			return;
		}

		widget = client.getWidget(WidgetInfo.CHATBOX_TRANSPARENT_BACKGROUND);

		Widget[] children = widget.getChildren();

		if (children.length == 20)
		{
			if (config.chatboxOpacity() != -1)
			{
				for (Widget child : children)
				{
					child.setOpacity(config.chatboxOpacity());
					previousChatboxOpacity = config.chatboxOpacity();
				}
			}
			else if (previousChatboxOpacity != -1)
			{
				previousChatboxOpacity = -1;
				client.runScript(BUILD_CHATBOX_SCRIPT);
			}
		}

		if (config.buttonOpacity() != -1)
		{
			widget = client.getWidget(CHATBOX_GROUP, CHATBOX_BUTTON_BACKGROUND);
			if (widget != null)
			{
				if (chatboxButtonBackgroundSprite == -1)
				{
					chatboxButtonBackgroundSprite = widget.getSpriteId();
					widget.setSpriteId(-1);
					widget.setType(NEW_BUTTON_BACKGROUND_TYPE);
					widget.setFilled(true);
				}
				widget.setOpacity(config.buttonOpacity());
			}
		}
		else if (chatboxButtonBackgroundSprite != -1)
		{
			widget = client.getWidget(CHATBOX_GROUP, CHATBOX_BUTTON_BACKGROUND);
			if (widget != null)
			{
				widget.setSpriteId(chatboxButtonBackgroundSprite);
				widget.setFilled(false);
				widget.setType(ORIGINAL_BUTTON_BACKGROUND_TYPE);
				chatboxButtonBackgroundSprite = -1;
				client.runScript(BUILD_CHATBOX_SCRIPT);
			}
		}
	}
}
