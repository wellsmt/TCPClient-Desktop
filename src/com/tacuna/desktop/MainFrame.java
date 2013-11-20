package com.tacuna.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;

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

public class MainFrame extends JFrame {

	private JPanel contentPane;	
	private JButton refreshButton;
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
		refreshButton = new JButton("Refresh");		
		listModel = new DefaultListModel();
		deviceList = new JList(listModel);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);		
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
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
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
				
		contentPane.add(deviceList, "2, 2, fill, fill");
				
		contentPane.add(refreshButton, "8, 6");
		
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		startDiscovery();
		setVisible(true);
	}
	
	private void startDiscovery()
	{	
		SwingWorker<Void, HashSet<String>> worker = new SwingWorker<Void, HashSet<String>>() {

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
					publish(printer.getDeviceAddresses());
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
			protected void process(List<HashSet<String>> chunks) {
				HashSet<String> devices = chunks.get(chunks.size() - 1);
				listModel.clear();
				for (String device : devices) {
					listModel.addElement(device);
				}
			}

			@Override
			protected void done() {
												
			}
							
		};
		
		worker.execute();
	}
	
	public class BroadcastPrintingClass implements MessageConsumer {
		protected HashSet<String> deviceAddresses = new HashSet<String>();
		@Override
		public void onMessage(Message message) {
		    DeviceBroadcastMessage broadCastMessage = (DeviceBroadcastMessage) message;
		    deviceAddresses.add(broadCastMessage.getHost() + ":"
			    + Integer.toString(broadCastMessage.getTcpPort()));
		}
		
		public HashSet<String> getDeviceAddresses() {
			return deviceAddresses;
		}
	}

}