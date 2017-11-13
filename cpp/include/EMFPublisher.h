/**
  * @file   EMFPublisher.h
  *
  * @brief This file provides APIs for publisher: start, subscribe, stop.
  */

#ifndef EMF_PUBLISHER_H
#define EMF_PUBLISHER_H

#include "EMFErrorCodes.h"

//Protobuf header file
#include "Event.pb.h"

//ZeroMQ header file
#include "zmq.hpp"

#include <list>
#include <mutex>

namespace emf
{
    /**
    * Callbacks to get error codes for start/stop of EMF publisher.
    * Note: As of now not being used.
    */
    typedef std::function<void(EMFErrorCode code)> EMFStartCB;
    typedef std::function<void(EMFErrorCode code)> EMFStopCB;
    typedef std::function<void(EMFErrorCode code)> EMFErrorCB;

    /**
    * @class  EMFPublisher
    * @brief   This class Contains the APIs related to start, stop, publish APIs
    *               of EMF Publisher.
    */
    class EMFPublisher
    {
        public:

            /**
            *  Construtor of EMFPublisher.
            *
            * @param port - Port to be used for publisher socket.
            * @param startCB- Start callback.
            * @param stopCB - Stop Callback.
            * @param errorCB - Error Callback.
            */
            EMFPublisher(int port, EMFStartCB startCB, EMFStopCB stopCB, EMFErrorCB errorCB);

            /**
            * Destructor of EMFPublisher.
            */
            ~EMFPublisher();

            /**
            * Starts PUB instance.
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            */
            EMFErrorCode start();

            /**
            * Publish events on the socket for subscribers.
            *
            * @param event - event to be published.
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            */
            EMFErrorCode publish(emf::Event event);

            /**
            * Publish events on a specific topic on socket for subscribers.
            *
            * @param topic - Topic on which event needs to be published.
            * @param event - event to be published.
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            *
            * @note (1) Topic name should be as path format. For example:
            *       home/livingroom/ (2) Topic name can have letters [a-z, A-z],
            *       numerics [0-9] and special characters _ - . and /
            */
            EMFErrorCode publish(std::string topic, emf::Event event);

            /**
            * Publish an events on list of topics on socket for subscribers. On any of
            * the topic in list, if it failed to publish event it will return
            * EMF_ERROR/EMF_INVALID_TOPIC.
            *
            * @param topic - List of Topics on which event needs to be published.
            * @param event - event to be published.
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            *
            *  @note (1) Topic name should be as path format. For example:
            *       home/livingroom/ (2) Topic name can have letters [a-z, A-z],
            *       numerics [0-9] and special characters _ - . and /
            */
            EMFErrorCode publish( std::list<std::string> topics, emf::Event event);

            /**
            * Stops PUB instance.
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            */
            EMFErrorCode stop();

            /**
            * Get the port of the publisher.
            *
            * @return port number as integer.
            */
            int getPort();

        private:
            int mPort;

            //callbacks
            EMFStartCB mStartCallback;
            EMFStopCB mStopCallback;
            EMFErrorCB mErrorCallback;

            //ZMQ socket
            zmq::socket_t *mPublisher;
            std::shared_ptr<zmq::context_t> mContext;

            //Mutex
            std::recursive_mutex mPubLock;

            std::string getSocketAddress();
            std::string  sanitizeTopic(std::string topic);
            EMFErrorCode syncClose();
    };
}
#endif //EMF_PUBLISHER_H