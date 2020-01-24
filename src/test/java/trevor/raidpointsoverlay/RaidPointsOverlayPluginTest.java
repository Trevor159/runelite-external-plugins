package trevor.raidpointsoverlay;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RaidPointsOverlayPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RaidPointsOverlayPlugin.class);
		RuneLite.main(args);
	}
}