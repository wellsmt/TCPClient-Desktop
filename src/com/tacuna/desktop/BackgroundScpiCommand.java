// Copyright 2013 Marc Bernardini.
package com.tacuna.desktop;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lp.io.Message;
import com.lp.io.MessageConsumer;
import com.lp.io.SimpleDeviceMessage;
import com.tacuna.common.devices.scpi.Command;
import com.tacuna.common.devices.scpi.ScpiMessageExchange;

public class BackgroundScpiCommand implements MessageConsumer {
	private static Logger log = Logger.getLogger(DeviceDetails.class.getName());
    private static final String TAG = "BACKGROUND SCPI COMMAND";
    protected Command outgoing;
    protected ArrayBlockingQueue<SimpleDeviceMessage> result;
    protected ScpiMessageExchange exchange;

    BackgroundScpiCommand() {
    	result = new ArrayBlockingQueue<SimpleDeviceMessage>(1);
    	exchange = (ScpiMessageExchange) ConnectionManager.INSTANCE
    			.getConnectionMessageProducer();
    	exchange.registerObserver(this);
    }
    
    protected SimpleDeviceMessage doInBackground(Command... command) {
    	try {
    		setOutgoing(command[0]);
    		exchange.send(outgoing);
    		return result.take();
    	} catch (InterruptedException e) {
    		log.log(Level.WARNING, "Interrupted waiting for response.", e);
    		return null;
    	}
    }

    public synchronized void setOutgoing(final Command command) {
    	this.outgoing = command;
    }

    @Override
    public void onMessage(Message message) {
    	SimpleDeviceMessage msg = (SimpleDeviceMessage) message;
    	if (msg.getChannel() == this.outgoing.getChannel()) {
    		exchange.removeObserver(this);
    		result.add(msg);
    	}
    }

}
