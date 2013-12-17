package com.tacuna.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import com.tacuna.common.devices.ChannelInterface;
import com.tacuna.common.devices.scpi.Command;
import javax.swing.JLabel;

public class ChannelPanel extends JPanel {
	private static Logger log = Logger.getLogger(ChannelPanel.class.getName());
	ChannelInterface channel = null;
	JButton btnMeasure = null;
	JLabel label = null;
	/**
	 * Create the panel.
	 */
	public ChannelPanel(ChannelInterface channel) {
		this.channel = channel;
		btnMeasure = new JButton("Measure");
		btnMeasure.setHorizontalAlignment(SwingConstants.TRAILING);
		btnMeasure.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){								
				startMeasurement();
			}
		});
		
		label = new JLabel(channel.getName());

		add(label);		
		add(btnMeasure);
		
		setVisible(true);
	}
	private void startMeasurement(){
		SwingWorker<Void,Void> worker = new SwingWorker<Void, Void>() {
			
			@Override
			protected Void doInBackground() throws Exception {
				BackgroundScpiCommand scpi = new BackgroundScpiCommand();
				Command command = new Command("INPut:PORt:STATe?", 1);
				scpi.doInBackground(command);
				return null;
			}
			
			@Override
			protected void done() {
				
			}
		};
		
		worker.execute();
	}
}
