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
}
