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
		name = "Opacity",
		description = "The opacity of your chatbox from 0 to 255 (0 being black and 255 being transparent)"
	)
	@Range(
		max = 255
	)
	default int opacity()
	{
		return 150;
	}
}
