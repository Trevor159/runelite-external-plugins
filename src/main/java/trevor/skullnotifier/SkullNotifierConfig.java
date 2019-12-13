
package trevor.skullnotifier;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("skullnotifier")
public interface SkullNotifierConfig extends Config
{
	@ConfigItem(
		position = 1,
		keyName = "skullNotification",
		name = "Skull Notification",
		description = "Receive a notification when you skull."
	)
	default boolean showSkullNotification()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "unskullNotification",
		name = "Unskull Notification",
		description = "Receive a notification when you unskull."
	)
	default boolean showUnskullNotification()
	{
		return false;
	}
}
