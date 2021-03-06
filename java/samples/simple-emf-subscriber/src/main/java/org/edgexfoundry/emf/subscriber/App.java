package org.edgexfoundry.emf.subscriber;

import java.util.List;

import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.domain.core.Reading;
import org.edgexfoundry.emf.EMFErrorCode;
import org.edgexfoundry.emf.EMFSubscriber.EMFSubCallback;

public class App {
	
	private static final int mPort = 5562;
	private static final String mHost = "localhost";
	private static EMFErrorCode result = EMFErrorCode.EMF_ERROR;
	
	 private static EMFSubCallback callbackFactory() {
		 EMFSubCallback mCallback = new EMFSubCallback() {
	            public void onMessageCB(Event event) {
	                System.out.println("[-------------------------------------");
	                System.out.println("[APP: onMessageCB]");
	                System.out.println("Device: " + event.getDevice());
	                System.out.println("Readings: ");
	                List<Reading> readings = event.getReadings();
	                for (Reading reading : readings) {
	                    System.out.print("Key: " + reading.getName());
	                    System.out.println("  Value: " + reading.getValue());
	                }
	                System.out.println("----------------------------------------");
	            }

	            public void onMessageCB(String topic, Event event) {
	                System.out.println("[-------------------------------------");
	                System.out.println("[APP: onMessageCB]");
	                System.out.println("Topic: " + topic);
	                System.out.println("Device: " + event.getDevice());
	                System.out.println("Readings: ");
	                List<Reading> readings = event.getReadings();
	                for (Reading reading : readings) {
	                    System.out.print("Key: " + reading.getName());
	                    System.out.println("  Value: " + reading.getValue());
	                }
	                System.out.println("----------------------------------------");
	            }
	        };
	        
	        return mCallback;
	    }

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Subscriber subRef = Subscriber.getInstance();
		result = subRef.startSubscriber(mHost, mPort, null, callbackFactory());
		
		if (result != EMFErrorCode.EMF_OK)
		{
			System.out.println("subscribe API: error occured");
			return;
		}
	}

}
