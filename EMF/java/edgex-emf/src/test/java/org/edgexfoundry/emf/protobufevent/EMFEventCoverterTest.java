package org.edgexfoundry.emf.protobufevent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.emf.TestUtils;
import org.junit.Test;

public class EMFEventCoverterTest {

    @Test
    public void toProtoBufTest() {
        Event event = TestUtils.getEdgeXEvent();
        byte[] byteEvent = EMFEventConverter.toProtoBuf(event);
        assertNotNull(byteEvent);
    }

    @Test
    public void toProtoBufNegativeTest1() {
        Event event = TestUtils.getWrongEvent();
        byte[] byteEvent = EMFEventConverter.toProtoBuf(event);
        assertEquals(null, byteEvent);
    }

    @Test
    public void toProtoBufNegativeTest2() {
        byte[] byteEvent = EMFEventConverter.toProtoBuf(null);
        assertEquals(null, byteEvent);
    }

    @Test
    public void toEdgeXEventTest() {
        Event event = TestUtils.getEdgeXEvent();
        byte[] byteEvent = EMFEventConverter.toProtoBuf(event);
        Event edgexEvent = EMFEventConverter.toEdgeXEvent(byteEvent);
        assertNotNull(event);
        assertEquals(edgexEvent, event);
    }

    @Test
    public void toEdgeXEventNegativeTest() {
        Event edgexEvent = EMFEventConverter.toEdgeXEvent(null);
        assertEquals(null, edgexEvent);
    }

}
