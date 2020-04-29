package gg.trevor.texttospeech;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import net.runelite.api.ChatMessageType;
import static net.runelite.api.ChatMessageType.BROADCAST;
import static net.runelite.api.ChatMessageType.FRIENDSCHAT;
import static net.runelite.api.ChatMessageType.LOGINLOGOUTNOTIFICATION;
import static net.runelite.api.ChatMessageType.MODCHAT;
import static net.runelite.api.ChatMessageType.MODPRIVATECHAT;
import static net.runelite.api.ChatMessageType.PRIVATECHAT;
import static net.runelite.api.ChatMessageType.PRIVATECHATOUT;
import static net.runelite.api.ChatMessageType.PUBLICCHAT;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Text-to-Speech"
)
public class TextToSpeechPlugin extends Plugin
{
	private static final Set<ChatMessageType> publicChatTypes = ImmutableSet.of(MODCHAT, PUBLICCHAT);
	private static final Set<ChatMessageType> privateChatTypes = ImmutableSet.of(PRIVATECHAT, PRIVATECHATOUT, MODPRIVATECHAT);

	@Inject
	private Client client;

	@Inject
	private TextToSpeechConfig config;

	@Inject
	private ScheduledExecutorService executorService;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private ClientThread clientThread;

	private MaryInterface marytts;
	private Clip clip;

	@Override
	protected void startUp() throws Exception
	{
		try
		{
			marytts = new LocalMaryInterface();
		}
		catch (MaryConfigurationException e)
		{
			log.error("Error starting Text-to-Speech plugin", e);

			SwingUtilities.invokeLater(() ->
			{
				try
				{
					pluginManager.setPluginEnabled(this, false);
					pluginManager.stopPlugin(this);
				}
				catch (PluginInstantiationException ex)
				{
					log.error("error stopping plugin", ex);
				}
			});
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		MaryInterface marytts = null;
		if (clip != null && clip.isOpen())
		{
			clip.close();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		String message = Text.removeTags(event.getMessage());
		ChatMessageType type = event.getType();

		if (publicChatTypes.contains(type))
		{
			playSound(message, config.getPublicChatVoice());
		}
		else if (privateChatTypes.contains(type))
		{
			playSound(message, config.getPrivateChatVoice());
		}
		else if (type == BROADCAST)
		{
			playSound(message, config.getBroadcastVoice());
		}
		else if (type == LOGINLOGOUTNOTIFICATION)
		{
			playSound(message, config.getLoginVoice());
		}
		else if (type == FRIENDSCHAT)
		{
			playSound(message, config.getClanChatVoice());
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == WidgetID.DIALOG_PLAYER_GROUP_ID)
		{
			clientThread.invokeLater(() -> processPlayerDialog());
		}
		else if (event.getGroupId() == WidgetID.DIALOG_NPC_GROUP_ID)
		{
			clientThread.invokeLater(() -> processNpcDialog());
		}
	}

	private void processPlayerDialog()
	{
		Widget widget = client.getWidget(WidgetID.DIALOG_PLAYER_GROUP_ID, 4);

		if (widget == null)
		{
			return;
		}

		String message = widget.getText().replaceAll("<br>", " ");
		playSound(message, config.getPlayerVoice(), true);
	}

	private void processNpcDialog()
	{
		Widget widget = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);

		if (widget == null)
		{
			return;
		}

		String message = widget.getText().replaceAll("<br>", " ");
		playSound(message, config.getNPCVoice(), true);
	}

	private void playSound(String text, Voices voice)
	{
		playSound(text, voice, false);
	}

	private void playSound(String text, Voices voice, boolean force)
	{
		String voicename = voice.getVoicename();

		if (voicename == null)
		{
			return;
		}

		executorService.submit(() -> textToSpeech(text, voicename, force));
	}

	private synchronized void textToSpeech(String text, String voicename, boolean force)
	{
		if (clip == null || !clip.isActive() || force)
		{
			if (clip != null)
			{
				clip.close();
			}

			try
			{
				clip = AudioSystem.getClip();
				marytts.setVoice(voicename);
				clip.open(marytts.generateAudio(text));
			}
			catch (IOException | LineUnavailableException | SynthesisException e)
			{
				log.warn("Unable to play Text-to-Speech", e);
				return;
			}

			clip.start();
		}
	}

	@Provides
	TextToSpeechConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TextToSpeechConfig.class);
	}
}
