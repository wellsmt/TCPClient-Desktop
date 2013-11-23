package com.tacuna.desktop;

import java.util.HashSet;

import com.lp.io.DeviceBroadcastMessage;
import com.lp.io.Message;
import com.lp.io.MessageConsumer;

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
