package gg.trevor.tobdamage;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TobDamageCounterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TobDamageCounterPlugin.class);
		RuneLite.main(args);
	}
}