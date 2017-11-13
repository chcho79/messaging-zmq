package org.edgexfoundry.emf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.edgexfoundry.domain.core.Event;
import org.junit.Before;
import org.junit.Test;

public class EMFPublisherTest {

    private static EMFCallback mCallback;
    private static final String mTopic = "topic";
    private static final int mPort = 5562;

    @Before
    public void setup() {
        mCallback = new EMFCallback() {
            public void onStopCB(EMFErrorCode code) {
            }

            public void onStartCB(EMFErrorCode code) {
            }

            public void onErrorCB(EMFErrorCode code) {
            }
        };
    }

    @Test
    public void constructorTest1() {
        EMFPublisher instance = new EMFPublisher(mPort, mCallback);
        assertNotNull(instance);
    }

    @Test
    public void constructorTest2() {
        EMFPublisher instance = new EMFPublisher(mPort, mCallback);
        assertNotNull(instance);
    }

    @Test
    public void startTest() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFPublisher pubInstance = new EMFPublisher(mPort, mCallback);
        assertNotNull(pubInstance);
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.start());
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.stop());
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.terminate());
    }

    @Test
    public void publishTest1() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFPublisher pubInstance = new EMFPublisher(mPort, mCallback);
        assertNotNull(pubInstance);
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.start());

        Event event = TestUtils.getEdgeXEvent();

        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(event));
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.stop());
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.terminate());
    }

    @Test
    public void publishTest2() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFPublisher pubInstance = new EMFPublisher(mPort, mCallback);
        assertNotNull(pubInstance);
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.start());

        Event event = TestUtils.getEdgeXEvent();

        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(event));
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.stop());
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.terminate());
    }

    @Test
    public void publishTest3() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFPublisher pubInstance = new EMFPublisher(mPort, mCallback);
        assertNotNull(pubInstance);
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.start());

        Event event = TestUtils.getEdgeXEvent();

        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(mTopic, event));
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.stop());
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.terminate());
    }

    @Test
    public void publishTest4() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFPublisher pubInstance = new EMFPublisher(mPort, mCallback);
        assertNotNull(pubInstance);
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.start());

        Event event = TestUtils.getEdgeXEvent();

        List<String> topics = new ArrayList<String>();
        topics.add("topic1");
        topics.add("topic2");

        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(topics, event));
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.stop());
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.terminate());
    }

    @Test
    public void publishTopicTest() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFPublisher pubInstance = new EMFPublisher(mPort, mCallback);
        assertNotNull(pubInstance);
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.start());

        Event event = TestUtils.getEdgeXEvent();

        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(mTopic, event));

        // Empty topic test
        String testingTopic = "";
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, pubInstance.publish(testingTopic, event));

        // Alphabet test
        testingTopic = "topic";
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(testingTopic, event));

        // Numeric test
        testingTopic = "123";
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(testingTopic, event));

        // Alpha-Numeric test
        testingTopic = "1a2b3";
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(testingTopic, event));

        // Alphabet forward slash test
        testingTopic = "topic/";
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(testingTopic, event));

        // Alphabet-Numeric, forward slash test
        testingTopic = "topic/13/4jtjos/";
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(testingTopic, event));

        // Alphabet-Numeric, forward slash test
        testingTopic = "123a/1this3/4jtjos";
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(testingTopic, event));

        // Alphabet, backslash test
        testingTopic = "topic\";";
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, pubInstance.publish(testingTopic, event));

        // Alphabet-Numeric, forward slash and space test
        testingTopic = "topic/13/4jtjos/ ";
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, pubInstance.publish(testingTopic, event));

        // Special character test
        testingTopic = "*123a";
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, pubInstance.publish(testingTopic, event));

        // Sentence test
        testingTopic = "This is a topic";
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, pubInstance.publish(testingTopic, event));

        // Topic contain forward slash at last
        testingTopic = "topic/122/livingroom/";
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(testingTopic, event));

        // Topic contain -
        testingTopic = "topic/122/livingroom/-";
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(testingTopic, event));

        // Topic contain _
        testingTopic = "topic/122/livingroom_";
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(testingTopic, event));

        // Topic contain .
        testingTopic = "topic/122.livingroom.";
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.publish(testingTopic, event));

        assertEquals(EMFErrorCode.EMF_OK, pubInstance.stop());
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.terminate());
    }

    @Test
    public void publishNegativeTest1() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        EMFPublisher pubInstance = new EMFPublisher(mPort, mCallback);
        assertNotNull(pubInstance);
        assertEquals(EMFErrorCode.EMF_ERROR, pubInstance.start());

        Event event = TestUtils.getWrongEvent();

        assertEquals(EMFErrorCode.EMF_ERROR, pubInstance.publish(event));
        assertEquals(EMFErrorCode.EMF_ERROR, pubInstance.stop());
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.terminate());
    }

    @Test
    public void publishNegativeTest2() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        EMFPublisher pubInstance = new EMFPublisher(mPort, mCallback);
        assertNotNull(pubInstance);
        assertEquals(EMFErrorCode.EMF_ERROR, pubInstance.start());

        Event event = TestUtils.getWrongEvent();

        assertEquals(EMFErrorCode.EMF_ERROR, pubInstance.publish(mTopic, event));
        assertEquals(EMFErrorCode.EMF_ERROR, pubInstance.stop());
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.terminate());
    }

    @Test
    public void publishNegativeTest3() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFPublisher pubInstance = new EMFPublisher(mPort, mCallback);
        assertNotNull(pubInstance);
        assertEquals(EMFErrorCode.EMF_OK, pubInstance.start());

        Event event = TestUtils.getEdgeXEvent();
        String topic = null;
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, pubInstance.publish(topic, event));

        List<String> topics = null;
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, pubInstance.publish(topics, event));

        topics = new ArrayList<String>();
        topics.add("topic1");
        topics.add(null);
        assertEquals(EMFErrorCode.EMF_INVALID_TOPIC, pubInstance.publish(topics, event));

        assertEquals(EMFErrorCode.EMF_OK, pubInstance.stop());
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.terminate());
    }

    @Test
    public void startStopTest() {
        EMFAPI apiInstance = EMFAPI.getInstance();
        assertNotNull(apiInstance);
        assertEquals(EMFErrorCode.EMF_OK, apiInstance.initialize());
        EMFPublisher pubInstance = new EMFPublisher(mPort, mCallback);
        for (int i = 1; i <= 10; i++) {
            assertNotNull(pubInstance);
            assertEquals(EMFErrorCode.EMF_OK, pubInstance.start());
            assertEquals(EMFErrorCode.EMF_OK, pubInstance.stop());
        }
    }

    @Test
    public void getPortTest() {
        EMFPublisher pubInstance = new EMFPublisher(mPort, mCallback);
        assertEquals(mPort, pubInstance.getPort());
    }
}
