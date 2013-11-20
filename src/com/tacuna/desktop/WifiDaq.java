package com.tacuna.desktop;

import javax.swing.SwingUtilities;

public class WifiDaq {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				new MainFrame("WiFi DAQ");				
			}
			
		});
	}

}
