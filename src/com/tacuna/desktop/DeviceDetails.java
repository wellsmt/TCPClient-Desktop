package com.tacuna.desktop;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import com.lp.io.SocketConnector;
import com.tacuna.common.devices.AnalogInputChannel;
import com.tacuna.common.devices.ChannelInterface;
import com.tacuna.common.devices.DeviceInterface;
import com.tacuna.common.devices.DigitalInputChannel;
import com.tacuna.common.devices.scpi.Command;
import javax.swing.JToggleButton;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JInternalFrame;
import java.awt.GridLayout;

public class DeviceDetails extends JPanel {

	private static Logger log = Logger.getLogger(DeviceDetails.class.getName());
	SocketConnector connection = null;
	DeviceConnectionInformation deviceInformation = null;
	DeviceInterface device = null;
	DeviceChannelLogger channelLogger = null;
	JToggleButton btnConnect = null;
	
	/**
	 * Create the frame.
	 */
	public DeviceDetails(final DeviceConnectionInformation deviceInformation) {
		setBounds(100, 100, 450, 300);		
		this.deviceInformation = deviceInformation;
		this.device = ConnectionManager.INSTANCE.getDevice();

		btnConnect = new JToggleButton("OFF");
		btnConnect.setBackground(Color.red);		
		btnConnect.setHorizontalAlignment(SwingConstants.LEFT);
		btnConnect.addItemListener(new ItemListener(){						
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED){
					btnConnect.setText("ON");
					btnConnect.setBackground(Color.green);
					connection = ConnectionManager.INSTANCE.createConnection(deviceInformation.getHost(), deviceInformation.getPort());
					waitForConnection();
				} else {
					btnConnect.setText("OFF");
					btnConnect.setBackground(Color.red);
					closeConnection();
				}				
			}
		});

		add(btnConnect);
		int numAI = 8;
		int numDI = 8;
		for(int aindex = 0;aindex < numAI;aindex++) {
			ChannelPanel channel = new ChannelPanel(new AnalogInputChannel("AI:"+Integer.toString(aindex)), aindex);
			add(channel);
		}
		for(int dindex = 0;dindex < numDI;dindex++) {
			ChannelPanel channel = new ChannelPanel(new DigitalInputChannel("DI:"+Integer.toString(dindex)), dindex);
			add(channel);
		}	
				
		setVisible(true);
	}

	private void waitForConnection(){
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {				
				while(!connection.isConnected());
				return null;
			}
			
			@Override
			protected void done() {													
				channelLogger = new DeviceChannelLogger(ConnectionManager.INSTANCE.getDevice());
				channelLogger.startLogging();			
			}
		};
		worker.execute();
	}
	
	private void closeConnection(){
		channelLogger.stopLogging();
		ConnectionManager.INSTANCE.closeAll();
	}
}
