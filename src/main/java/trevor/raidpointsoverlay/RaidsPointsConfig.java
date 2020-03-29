package trevor.raidpointsoverlay;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("raidpointsoverlay")
public interface RaidsPointsConfig extends Config
{
	@ConfigItem(
		keyName = "teamSize",
		name = "Display team size",
		description = "Display team size on the overlay if team size is greater than 1."
	)
	default boolean showTeamSize()
	{
		return true;
	}

	@ConfigItem(
		keyName = "raidsTimer",
		name = "Display elapsed raid time",
		description = "Display elapsed raid time"
	)
	default boolean raidsTimer()
	{
		return true;
	}

	@ConfigItem(
		keyName = "raidsPointsPercent",
		name = "Display personal points percent",
		description = "Displays the personal points percents of the total points"
	)
	default boolean raidsPointsPercent()
	{
		return true;
	}

	@ConfigItem(
		keyName = "raidsUniqueChance",
		name = "Display the chance of an unique",
		description = "Displays the chance that a single unique could be in raid loot"
	)
	default UniqueConfigOptions raidsUniqueChance()
	{
		return UniqueConfigOptions.BOTH;
	}
}
