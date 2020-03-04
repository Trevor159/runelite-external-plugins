package com.trevor.traynotifications;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RlTrayNotificationsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RlTrayNotificationsPlugin.class);
		RuneLite.main(args);
	}
}