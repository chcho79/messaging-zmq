/**
  * @file   EMFSubscriber.h
  *
  * @brief This file provides APIs for subscriber: start, subscribe, stop.
  */

#ifndef EMF_SUBSCRIBER_H
#define EMF_SUBSCRIBER_H

#include "EMFErrorCodes.h"

//Protobuf header file
#include "Event.pb.h"

//ZeroMQ header file
#include "zmq.hpp"

#include <list>
#include <thread>
#include <mutex>

namespace emf
{
    /**
    * Callbacks to get all the subscribed events.
    */
    typedef std::function<void(emf::Event event)> EMFSubCB;

    /**
    * Callbacks to get all the subscribed events for a specific topic.
    */
    typedef std::function<void(std::string topic, emf::Event event)> EMFSubTopicCB;

    /**
    * @class  EMFSubscriber
    * @brief   This class Contains the APIs related to start, stop, subscribe APIs
    *               of EMF Subscriber.
    */
    class EMFSubscriber
    {
        public:

            /**
            *  Construtor of EMFSubscriber.
            *
            * @param ip - ip to be used for subscriber socket.
            * @param port - Port to be used for subscriber socket.
            * @param subCallback- Subscriber callback to receive events.
            * @param topicCallback - Subscriber callback to receive events for a particular topic.
            */
            EMFSubscriber(std::string ip, int port, EMFSubCB subCallback, EMFSubTopicCB topicCallback);

            /**
            *  Construtor of EMFSubscriber.
            *
            * @param serviceName -Subscribe to given serviceName.
            * @param subCallback- Subscriber callback to receive events.
            * @param topicCallback - Subscriber callback to receive events for a particular topic.
            */
            EMFSubscriber(std::string serviceName, EMFSubCB subCallback, EMFSubTopicCB topicCallback);

            /**
            * Destructor of EMFPublisher.
            */
            ~EMFSubscriber();

            /**
            * Starts SUB  instance.
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            */
            EMFErrorCode start();

            /**
            *  Subscribe for event/messages.
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            */
            EMFErrorCode subscribe();

            /**
            * Subscribe for event/messages on a particular topic.
            *
            * @param topic - Topic to be subscribed.
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            *
            * @note (1) Topic name should be as path format. For example:
            *       home/livingroom/ (2) Topic name can have letters [a-z, A-z],
            *       numerics [0-9] and special characters _ - . and /
            */
            EMFErrorCode subscribe( std::string topic);

            /**
            * Subscribe for event/messages on given list of topics. On any of the topic
            * in list, if it failed to subscribe events it will return
            * EMF_ERROR/EMF_INVALID_TOPIC.
            *
            * @param topic - List of topics to be subscribed
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            *
            *  @note (1) Topic name should be as path format. For example:
            *       home/livingroom/ (2) Topic name can have letters [a-z, A-z],
            *       numerics [0-9] and special characters _ - . and /
            */
            EMFErrorCode subscribe(std::list<std::string> topics);

            /**
            * Un-subscribe all the events from publisher.
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            */
            EMFErrorCode unSubscribe() ;

            /**
            * Un-subscribe specific topic events.
            *
            * @param topic - topic to be unsubscribed.
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            *
            * @note (1) Topic name should be as path format. For example:
            *       home/livingroom/ (2) Topic name can have letters [a-z, A-z],
            *       numerics [0-9] and special characters _ - . and /
            */
            EMFErrorCode unSubscribe(std::string topic);

            /**
            * Un-subscribe event/messages on given list of topics. On any of the topic
            * in list, if it failed to unsubscribe events it will return
            * EMF_ERROR/EMF_INVALID_TOPIC.
            *
            * @param topic - List of topics to be unsubscribed
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            *
            *  @note (1) Topic name should be as path format. For example:
            *       home/livingroom/ (2) Topic name can have letters [a-z, A-z],
            *       numerics [0-9] and special characters _ - . and /
            */
            EMFErrorCode unSubscribe(std::list<std::string> topics);

            /**
            * Stops SUB instance.
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            */
            EMFErrorCode stop();

            /**
            * Get the IP address.
            *
            * @return IP address as String.
            */
            std::string getIp();

            /**
            * Get the port of the subscriber.
            *
            * @return port number as integer.
            */
            int getPort();

            /**
            * Get the service name.
            *
            * @return Service name.
            */
            std::string getServiceName();

        private:
            std::string mServiceName;
            std::string mIp;
            int mPort;

            //Receiver Thread
            std::thread mThread;
            bool isReceiverStarted;

            //EMF callbacks
            EMFSubCB mSubCallback;
            EMFSubTopicCB mSubTopicCallback;

            // ZMQ Subscriber socket
            zmq::socket_t * mSubscriber;
            std::shared_ptr<zmq::context_t> mContext;

            // ZMQ shut-down sockets
            zmq::socket_t *mShutdownServer;
            zmq::socket_t *mShutdownClient;

            // ZMQ poller
            std::vector<zmq::pollitem_t> mPollItems;

            //Mutex
            std::recursive_mutex mSubLock;

            EMFErrorCode subscribeInternal(std::string topic);
            EMFErrorCode unSubscribeInternal(std::string topic);
            std::string getSocketAddress();
            std::string getInProcUniqueAddress();
            void receive();
            std::string  sanitizeTopic(std::string topic);
    };
}
#endif //EMF_SUBSCRIBER_H
