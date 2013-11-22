package com.tacuna.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.lp.io.DeviceBroadcastMessage;
import com.lp.io.Message;
import com.lp.io.MessageConsumer;
import com.lp.io.UdpBroadcast;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {

	private HashSet<DeviceConnectionInformation> deviceConnectionInformation;
	private static Logger log = Logger.getLogger(MainFrame.class.getName());
	private JPanel contentPane;	
	private JList deviceList;	
	private DefaultListModel listModel;	

	/**
	 * Create the frame.
	 */
	public MainFrame(String title) {
		super(title);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		listModel = new DefaultListModel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);		
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, "1, 1, 10, 8, fill, fill");
		
		JPanel devicePanel = new JPanel();
		tabbedPane.addTab("Data", null, devicePanel, null);
		deviceList = new JList(listModel);
		devicePanel.add(deviceList);
		
		JPanel configPanel = new JPanel();
		tabbedPane.addTab("Config", null, configPanel, null);		
		setVisible(true);
		
		startDiscovery();
	}
	
	private void startDiscovery()
	{	
		SwingWorker<Void, HashSet<DeviceConnectionInformation>> worker = new SwingWorker<Void, HashSet<DeviceConnectionInformation>>() {

			@Override
			protected Void doInBackground() throws Exception {

			    BroadcastPrintingClass printer = new BroadcastPrintingClass();
				try {				    


				    // The IP address can be 255.255.255.255 but most documents
				    // seem to indicate that its better to use the local broadcast
				    // address which might change from network to network. A local
				    // address on a typical home network would look like 192.168.1.255.
				    // We need to figure out a better to do this find the local address.
				    UdpBroadcast broadcaster = new UdpBroadcast(12345,
					    InetAddress.getByName("192.168.1.255"));
				    broadcaster.registerObserver(printer);
				    Thread listenThread = new Thread(broadcaster);
				    listenThread.start();
				    while (true) {
					Thread.sleep(1000);
					System.out.println("Sending message...");
					broadcaster.send("Discovery: Who is out there?\r\n", 30303);

					Thread.sleep(1000);
					publish(printer.getDevices());
					// Connect to the device here:
					// SocketConnector connector = new SocketConnector();
				    }
				} catch (SocketException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				} catch (UnknownHostException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				} catch (InterruptedException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				} catch (IOException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				}		
				return null;
			}

			
			
			@Override
			protected void process(List<HashSet<DeviceConnectionInformation>> chunks) {				
				deviceConnectionInformation = chunks.get(chunks.size() - 1);
				listModel.clear();
				for (DeviceConnectionInformation device : deviceConnectionInformation) {
					listModel.addElement(device.getName() + " " + device.getTimeString());
				}
			}

			@Override
			protected void done() {
												
			}
							
		};
		
		worker.execute();
	}
	
	public class BroadcastPrintingClass implements MessageConsumer {
		static final long TIMEOUT_MILLI = 30000; // TODO: Consider timeing out resources
		protected HashSet<DeviceConnectionInformation> devices = new HashSet<DeviceConnectionInformation>();
		@Override
		public void onMessage(Message message) {
		    DeviceBroadcastMessage broadCastMessage = (DeviceBroadcastMessage) message;
		    DeviceConnectionInformation newDevice = new DeviceConnectionInformation(broadCastMessage.getHost(), 
		    		broadCastMessage.getTcpPort(), 
		    		broadCastMessage.getMacAddress(), 
		    		broadCastMessage.getDeviceName(),
		    		broadCastMessage.getTimestamp());
		    		    
		    devices.add(newDevice);
		}
		
		public HashSet<DeviceConnectionInformation> getDevices() {				
			return devices;
		}
	}

}
