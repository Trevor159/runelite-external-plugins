package com.trevor.traynotifications;

import java.awt.TrayIcon;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NotificationFired;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;

@Slf4j
@PluginDescriptor(
	name = "RL Tray Notifications"
)
public class RlTrayNotificationsPlugin extends Plugin
{
//	@Inject
//	private RlTrayNotificationsConfig config;

	@Inject
	private ClientUI clientUI;

	@Inject
	private RuneLiteConfig runeLiteConfig;

	@Subscribe
	public void onNotificationFired(NotificationFired event)
	{
		if (!runeLiteConfig.sendNotificationsWhenFocused() && clientUI.isFocused())
		{
			return;
		}

		SwingUtilities.invokeLater(() -> sendCustomNotification(RuneLiteProperties.getTitle(), event.getMessage(), event.getType()));
	}

	private void sendCustomNotification(
		final String title,
		final String message,
		final TrayIcon.MessageType type)
	{
		CustomNotification.sendCustomNotification(title, message, type, clientUI.getGraphicsConfiguration().getBounds());
	}

//	@Provides
//	RlTrayNotificationsConfig provideConfig(ConfigManager configManager)
//	{
//		return configManager.getConfig(RlTrayNotificationsConfig.class);
//	}
}
