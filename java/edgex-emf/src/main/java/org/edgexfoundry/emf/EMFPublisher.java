package org.edgexfoundry.emf;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.emf.protobufevent.EMFEventConverter;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import org.zeromq.ZMQ;

/**
 * This class represents the EMF Publisher.
 */
public class EMFPublisher {

    private int mPort;
    @SuppressWarnings("unused")
    private EMFCallback mCallback;

    private ZMQ.Socket mPublisher;
    private ZMQ.Context mContext;

    // Thread safety lock
    private ReentrantLock mPubLock;

    private final String PUB_TCP_PREFIX = "tcp://*:";
    private final String TOPIC_PATTERN = "[a-zA-Z0-9-_./]+";
    private final static EdgeXLogger logger = EdgeXLoggerFactory.getEdgeXLogger(EMFPublisher.class);

    /**
     * Publish data on specified port number. {@link EMFPublisher#start} API
     * should be called before publishing the message.
     *
     * @param port
     *            port for publishing message/events.
     * @param callback
     *            {@link EMFCallback}
     */
    public EMFPublisher(int port, EMFCallback callback) {
        mPort = port;
        mCallback = callback;
        mContext = EMFAPI.getInstance().getContext();
        mPubLock = new ReentrantLock(true);
    }

    // finalize method to be called by Java Garbage collector before destroying
    // this object.
    @Override
    protected void finalize() {
        logger.debug("In finalize");
        stop();
    }

    /**
     * Starts PUB instance.
     *
     * @return {@link EMFErrorCode}
     */
    public EMFErrorCode start() {
        if (null == mContext) {
            logger.error("Context is null");
            return EMFErrorCode.EMF_ERROR;
        }

        try {
            mPubLock.lock();
            if (null == mPublisher) {
                mPublisher = mContext.socket(ZMQ.PUB);
                mPublisher.bind(getSocketAddress());
            }
        } catch (Exception e) {
            logger.error("Exception while starting publisher: " + e.getMessage());
            mPublisher = null;
            return EMFErrorCode.EMF_ERROR;
        } finally {
            mPubLock.unlock();
        }
        logger.debug("Publisher started [address]: " + getSocketAddress());
        return EMFErrorCode.EMF_OK;
    }

    /**
     * Publish events on the socket for subscribers.
     *
     * @param event
     *            {@link Event}
     * @return {@link EMFErrorCode}
     */
    public EMFErrorCode publish(Event event) {
        if (null == mPublisher || null == event) {
            logger.error("Publisher or event is null");
            return EMFErrorCode.EMF_ERROR;
        }
        byte[] byteEvent = EMFEventConverter.toProtoBuf(event);
        if (null == byteEvent) {
            logger.error("ByteEvent is null");
            return EMFErrorCode.EMF_ERROR;
        }
        boolean result = false;
        try {
            mPubLock.lock();
            if (null != mPublisher) {
                result = mPublisher.send(byteEvent);
            } else {
                return EMFErrorCode.EMF_ERROR;
            }
        } catch (Exception e) {
            logger.error("Exception while publishing: " + e.getMessage());
            return EMFErrorCode.EMF_ERROR;
        } finally {
            mPubLock.unlock();
        }
        if (false == result) {
            logger.error("Published without topic failed");
            return EMFErrorCode.EMF_ERROR;
        }
        logger.debug("Published without topic");
        return EMFErrorCode.EMF_OK;
    }

    /**
     * Publish events on a specific topic on socket for subscribers.
     *
     * Note (1) Topic name should be as path format. For example:
     * home/livingroom/ (2) Topic name can have letters [a-z, A-z], numerics
     * [0-9] and special characters _ - . and / (3) Topic will be appended with
     * forward slash [/] in case, if application has not appended it.
     *
     * @param topic
     *            Topic on which event needs to be published.
     * @param event
     *            {@link Event}
     * @return {@link EMFErrorCode}
     */
    public EMFErrorCode publish(String topic, Event event) {
        if (null == mPublisher || null == event) {
            logger.error("Publisher or event is null");
            return EMFErrorCode.EMF_ERROR;
        }

        // validate topic
        String validTopic = validateTopic(topic);
        if (null == validTopic) {
            logger.error("Invalid topic: " + topic);
            return EMFErrorCode.EMF_INVALID_TOPIC;
        }

        boolean result = false;
        byte[] byteEvent = EMFEventConverter.toProtoBuf(event);
        if (null == byteEvent) {
            logger.error("byteEvent is null");
            return EMFErrorCode.EMF_ERROR;
        }
        try {
            mPubLock.lock();
            if (null != mPublisher) {
                result = mPublisher.sendMore(validTopic);
                if (false == result) {
                    logger.error("SendMore failed");
                    return EMFErrorCode.EMF_ERROR;
                }
                result = mPublisher.send(byteEvent);
            } else {
                return EMFErrorCode.EMF_ERROR;
            }
        } catch (Exception e) {
            logger.error("Exception while publishing: " + e.getMessage());
            return EMFErrorCode.EMF_ERROR;
        } finally {
            mPubLock.unlock();
        }
        logger.debug("Published on topic: " + validTopic);
        if (false == result) {
            logger.error("SendMore failed");
            return EMFErrorCode.EMF_ERROR;
        }
        return EMFErrorCode.EMF_OK;
    }

    /**
     * Publish an events on list of topics on socket for subscribers. On any of
     * the topic in list, if it failed to publish event it will return
     * EMF_ERROR/EMF_INVALID_TOPIC.
     *
     * Note: (1) Topic name should be as path format. For example:
     * home/livingroom/ (2) Topic name can have letters [a-z, A-z], numerics
     * [0-9] and special characters _ - . and / (3) Topic will be appended with
     * forward slash [/] in case, if application has not appended it.
     *
     * @param topics
     *            Topic on which event needs to be published.
     * @param event
     *            {@link Event}
     * @return {@link EMFErrorCode}
     *
     */
    public EMFErrorCode publish(List<String> topics, Event event) {
        if (null == event) {
            logger.error("Event is null");
            return EMFErrorCode.EMF_ERROR;
        }

        if (null == topics) {
            logger.error("Topic list is null");
            return EMFErrorCode.EMF_INVALID_TOPIC;
        }

        EMFErrorCode result = EMFErrorCode.EMF_OK;
        for (String topic : topics) {
            result = publish(topic, event);
            if (result != EMFErrorCode.EMF_OK) {
                return result;
            }
        }
        return result;
    }

    /**
     * Stops PUB instance.
     *
     * @return {@link EMFErrorCode}
     */
    public EMFErrorCode stop() {
        if (null == mPublisher) {
            logger.error("Publisher is null");
            return EMFErrorCode.EMF_ERROR;
        }

        EMFErrorCode result = EMFErrorCode.EMF_ERROR;
        try {
            mPubLock.lock();
            if (null != mPublisher) {
                // Sync close
                result = syncClose();
            } else {
                return result;
            }
            mPublisher = null;
        } finally {
            mPubLock.unlock();
        }
        logger.debug("Publisher stopped");
        return result;
    }

    /**
     * Get the port of the publisher.
     *
     * @return port number as integer.
     */
    public int getPort() {
        return mPort;
    }

    private String validateTopic(String topic) {
        if (null == topic || topic.isEmpty()) {
            return null;
        }

        // check whether topic contains only alphabet, digits and special
        // characters _ - . and /
        if (!topic.matches(TOPIC_PATTERN)) {
            return null;
        }

        // check whether last character is forward slash or not
        // if not append and return the string
        if (!topic.endsWith("/")) {
            topic = topic + "/";
        }
        return topic;
    }

    private String getSocketAddress() {
        return PUB_TCP_PREFIX + Integer.toString(mPort);
    }

    private static String getMonitorAddress() {
        String MONITOR_PREFIX = "inproc://monitor-";
        return MONITOR_PREFIX + UUID.randomUUID().toString();
    }

    private EMFErrorCode syncClose() {
        String address = getMonitorAddress();
        boolean result = mPublisher.monitor(address, ZMQ.EVENT_CLOSED);
        if (false == result) {
            logger.info("Error in monitor");
        }

        ZMQ.Socket socket = mContext.socket(ZMQ.PAIR);
        if (socket != null) {
            result = socket.connect(address);
            logger.info("Pair socket connection result: " + result);
        } else {
            logger.info("Pair socket creation failed");
        }

        try {
            mPublisher.close();
            logger.debug("Closed publisher socket");
        } catch (Exception e) {
            logger.error("Exception while closing socket: " + e.getMessage());
            return EMFErrorCode.EMF_ERROR;
        }

        if (null == socket || false == result) {
            return EMFErrorCode.EMF_OK;
        }

        // set receive timeout
        socket.setReceiveTimeOut(1000);
        while (true) {
            ZMQ.Event event = null;
            try {
                event = ZMQ.Event.recv(socket);
            } catch (Exception e) {
                logger.info("Exception while receiving Event");
                socket.close();
                break;
            }
            if (event == null) {
                logger.info("Event is null, timeout occured");
                socket.close();
                break;
            }

            logger.debug("Event received for socket: " + event.getEvent() + "  address: "
                    + event.getAddress());
            if (ZMQ.EVENT_CLOSED == event.getEvent()) {
                logger.debug("Received EVENT_CLOSED from socket");
                socket.close();
                break;
            }
        }
        return EMFErrorCode.EMF_OK;
    }
}
