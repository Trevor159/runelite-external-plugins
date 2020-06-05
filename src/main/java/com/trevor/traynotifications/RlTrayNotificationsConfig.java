package com.trevor.traynotifications;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("rltraynotifications")
public interface RlTrayNotificationsConfig extends Config
{
	@ConfigItem(
		keyName = "monitor",
		name = "Monitor",
		description = "Which monitor do you want to display the notification on"
	)
	default RlTrayNotificationsPlugin.MonitorConfig monitor()
	{
		return RlTrayNotificationsPlugin.MonitorConfig.CURRENT_MONITOR;
	}

	@ConfigItem(
			keyName = "corner",
			name = "Corner",
			description = "Which corner of your monitor do you want to display the notification on"
	)
	default RlTrayNotificationsPlugin.CornerConfig corner()
	{
		return RlTrayNotificationsPlugin.CornerConfig.BOTTOM_RIGHT;
	}
}
