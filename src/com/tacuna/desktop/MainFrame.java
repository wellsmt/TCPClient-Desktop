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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
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
import javax.swing.JLayeredPane;
import com.javaswingcomponents.accordion.JSCAccordion;
import com.javaswingcomponents.accordion.TabOrientation;
import com.javaswingcomponents.accordion.listener.AccordionEvent;
import com.javaswingcomponents.accordion.listener.AccordionListener;

public class MainFrame extends JFrame {

	private HashSet<DeviceConnectionInformation> deviceConnectionInformation = new HashSet<DeviceConnectionInformation>();
	private static Logger log = Logger.getLogger(MainFrame.class.getName());
	private JPanel contentPane;	
	private	JSCAccordion deviceList;

	/**
	 * Create the frame.
	 */
	public MainFrame(String title) {
		super(title);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 600);
		contentPane = new JPanel();

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
		
		deviceList = new JSCAccordion();
		deviceList.setTabOrientation(TabOrientation.VERTICAL);
		
		deviceList.addAccordionListener(new AccordionListener() {
			
			@Override
			public void accordionChanged(AccordionEvent accordionEvent) {
				//available fields on accordionEvent.
				
				switch (accordionEvent.getEventType()) {
				case TAB_ADDED: {
					deviceList.setSelectedIndex(accordionEvent.getTabIndex());
					break;
				}
				case TAB_REMOVED: {
					//add your logic here to react to a tab being removed.
					break;					
				}
				case TAB_SELECTED: {
					//add your logic here to react to a tab being selected.
					break;					
				}
				}
			}
		});

		/* TEST CODE */
		//deviceList.addTab("TESTDEVICE001", new JScrollPane(new JLabel("Insert Device Details here...")));
		/* TEST CODE */

		tabbedPane.addTab("Data", null, deviceList, null);				
		tabbedPane.addTab("Config", null, new JPanel(), null);
					
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
					UdpBroadcast broadcaster = ConnectionManager.INSTANCE.getBroadcaster();
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
				HashSet<DeviceConnectionInformation> newDevices = chunks.get(chunks.size() - 1);
				for (DeviceConnectionInformation device : newDevices) {

					if( !this.contains(device) ) {						
						deviceConnectionInformation.add(device);
						
						String title = device.getName() + " " + (new Date()).toString();	
						JTextArea details = new JTextArea("DETAILS" + "\nHOST : " + device.getHost() + "\nPORT : " + device.getPort() + "\nMAC ADDRESS : " + device.getMacAddress());
						deviceList.addTab(title, details);												
					}
					else {
						// TODO: Do some update type stuff here, like device status/type/etc
					}
				}
			}

			private boolean contains(DeviceConnectionInformation newDevice) {
				for(DeviceConnectionInformation device : deviceConnectionInformation) {
					if(device.getMacAddress().equals(newDevice.getMacAddress()))
						return true;
				}
				
				return false;				
			}



			@Override
			protected void done() {
												
			}
				
		};
		
		worker.execute();
	}
		
}
