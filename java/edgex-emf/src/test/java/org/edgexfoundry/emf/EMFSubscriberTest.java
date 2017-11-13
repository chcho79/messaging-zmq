package org.edgexfoundry.emf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.edgexfoundry.emf.EMFSubscriber.EMFSubCallback;
import org.junit.Before;
import org.junit.Test;

public class EMFSubscriberTest {

    private static EMFSubCallback mCallback;
    private static final String mTopic = "topic";
    private static final String mServiceName = "service1";
    private static final String mip = "0.0.0.0";
    private static final int mPort = 5562;

    @Before
    public void setup() {
    }

    @Test
    public void constructorTest1() {
        EMFSubscriber instance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(instance);
    }

    @Test
    public void constructorTest2() {
        EMFSubscriber instance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(instance);
    }

    @Test
    public void constructorTest3() {
        EMFSubscriber instance = new EMFSubscriber(mServiceName, mCallback);
        assertNotNull(instance);
    }

    @Test
    public void startTest() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFSubscriber subInstance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(subInstance);
        assertEquals(EMFErrorCode.EMF_OK, subInstance.start());
        assertEquals(EMFErrorCode.EMF_OK, subInstance.stop());
    }

    @Test
    public void subscribeTest1() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFSubscriber subInstance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(subInstance);
        assertEquals(EMFErrorCode.EMF_OK, subInstance.start());
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe());
        assertEquals(EMFErrorCode.EMF_OK, subInstance.stop());
    }

    @Test
    public void subscribeTest2() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFSubscriber subInstance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(subInstance);
        assertEquals(EMFErrorCode.EMF_OK, subInstance.start());
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe());
        assertEquals(EMFErrorCode.EMF_OK, subInstance.stop());
    }

    @Test
    public void subscribeTest3() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFSubscriber subInstance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(subInstance);
        assertEquals(EMFErrorCode.EMF_OK, subInstance.start());
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(mTopic));
        assertEquals(EMFErrorCode.EMF_OK, subInstance.stop());
    }

    @Test
    public void subscribeTest4() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFSubscriber subInstance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(subInstance);
        assertEquals(EMFErrorCode.EMF_OK, subInstance.start());
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(mTopic));
        assertEquals(EMFErrorCode.EMF_OK, subInstance.stop());
    }

    @Test
    public void subscribeTest5() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFSubscriber subInstance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(subInstance);
        assertEquals(EMFErrorCode.EMF_OK, subInstance.start());

        List<String> topics = new ArrayList<String>();
        topics.add("topic1");
        topics.add("topic2");
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(topics));
        assertEquals(EMFErrorCode.EMF_OK, subInstance.stop());
    }

    @Test
    public void SubscribeTopicTest() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFSubscriber subInstance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(subInstance);
        assertEquals(EMFErrorCode.EMF_OK, subInstance.start());

        // Empty topic test
        String testingTopic = "";
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, subInstance.subscribe(testingTopic));

        // Alphabet test
        testingTopic = "topic";
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(testingTopic));

        // Numeric test
        testingTopic = "123";
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(testingTopic));

        // Alpha-Numeric test
        testingTopic = "1a2b3";
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(testingTopic));

        // Alphabet forward slash test
        testingTopic = "topic/";
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(testingTopic));

        // Alphabet-Numeric, forward slash test
        testingTopic = "topic/13/4jtjos/";
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(testingTopic));

        // Alphabet-Numeric, forward slash test
        testingTopic = "123a/1this3/4jtjos";
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(testingTopic));

        // Alphabet, backslash test
        testingTopic = "topic\";";
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, subInstance.subscribe(testingTopic));

        // Alphabet-Numeric, forward slash and space test
        testingTopic = "topic/13/4jtjos/ ";
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, subInstance.subscribe(testingTopic));

        // Special character test
        testingTopic = "*123a";
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, subInstance.subscribe(testingTopic));

        // Sentence test
        testingTopic = "This is a topic";
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, subInstance.subscribe(testingTopic));

        // Topic contain forward slash at last
        testingTopic = "topic/122/livingroom/";
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(testingTopic));

        // Topic contain -
        testingTopic = "topic/122/livingroom/-";
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(testingTopic));

        // Topic contain _
        testingTopic = "topic/122/livingroom_";
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(testingTopic));

        // Topic contain .
        testingTopic = "topic/122.livingroom.";
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(testingTopic));

        assertEquals(EMFErrorCode.EMF_OK, subInstance.stop());
    }

    @Test
    public void subscribeTestNegative1() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFSubscriber subInstance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(subInstance);
        assertEquals(EMFErrorCode.EMF_OK, subInstance.start());

        String topic = null;
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, subInstance.subscribe(topic));

        List<String> topics = null;
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, subInstance.subscribe(topics));

        topics = new ArrayList<String>();
        topics.add("topic1");
        topics.add(null);

        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, subInstance.subscribe(topics));
        assertEquals(EMFErrorCode.EMF_OK, subInstance.stop());
    }

    @Test
    public void subscribeTestNegative2() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());

        // invalid port
        EMFSubscriber subInstance = new EMFSubscriber("localhost", -1, mCallback);
        EMFErrorCode errorCode = subInstance.start();
        assertEquals(EMFErrorCode.EMF_ERROR, errorCode);
        subInstance.stop();
        apiInstance.terminate();
    }

    @Test
    public void unsubscribeTest1() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFSubscriber subInstance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(subInstance);
        assertEquals(EMFErrorCode.EMF_OK, subInstance.start());
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe("topic"));
        assertEquals(EMFErrorCode.EMF_OK, subInstance.unSubscribe("topic"));
        assertEquals(EMFErrorCode.EMF_OK, subInstance.stop());
    }

    @Test
    public void unsubscribeTest2() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFSubscriber subInstance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(subInstance);
        assertEquals(EMFErrorCode.EMF_OK, subInstance.start());

        List<String> topics = new ArrayList<String>();
        topics.add("topic1");
        topics.add("topic2");
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe(topics));
        assertEquals(EMFErrorCode.EMF_OK, subInstance.unSubscribe(topics));
        assertEquals(EMFErrorCode.EMF_OK, subInstance.stop());
    }

    @Test
    public void unsubscribeNegativeTest() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFSubscriber subInstance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(subInstance);
        assertEquals(EMFErrorCode.EMF_OK, subInstance.start());
        assertEquals(EMFErrorCode.EMF_OK, subInstance.subscribe("topic"));

        String topic = null;
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, subInstance.unSubscribe(topic));

        topic = "!$tpoic";
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, subInstance.unSubscribe(topic));

        List<String> topics = new ArrayList<String>();
        topics.add("topic1");
        topics.add(null);
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, subInstance.unSubscribe(topics));

        assertEquals(EMFErrorCode.EMF_OK, subInstance.stop());
    }

    @Test
    public void stopTest() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFSubscriber subInstance = new EMFSubscriber(mip, mPort, mCallback);
        assertNotNull(subInstance);
        assertEquals(EMFErrorCode.EMF_OK, subInstance.start());
        assertEquals(EMFErrorCode.EMF_OK, subInstance.stop());
    }

    @Test
    public void startStopTest() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFSubscriber subInstance = new EMFSubscriber(mip, mPort, mCallback);
        for (int i = 1; i <= 10; i++) {
            assertEquals(EMFErrorCode.EMF_OK, subInstance.start());
            assertEquals(EMFErrorCode.EMF_OK, subInstance.stop());
        }
    }

    @Test
    public void uniqueAddressTest() {
        for (int i = 1; i <= 100; i++) {
            System.out.println("Address: " + TestUtils.getInProcUniqueAddress());
        }
    }

    @Test
    public void getIpTest() {
        EMFSubscriber instance = new EMFSubscriber(mip, mPort, mCallback);
        assertEquals(mip, instance.getIp());
    }

    @Test
    public void getPortTest() {
        EMFSubscriber instance = new EMFSubscriber(mip, mPort, mCallback);
        assertEquals(mPort, instance.getPort());
    }

    @Test
    public void getServiceNameTest() {
        EMFSubscriber instance = new EMFSubscriber(mServiceName, mCallback);
        assertEquals(mServiceName, instance.getServiceName());
    }
}
