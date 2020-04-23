package gg.trevor.tobdamage;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tobdamagecounter")
public interface TobDamageCounterConfig extends Config
{
	@ConfigItem(
		keyName = "damageSummary",
		name = "Print Raid Summary",
		description = "Print the damage of all the rooms when the raid ends."
	)
	default boolean showDamageSummary()
	{
		return true;
	}
}
