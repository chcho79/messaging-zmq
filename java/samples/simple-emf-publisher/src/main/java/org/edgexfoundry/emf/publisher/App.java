package org.edgexfoundry.emf.publisher;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.domain.core.Reading;
import org.edgexfoundry.emf.EMFCallback;
import org.edgexfoundry.emf.EMFErrorCode;

public class App {
	private static final int mPort = 5562;
	private static EMFErrorCode result = EMFErrorCode.EMF_ERROR;

	public static Event getEdgeXEvent() {

		List<Reading> readings = null;
		readings = new ArrayList<Reading>();
		Random rand = new Random();
		Reading reading1 = new Reading();
		reading1.setName("TestReading1");
		reading1.setValue(rand.nextLong() + "");
		reading1.setCreated(0);
		reading1.setDevice("TestDevice1");
		reading1.setModified(10);
		reading1.setId("id1");
		reading1.setOrigin(15);
		reading1.setPushed(20);

		Reading reading2 = new Reading();
		reading2.setName("TestReading2");
		reading2.setValue(rand.nextLong() + "");
		reading2.setCreated(25);
		reading2.setDevice("TestDevice2");
		reading2.setModified(30);
		reading2.setId("id2");
		reading2.setOrigin(35);
		reading2.setPushed(30);

		readings.add(reading1);
		readings.add(reading2);

		Event event = new Event("Test", readings);
		event.setCreated(10);
		event.setModified(20);
		event.setId("id");
		event.markPushed(new Timestamp(System.currentTimeMillis()).getTime());
		event.setOrigin(new Timestamp(System.currentTimeMillis()).getTime());

		return event;
	}

	private static EMFCallback callbackFactory() {
		EMFCallback mCallback = new EMFCallback() {

			public void onStopCB(EMFErrorCode code) {
				System.out.println("onStopCB Called Code: " + code);
			}

			public void onStartCB(EMFErrorCode code) {
				System.out.println("onStartCB Called Code: " + code);
			}

			public void onErrorCB(EMFErrorCode code) {
				System.out.println("onErrorCB Called Code: " + code);
			}
		};

		return mCallback;
	}

	public static void main(String[] args) throws InterruptedException {

		Publisher pubRef = Publisher.getInstance();
		result = pubRef.startPublisher(mPort, callbackFactory());

		if (result != EMFErrorCode.EMF_OK) {
			System.out.println("start API: error occured");
			return;
		}

		int i = 1;
		System.out.println("--------- Will Publish 15 events at interval of 2 seconds ---------");
		while (i <= 15) {

			result = pubRef.publishEvent(getEdgeXEvent());

			if (result != EMFErrorCode.EMF_OK) {
				System.out.println("publish API: error occured");
				return;
			}
			System.out.println("Event " + i + " Published!");
			Thread.sleep(2000);
			i++;
		}

		result = pubRef.stopPublisher();
		if (result != EMFErrorCode.EMF_OK) {
			System.out.println("stop API: error occured");
			return;
		}
		System.out.println("!!!!! Exiting !!!!");
	}
}