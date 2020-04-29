package gg.trevor.texttospeech;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TextToSpeechPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TextToSpeechPlugin.class);
		RuneLite.main(args);
	}
}