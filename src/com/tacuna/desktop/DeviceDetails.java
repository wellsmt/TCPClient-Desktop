package com.tacuna.desktop;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import com.lp.io.SocketConnector;
import com.tacuna.common.devices.ChannelInterface;
import com.tacuna.common.devices.scpi.Command;
import javax.swing.JToggleButton;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JInternalFrame;

public class DeviceDetails extends JPanel {

	private static Logger log = Logger.getLogger(DeviceDetails.class.getName());
	SocketConnector connection = null;
	DeviceConnectionInformation device = null;
	DeviceChannelLogger channelLogger = null;
	JButton btnConnect = null;
	JButton btnClose = null;
	JPanel panelChannels = null;
	/**
	 * Create the frame.
	 */
	public DeviceDetails(final DeviceConnectionInformation device) {
		setBounds(100, 100, 450, 300);		
		this.device = device;
		btnClose = new JButton("Close");
		btnClose.setHorizontalAlignment(SwingConstants.TRAILING);
		btnClose.addActionListener(new ActionListener(){			
			@Override
			public void actionPerformed(ActionEvent e) {					
					startConnection();
					btnConnect.setEnabled(true);
					btnClose.setEnabled(false);
			}
		});
		btnConnect = new JButton("Connect");
		btnConnect.setHorizontalAlignment(SwingConstants.TRAILING);
		btnConnect.addActionListener(new ActionListener(){			
			@Override
			public void actionPerformed(ActionEvent e) {					
					startConnection();
					btnConnect.setEnabled(false);
					btnClose.setEnabled(true);
			}
		});
			
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(btnConnect);
		add(btnClose);
		
		panelChannels = new JPanel();
		add(panelChannels);
				
		setVisible(true);
	}

	private void startConnection(){
		SwingWorker<Void,Void> worker = new SwingWorker<Void, Void>() {
			
			@Override
			protected Void doInBackground() throws Exception {
				do {
					if(null == connection){
						connection = ConnectionManager.INSTANCE.createConnection(device.getHost(), device.getPort());						
					} else {
						channelLogger.stopLogging();
						ConnectionManager.INSTANCE.closeAll();
						connection = null;
						return null;
					}
				} while(!connection.isConnected());
				return null;
			}
			
			@Override
			protected void done() {
				
				if(connection != null){
									
					channelLogger = new DeviceChannelLogger(ConnectionManager.INSTANCE.getDevice());
					channelLogger.startLogging();

					for (ChannelInterface channel :	ConnectionManager.INSTANCE.getDevice().getChannels()) {
						ChannelPanel newChannelPanel = new ChannelPanel(channel);
					
						panelChannels.add(newChannelPanel);	
					}	
				}				
			}
		};
		
		worker.execute();
	}
}
