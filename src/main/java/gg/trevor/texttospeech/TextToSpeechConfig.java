package gg.trevor.texttospeech;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("texttospeech")
public interface TextToSpeechConfig extends Config
{
	@ConfigItem(
		keyName = "npcDialog",
		name = "NPC Dialog",
		description = "What voice the NPC text box should have.",
		position = 1
	)
	default Voices getNPCVoice()
	{
		return Voices.FEMALE_AMERICAN;
	}

	@ConfigItem(
		keyName = "playerDialog",
		name = "Player Dialog",
		description = "What voice the player text box should have.",
		position = 2
	)
	default Voices getPlayerVoice()
	{
		return Voices.MALE_AMERICAN_2;
	}

	@ConfigItem(
		keyName = "publicChatVoice",
		name = "Public Chat",
		description = "What voice the public chat text has.",
		position = 3
	)
	default Voices getPublicChatVoice()
	{
		return Voices.OFF;
	}

	@ConfigItem(
		keyName = "privateChatVoice",
		name = "Private Chat",
		description = "What voice the private chat text has.",
		position = 4
	)
	default Voices getPrivateChatVoice()
	{
		return Voices.OFF;
	}

	@ConfigItem(
		keyName = "clanchatVoice",
		name = "Clan Chat",
		description = "What voice the clan chat messages should have.",
		position = 5
	)
	default Voices getClanChatVoice()
	{
		return Voices.OFF;
	}

	@ConfigItem(
		keyName = "loginVoice",
		name = "Log in/out",
		description = "What voice the log in/out messages should have.",
		position = 6
	)
	default Voices getLoginVoice()
	{
		return Voices.OFF;
	}

	@ConfigItem(
		keyName = "broadcastVoice",
		name = "Broadcasts",
		description = "What voice the broadcasts should have.",
		position = 7
	)
	default Voices getBroadcastVoice()
	{
		return Voices.MALE_AMERICAN_2;
	}
}
