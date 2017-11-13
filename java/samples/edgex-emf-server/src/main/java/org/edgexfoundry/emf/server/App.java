package org.edgexfoundry.emf.server;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.domain.core.Reading;
import org.edgexfoundry.emf.EMFAPI;
import org.edgexfoundry.emf.EMFCallback;
import org.edgexfoundry.emf.EMFErrorCode;
import org.edgexfoundry.emf.EMFPublisher;

public class App {
    private static EMFCallback mCallback;
    private static final int mPort = 5562;
    private static EMFErrorCode result = EMFErrorCode.EMF_ERROR;

    private static void callback() {
        mCallback = new EMFCallback() {

            @Override
            public void onStopCB(EMFErrorCode code) {
            }

            @Override
            public void onStartCB(EMFErrorCode code) {
            }

            @Override
            public void onErrorCB(EMFErrorCode code) {
            }
        };
    }

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

    public static void main(String[] args) throws InterruptedException {
        EMFAPI apiInstance = EMFAPI.getInstance();
        apiInstance.initialize();
        callback();

        EMFPublisher pubInstance = null;
        int choice = -1;
        String topic = null;

        System.out.println("Enter 1 for General Event testing");
        System.out.println("Enter 2 for Topic Based delivery");

        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        choice = scanner.nextInt();

        switch (choice) {
        case 1:
            pubInstance = new EMFPublisher(mPort, mCallback);
            break;
        case 2:
            pubInstance = new EMFPublisher(mPort, mCallback);
            System.out.print("Enter the topic: ");
            topic = scanner.next();
            System.out.println("Topic is : " + topic);
            break;
        default:
            System.out.println("Invalid choice..[Re-run application]");
            return;
        }

        result = pubInstance.start();
        if (result != EMFErrorCode.EMF_OK) {
            System.out.println("start API: error occured");
            return;
        }

        Event event = getEdgeXEvent();
        int i = 1;
        System.out.println("--------- Will Publish 15 events at interval of 2 seconds ---------");
        while (i <= 15) {
            if (null == topic) {
                result = pubInstance.publish(event);
            } else {
                result = pubInstance.publish(topic, event);
            }
            if (result != EMFErrorCode.EMF_OK) {
                System.out.println("publish API: error occured");
                return;
            }
            System.out.println("Event " + i + " Published!");
            Thread.sleep(2000);
            i++;
        }

        // stop API call is required to clear zeroMQ socket
        result = pubInstance.stop();
        if (result != EMFErrorCode.EMF_OK) {
            System.out.println("stop API: error occured");
            return;
        }
        System.out.println("!!!!! Exiting !!!!");
    }
}
