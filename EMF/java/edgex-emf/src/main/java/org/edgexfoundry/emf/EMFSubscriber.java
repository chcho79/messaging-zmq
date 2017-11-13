package org.edgexfoundry.emf;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.emf.protobufevent.EMFEventConverter;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;

/**
 * This class represents the EMF Subscriber.
 */
public class EMFSubscriber {

    private String mServiceName;
    private EMFSubCallback mCallback;
    private String mIp;
    private int mPort;
    private Thread mThread;

    // ZMQ Subscriber socket
    private ZMQ.Socket mSubscriber;
    private ZMQ.Context mContext;

    // ZMQ poller
    private Poller mPoller;

    // ZMQ shut-down socket
    private ZMQ.Socket mShutdownServer;
    private ZMQ.Socket mShutdownClient;

    // Thread safety lock
    private ReentrantLock mSubLock;

    private final String TCP_PREFIX = "tcp://";
    private final String INPROC_PREFIX = "inproc://shutdown-";
    private final String TOPIC_PATTERN = "[a-zA-Z0-9-_./]+";
    private final static EdgeXLogger logger = EdgeXLoggerFactory
            .getEdgeXLogger(EMFSubscriber.class);

    /**
     * Subscribe to publisher at given IP and Port.
     *
     * @param ip
     *            IP address
     * @param port
     *            port number
     * @param msgType
     *            Message type of the message/event to be received from
     *            publisher. {@link EMFMessageType}
     * @param callback
     *            {@link EMFCallback}
     */
    public EMFSubscriber(String ip, int port, EMFSubCallback callback) {
        mIp = ip;
        mPort = port;
        mCallback = callback;
        mContext = EMFAPI.getInstance().getContext();
        mSubLock = new ReentrantLock(true);
    }

    /**
     * Subscribe to given serviceName.
     *
     * @param serviceName
     *            service for which subscription is required.
     * @param callback
     *            {@link EMFSubCallback}
     */
    public EMFSubscriber(String serviceName, EMFSubCallback callback) {
        mServiceName = serviceName;
        // For given serviceName get the ip and port [TODO]
        mCallback = callback;
        mContext = EMFAPI.getInstance().getContext();
        mSubLock = new ReentrantLock(true);
    }

    // finalize method to be called by Java Garbage collector before destroying
    // this object.
    @Override
    protected void finalize() {
        logger.debug("In finalize");
        stop();
    }

    /**
     * Interface to receive message callback from EMF subscriber.
     */
    public interface EMFSubCallback {
        /**
         * Invoked when message is received.
         *
         * @param event
         *            {@link Event}
         */
        public void onMessageCB(Event event);

        /**
         * Invoked when message is received for a specific topic.
         *
         * @param event
         *            {@link Event}
         */
        public void onMessageCB(String topic, Event event);
    }

    /**
     * Create filter as data consumer.
     *
     * @param filter
     *            {@link EMFFilter}
     *
     * @return {@link EMFErrorCode}
     */
    public EMFErrorCode createFilter(EMFFilter filter) {
        return EMFErrorCode.EMF_OK;
    }

    /**
     * Starts SUB instance.
     *
     * @return {@link EMFErrorCode}
     */
    public EMFErrorCode start() {
        if (null == mContext) {
            logger.error("Context is null");
            return EMFErrorCode.EMF_ERROR;
        }

        String address = getInProcUniqueAddress();
        try {
            mSubLock.lock();
            // Shutdown server sockets
            if (null == mShutdownServer) {
                mShutdownServer = mContext.socket(ZMQ.PAIR);
                mShutdownServer.bind(address);
            }

            // Shutdown client sockets
            if (null == mShutdownClient) {
                mShutdownClient = mContext.socket(ZMQ.PAIR);
                mShutdownClient.connect(address);
            }

            // Subscriber socket
            if (null == mSubscriber) {
                mSubscriber = mContext.socket(ZMQ.SUB);
                mSubscriber.connect(getSocketAddress());
            }

            // Register sockets to poller
            if (null == mPoller) {
                mPoller = mContext.poller(2);
                mPoller.register(mSubscriber);
                mPoller.register(mShutdownClient);
            }
        } catch (Exception e) {
            logger.error("Exception while starting subscriber: " + e.getMessage());
            mSubLock.unlock();
            stop();
            return EMFErrorCode.EMF_ERROR;
        }

        // Receiver thread
        if (null == mThread) {
            mThread = new Thread(new Runnable() {
                public void run() {
                    receive();
                }
            });
            mThread.start();
            logger.debug("Receiver thread started");
        }

        mSubLock.unlock();
        return EMFErrorCode.EMF_OK;
    }

    private String getSocketAddress() {
        return TCP_PREFIX + mIp + ":" + mPort;
    }

    private String getInProcUniqueAddress() {
        String address = INPROC_PREFIX + UUID.randomUUID().toString();
        return address;
    }

    /**
     * Subscribe for event/messages.
     *
     * @return {@link EMFErrorCode}
     */
    public EMFErrorCode subscribe() {
        return subscribeInternal("");
    }

    /**
     * Subscribe for event/messages on a particular topic.
     *
     * @param topic
     *            Topic to be subscribed.
     * @return {@link EMFErrorCode}
     *
     * @note (1) Topic name should be as path format. For example:
     *       home/livingroom/ (2) Topic name can have letters [a-z, A-z],
     *       numerics [0-9] and special characters _ - . and /
     */
    public EMFErrorCode subscribe(String topic) {
        // validate the topic
        String validTopic = validateTopic(topic);
        if (null == validTopic) {
            logger.error("Invalid topic: " + topic);
            return EMFErrorCode.EMF_INVALID_TOPIC;
        }
        logger.debug("Topic is: " + validTopic);
        return subscribeInternal(validTopic);
    }

    private EMFErrorCode subscribeInternal(String topic) {
        if (null == mSubscriber) {
            logger.error("Subscriber is null");
            return EMFErrorCode.EMF_ERROR;
        }

        // subscribe for messages
        try {
            mSubLock.lock();
            if (null != mSubscriber) {
                mSubscriber.subscribe(topic.getBytes());
            } else {
                return EMFErrorCode.EMF_ERROR;
            }
        } finally {
            mSubLock.unlock();
        }
        logger.debug("subscribed for events");
        return EMFErrorCode.EMF_OK;
    }

    /**
     * Subscribe for event/messages on given list of topics. On any of the topic
     * in list, if it failed to subscribe events it will return
     * EMF_ERROR/EMF_INVALID_TOPIC.
     *
     * @param topic
     *            List of topics to be subscribed.
     * @return {@link EMFErrorCode}
     *
     * @note (1) Topic name should be as path format. For example:
     *       home/livingroom/ (2) Topic name can have letters [a-z, A-z],
     *       numerics [0-9] and special characters _ - . and /
     */
    public EMFErrorCode subscribe(List<String> topics) {
        if (null == topics) {
            logger.error("Topic list is null");
            return EMFErrorCode.EMF_INVALID_TOPIC;
        }

        EMFErrorCode result = EMFErrorCode.EMF_OK;
        for (String topic : topics) {
            result = subscribe(topic);
            if (result != EMFErrorCode.EMF_OK) {
                return result;
            }
        }
        return result;
    }

    private void receive() {
        byte[] data = null;
        byte[] topicBytes = null;
        Event event = null;

        while (null != mThread && !mThread.isInterrupted()) {
            if (null == mSubscriber || null == mPoller) {
                logger.error("Subscriber or poller is null");
                return;
            }

            mPoller.poll();
            if (mPoller.pollin(0)) {
                try {
                    mSubLock.lock();
                    if (null != mSubscriber) {
                        data = mSubscriber.recv();
                        if (mSubscriber.hasReceiveMore()) {
                            topicBytes = data;
                            data = mSubscriber.recv();
                        }
                    }
                } catch (Exception e) {
                    logger.error("Exception while receiving: " + e.getMessage());
                } finally {
                    mSubLock.unlock();
                }

                logger.debug("Event received");
                event = EMFEventConverter.toEdgeXEvent(data);
                if (null != event) {
                    if (null == topicBytes) {
                        mCallback.onMessageCB(event);
                    } else {
                        String recvTopic = new String(topicBytes);
                        logger.debug("Topic: " + recvTopic);
                        mCallback.onMessageCB(recvTopic, event);
                    }
                }
            } else if (mPoller.pollin(1)) {
                logger.debug("Received shut down request");
                break;
            }

            data = null;
            topicBytes = null;
        }
    }

    /**
     * Un-subscribe for all the events from publisher.
     *
     * @return {@link EMFErrorCode}
     */
    public EMFErrorCode unSubscribe() {
        return unSubscribeInternal("");
    }

    /**
     * Un-subscribe for a specific topic events.
     *
     * @param topic
     *            topic to be unsubscribed.
     * @return {@link EMFErrorCode}
     *
     * @note (1) Topic name should be as path format. For example:
     *       home/livingroom/ (2) Topic name can have letters [a-z, A-z],
     *       numerics [0-9] and special characters _ - . and /
     */
    public EMFErrorCode unSubscribe(String topic) {
        // validate topic
        String validTopic = validateTopic(topic);
        if (null == validTopic) {
            logger.error("Invalid topic: " + topic);
            return EMFErrorCode.EMF_INVALID_TOPIC;
        }
        logger.debug("Topic is: " + validTopic);
        return unSubscribeInternal(topic);

    }

    private EMFErrorCode unSubscribeInternal(String topic) {
        if (null == mSubscriber) {
            logger.error("Subscriber is null");
            return EMFErrorCode.EMF_ERROR;
        }

        try {
            mSubLock.lock();
            if (null != mSubscriber) {
                mSubscriber.unsubscribe(topic.getBytes());
            } else {
                return EMFErrorCode.EMF_ERROR;
            }
        } finally {
            mSubLock.unlock();
        }
        logger.debug("Un-subscribed for events");
        return EMFErrorCode.EMF_OK;
    }

    /**
     * Un-subscribe for given list of topic events. On any of the topic in list,
     * if it failed to unsubscribe events it will return
     * EMF_ERROR/EMF_INVALID_TOPIC.
     *
     * @param topics
     *            List of topics to be unsubscribed.
     * @return {@link EMFErrorCode}
     *
     * @note (1) Topic name should be as path format. For example:
     *       home/livingroom/ (2) Topic name can have letters [a-z, A-z],
     *       numerics [0-9] and special characters _ - . and /
     */
    public EMFErrorCode unSubscribe(List<String> topics) {
        if (null == topics) {
            logger.error("Topic list is null");
            return EMFErrorCode.EMF_INVALID_TOPIC;
        }
        EMFErrorCode result = EMFErrorCode.EMF_OK;
        for (String topic : topics) {
            result = unSubscribe(topic);
            if (result != EMFErrorCode.EMF_OK) {
                return result;
            }
        }
        return result;
    }

    /**
     * Stop the SUB instance.
     *
     * @return {@link EMFErrorCode}
     */
    public EMFErrorCode stop() {

        try {
            mSubLock.lock();

            // Send a shutdown message to receiver thread
            if (null != mShutdownServer) {
                boolean result = mShutdownServer.send("shutdown");
                logger.debug("Shutdown send result: " + result);
            }

            // wait for receiver thread to stop
            try {
                if (null != mThread) {
                    mThread.join();
                }
            } catch (InterruptedException e) {
                logger.error("Thread join exception" + e.getMessage());
            }

            // Unregister sockets from poller
            if (null != mPoller) {
                mPoller.unregister(mSubscriber);
                mPoller.unregister(mShutdownClient);
            }

            // close shut down sockets
            if (null != mShutdownClient) {
                mShutdownClient.close();
            }
            if (null != mShutdownServer) {
                mShutdownServer.close();
            }

            // Close subscriber
            if (null != mSubscriber) {
                mSubscriber.close();
            }

            mSubscriber = null;
            mPoller = null;
            mShutdownClient = null;
            mShutdownServer = null;
            mThread = null;
        } catch (Exception e) {
            logger.error("Exception while stopping subscriber: " + e.getMessage());
            return EMFErrorCode.EMF_ERROR;
        } finally {
            mSubLock.unlock();
        }

        logger.debug("Subscriber stopped");
        return EMFErrorCode.EMF_OK;
    }

    /**
     * Get the IP address.
     *
     * @return IP address as String.
     */
    public String getIp() {
        return mIp;
    }

    /**
     * Get the port of the subscriber.
     *
     * @return port number as integer.
     */
    public int getPort() {
        return mPort;
    }

    /**
     * Get the service name.
     *
     * @return Service name.
     */
    public String getServiceName() {
        return mServiceName;
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
}
