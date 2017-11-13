package org.edgexfoundry.emf.protobufevent;

import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.domain.core.Reading;
import org.edgexfoundry.emf.protobufevent.EMFProtoEvent.Event.Builder;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;

/**
 * This class represents the EMFEventConverter It provides methods to convert
 * edgeX message to protocol buffer message.
 */
public class EMFEventConverter {

    private final static EdgeXLogger logger = EdgeXLoggerFactory
            .getEdgeXLogger(EMFEventConverter.class);

    /**
     * Convert EdgeX event to protocol buffer event byte array.
     *
     * @param {@link
     *            Event}
     * @return Converted Protobuf event as byte array.
     */
    public static byte[] toProtoBuf(Event event) {

        if (null == event) {
            return null;
        }

        org.edgexfoundry.emf.protobufevent.EMFProtoEvent.Event protoEventObj = null;
        org.edgexfoundry.emf.protobufevent.EMFProtoEvent.Event protoEvent = null;
        try {

            protoEventObj = new org.edgexfoundry.emf.protobufevent.EMFProtoEvent.Event(event);
            Builder eventbuild = EMFProtoEvent.Event.newBuilder();
            eventbuild.setEdgeXReading(event);

            for (Reading reading : event.getReadings()) {
                org.edgexfoundry.emf.protobufevent.EMFProtoEvent.Reading protoReading = new org.edgexfoundry.emf.protobufevent.EMFProtoEvent.Reading(
                        reading);
                org.edgexfoundry.emf.protobufevent.EMFProtoEvent.Reading.Builder readingBuild = EMFProtoEvent.Reading
                        .newBuilder();
                readingBuild.setEdgeXReading(reading);
                eventbuild.addReading(readingBuild);
            }

            protoEvent = eventbuild.build();
        } catch (Exception e) {
            logger.error("toEvent: Invalid byte array", e.getMessage());
        }
        if (null == protoEvent) {
            return null;
        }
        return protoEvent.toByteArray();
    }

    /**
     * Convert byte[] array of Protocol buffer event to edgeX event.
     *
     * @return EdgeX event.
     */
    public static Event toEdgeXEvent(byte[] event) {

        if (null == event) {
            return null;
        }

        Event edgexEvent = null;
        try {
            org.edgexfoundry.emf.protobufevent.EMFProtoEvent.Event eventObj = org.edgexfoundry.emf.protobufevent.EMFProtoEvent.Event
                    .parseFrom(event);
            edgexEvent = eventObj.getEdgeXEventObject();
        } catch (Exception e) {
            logger.error("toEvent: Invalid byte array", e.getMessage());
        }
        return edgexEvent;
    }
}
