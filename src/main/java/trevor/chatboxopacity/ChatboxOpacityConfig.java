package trevor.chatboxopacity;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("chatboxopacity")
public interface ChatboxOpacityConfig extends Config
{
	@ConfigItem(
		keyName = "opacity",
		name = "Chatbox Opacity",
		description = "The opacity of your chatbox from 0 to 255 (0 being black and 255 being transparent)",
		position = 1
	)
	@Range(
		min = -1,
		max = 255
	)
	default int chatboxOpacity()
	{
		return 150;
	}

	@ConfigItem(
		keyName = "buttonOpacity",
		name = "Button Opacity",
		description = "The opacity of your chatbox buttons from 0 to 255 (0 being black and 255 being transparent)",
		position = 2
	)
	@Range(
		min = -1,
		max = 255
	)
	default int buttonOpacity()
	{
		return -1;
	}
}
