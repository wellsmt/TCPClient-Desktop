// Copyright 2013 Marc Bernardini.
package com.tacuna.desktop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lp.io.MessageProducer;
import com.lp.io.SocketConnector;
import com.lp.io.UdpBroadcast;
import com.tacuna.common.devices.AD7195W;
import com.tacuna.common.devices.DeviceCommandSchedule;
import com.tacuna.common.devices.DeviceInterface;

/**
 * Singleton connection manager class. Manages connections and their data
 * interpreters. A singleton instance is used so that connections persist
 * between views. This Singleton uses the singleton enum pattern.
 * 
 * @author marc
 * 
 */
public enum ConnectionManager implements PropertyChangeListener {
    /** The singleton instance of the ConnectionManager */
    INSTANCE;
	private static Logger log = Logger.getLogger(ConnectionManager.class.getName());
    private static final String TAG = "CONNECTION MANAGER";

    /** This is the devices UDP listening port. */
    public static final int DEVICE_LISTENING_PORT = 30303;
    /**
     * UDP response port. Note that for now this must be the same as the
     * listening port due to device limitations.
     */
    public static final int RESPONSE_PORT = 30303;

    private UdpBroadcast broadcaster;
    // TODO: Store multiple connections, this should be a list in the future
    private SocketConnector connection;

    private AD7195W device = null;

    public DeviceInterface getDevice() {
	return device;
    }

    private final DeviceCommandSchedule schedule = null;

    private final HashMap<String, DeviceCommandSchedule> deviceSchedules = new HashMap<String, DeviceCommandSchedule>();

    public DeviceCommandSchedule getScheduleByDeviceName(String device) {
	return deviceSchedules.get(device);
    }

    // private DataInterpreter dataInterpreter = new
    // ProtoBuffersDataFrameInterpretor();
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
	    1);

    /**
     * Socket connection factory method. Currently, the APP can only have one
     * open connection but that may change
     * 
     * @param host
     * @param port
     * @return
     */
    public SocketConnector createConnection(final String host, int port) {
	// Attempt connection.
    	try {
    		if (connection != null && connection.isConnected()) {
    			connection.removeChangeListener(this);
    			connection.close();
    		}

    		log.log(Level.INFO, String.format("Creating connection to %s:%d", host, port));
//    		if(device != null){
//    			device.close();
//    		}
    		device = new AD7195W();
    		device.setNetworkAddress(InetSocketAddress.createUnresolved(host, port));
    		device.connect();
    		connection = device.getConnection();

    		connection.addChangeListener(this);
    		DeviceCommandSchedule schedule = new DeviceCommandSchedule(device);

    		deviceSchedules.put(device.getDeviceName(), schedule);
    		return connection;
    	} catch (final Exception err) {
    		log.log(Level.SEVERE, "Unable to create connection.", err);
    	}
    	return null;
    }

    /**
     * Returns true if the application is connected to the device specified by
     * the device connection info.
     * 
     * @param info
     * @return
     */
    public boolean isAppConnected(DeviceConnectionInformation info) {
	if (connection == null) {
	    return false;
	}
	if (connection.isConnected()
		&& connection.getHost().equals(info.getHost())) {
	    return true;
	}
	return false;
    }

    public MessageProducer getConnectionMessageProducer() {
	if (device != null) {
	    return device.getEx();
	}
	return null;
    }

    /**
     * Returns the UdpBroadcaster. This method will create the broadcaster the
     * first time it is called.
     * 
     * @param context
     *            Application context needed to access the WIFI service.
     * @return UdpBroadcast
     * @throws IOException
     */
    public UdpBroadcast getBroadcaster(){
	if (broadcaster == null) {
	    try {
			broadcaster = new UdpBroadcast(RESPONSE_PORT,
					getBroadcastAddress());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    new UdpListenThread(broadcaster);
	}
	return broadcaster;
    }

    /**
     * Returns the broadcast address used on the current WIFI network.
     * 
     * @param context
     *            Application context needed to access the WIFI service.
     * @return InetAdress for broadcasts
     * @throws IOException
     */
    protected InetAddress getBroadcastAddress(){
    	try {
			return InetAddress.getByName("255.255.255.255");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;			
		}
    }

    /**
     * Closes the connections. Right now there is only one connection but that
     * may change.
     */
    public void closeAll() {
    	if (connection != null) {
    		connection.removeChangeListener(this);
	    	connection.close();
		}
    	if(device != null){
    		device.close();
    	}
    }

    /**
     * In order to make the UDP broad cast listen in the background we have to
     * run it in its own thread. Originally I had set this up to just be an
     * AsynchTask but that doesn't work for blocking calls (like receive) on
     * Android 3.+ due to the API running all asynch tasks on a single thread.
     */
    protected class UdpListenThread extends Thread {
	/**
	 * Constructor. Takes in the UdpBroadcast instance and runs it in its
	 * own thread.
	 * 
	 * @param broadcaster
	 */
	UdpListenThread(UdpBroadcast broadcaster) {
	    super(broadcaster);
	    start();
	}

    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
	notifyListeners("connection", null, connection);
    }

    private final ArrayList<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    /**
     * Register a change listener for receiving state change updates.
     * 
     * @param listener
     */
    public void addChangeListener(PropertyChangeListener listener) {
	listeners.add(listener);
    }

    /**
     * Removes a previously registered listener.
     * 
     * @param listener
     */
    public void removeChangeListener(PropertyChangeListener listener) {
	listeners.remove(listener);
    }

    protected void notifyListeners(String property, Object oldValue,
	    Object newValue) {
	for (PropertyChangeListener listener : listeners) {
	    listener.propertyChange(new PropertyChangeEvent(this, property,
		    oldValue, newValue));
	}
    }
}
