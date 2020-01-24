package trevor.tobhealthbars;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tobhealthbars")
public interface TobHealthBarsConfig extends Config
{
	@ConfigItem(
		keyName = "showLocalName",
		name = "Display own name",
		description = "Show local player name instead of 'me'"
	)
	default boolean showLocalName()
	{
		return true;
	}

//	@ConfigItem(
//		keyName = "assumeMaxHealth",
//		name = "Assume 99 hp",
//		description = "Show hp instead of percents assuming your teamates are 99 hp."
//	)
//	default boolean assumeMaxHealth()
//	{
//		return false;
//	}
}
