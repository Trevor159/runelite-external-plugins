/*
 * Copyright (c) 2018 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.trevor.traynotifications;

import static com.trevor.traynotifications.RlTrayNotificationsPlugin.CornerConfig.BOTTOM_LEFT;
import static com.trevor.traynotifications.RlTrayNotificationsPlugin.CornerConfig.TOP_LEFT;
import static com.trevor.traynotifications.RlTrayNotificationsPlugin.CornerConfig.TOP_RIGHT;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.util.ImageUtil;
//import org.pushingpixels.substance.internal.SubstanceSynapse;

class CustomNotification
{
	private static final int WIDTH = 360;
	private static final int HEIGHT = 100;
	private static final int PADDING = 50;

	private static final double ANIMATION_LENGTH_SECONDS = 2.f;
	private static final double ANIMATION_MAX_LUM = .5f;

	private static final BufferedImage RUNELITE_ICON = ImageUtil.getResourceStreamFromClass(RlTrayNotificationsPlugin.class, "/util/runelite100.png");

	static void sendCustomNotification(
			final String title,
			final String message,
			final Rectangle display,
			final RlTrayNotificationsPlugin.CornerConfig cornerConfig,
			final int expireTime
			)
	{
		JFrame window = new JFrame();
		window.setIconImage(ClientUI.ICON_128);

		// No chrome/decorations
		window.setUndecorated(true);
		// Do not let this window take focus from another window.
		window.setFocusableWindowState(false);
		window.setAlwaysOnTop(true);

//		fullySaturateColors(window);

		int x;
		int y;

		if (cornerConfig == BOTTOM_LEFT || cornerConfig == TOP_LEFT)
		{
			x = PADDING;
		}
		else
		{
			x = display.x + display.width - WIDTH - PADDING;
		}

		if (cornerConfig == TOP_RIGHT || cornerConfig == TOP_LEFT)
		{
			y = PADDING;
		}
		else
		{
			y = display.y + display.height - HEIGHT - PADDING;
		}

		// Position in lower right corner of the display containing parent
		window.setBounds(
			x,
			y,
			WIDTH,
			HEIGHT
		);

		// Ramp the lum up to 50% then back down again to grab the user's attention
		float[] hsbs = new float[3];
		Color stock = window.getContentPane().getBackground();
		Color.RGBtoHSB(stock.getRed(), stock.getGreen(), stock.getBlue(), hsbs);
		final long start = System.currentTimeMillis();
		Timer anim = new Timer(20, e ->
		{
			double seconds = (System.currentTimeMillis() - start) / 1000.d;
			if (seconds > ANIMATION_LENGTH_SECONDS)
			{
				((Timer) e.getSource()).stop();
				window.getContentPane().setBackground(stock);
				return;
			}
			double lumLevel = Math.sin(seconds / ANIMATION_LENGTH_SECONDS * Math.PI);
			float lum = (float) (lumLevel * (ANIMATION_MAX_LUM - hsbs[2]) + hsbs[2]);
			window.getContentPane().setBackground(new Color(Color.HSBtoRGB(hsbs[0], hsbs[1], lum)));
		});
		anim.setRepeats(true);
		anim.start();

		// Close the window after a delay
		Timer closeTimer = new Timer(expireTime, e ->
		{
			anim.stop();
			window.setVisible(false);
			window.dispose();
		});
		closeTimer.start();

		// Use a mousePressed event rather than clicked, so it goes away on mouseDown, not mouseUp
		// I want this window to go away as fast as possible, because it tends to cover the lower right
		// part of the inventory panel. Nothing is worse than getting a low hp notification that covers up
		// your last food.
		window.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				anim.stop();
				closeTimer.stop();
				window.setVisible(false);
				window.dispose();
			}
		});

		JLabel iconLabel = new JLabel(new ImageIcon(RUNELITE_ICON));
		JLabel titleLabel = new JLabel(title);
		// Not using the runescape fonts, because they need to be scaled, and look awful
		titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		JLabel messageLabel = new JLabel("<html>" + message + "</html>");
		messageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

		GroupLayout layout = new GroupLayout(window.getContentPane());
		window.setLayout(layout);

		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(iconLabel)
			.addGroup(layout.createParallelGroup()
				.addComponent(titleLabel)
				.addComponent(messageLabel)
			)
		);
		layout.setVerticalGroup(layout.createParallelGroup()
			.addComponent(iconLabel)
			.addGroup(layout.createSequentialGroup()
				.addGap(8)
				.addComponent(titleLabel)
				.addGap(4)
				.addComponent(messageLabel)
			)
		);

		window.revalidate();
		window.setVisible(true);
	}

//	/**
//	 * Sets the substance Colorization factor to 1.0 so colors don't appear desaturated.
//	 */
//	public static void fullySaturateColors(JFrame window)
//	{
//		((JPanel) window.getContentPane()).putClientProperty(SubstanceSynapse.COLORIZATION_FACTOR, 1.0);
//	}
}