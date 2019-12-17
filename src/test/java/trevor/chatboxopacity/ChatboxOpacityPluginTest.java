package trevor.chatboxopacity;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ChatboxOpacityPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ChatboxOpacityPlugin.class);
		RuneLite.main(args);
	}
}