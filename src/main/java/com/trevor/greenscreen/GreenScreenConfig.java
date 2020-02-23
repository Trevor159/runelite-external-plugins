package com.trevor.greenscreen;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("greenscreen")
public interface GreenScreenConfig extends Config
{
	@ConfigItem(
		keyName = "color",
		name = "Color",
		description = "The color of the greenscreen"
	)
	default Color greenscreenColor()
	{
		return new Color(41, 244, 24);
	}
}
