
#include "EMFAPI.h"
#include "EMFPublisher.h"
#include "EMFLogger.h"
#include "zmq_addon.hpp"

#include <iostream>
#include <string>
#include <regex>

#include <unistd.h>

#define PUB_TCP_PREFIX "tcp://*:"
#define TOPIC_PATTERN "[a-zA-Z0-9-_./]+"
#define TAG "EMFPublisher"

#ifdef __GNUC__
#define EMF_GCC_VERSION (__GNUC__ * 10000 + __GNUC_MINOR__ * 100 + __GNUC_PATCHLEVEL__)
#endif

namespace emf
{
    EMFPublisher::EMFPublisher(int port, EMFStartCB startCB, EMFStopCB stopCB, EMFErrorCB errorCB):
        mPort(port), mStartCallback(startCB), mStopCallback(stopCB), mErrorCallback(errorCB)
    {
        mContext = EMFAPI::getInstance()->getContext();
        if(nullptr == mContext)
        {
             EMF_LOG(ERROR, TAG, "Context is null");
        }
        mPublisher = nullptr;
        std::srand(std::time(0));
    }

    EMFPublisher::~EMFPublisher()
    {
        stop();
        if(mPublisher)
        {
           delete mPublisher;
           mPublisher = nullptr;
        }
    }

    EMFErrorCode EMFPublisher::start()
    {
        EMF_SCOPE_LOGGER(TAG, __func__);
        if(nullptr == mContext)
        {
            EMF_LOG(ERROR, TAG, "Context is null");
            return EMF_ERROR;
        }

        try
        {
            std::lock_guard<std::recursive_mutex> lock(mPubLock);
            if(nullptr == mPublisher)
            {
                mPublisher = new zmq::socket_t(*mContext, ZMQ_PUB);
                mPublisher->bind(getSocketAddress());
            }
        }
        catch (std::exception e)
        {
            EMF_LOG(ERROR, TAG, "caught exception");
            delete mPublisher;
            mPublisher = nullptr;
            return EMF_ERROR;
        }
        EMF_LOG_V(DEBUG, TAG, "Publisher started [address]: %s", getSocketAddress().c_str());
        return EMF_OK;
    }

    EMFErrorCode EMFPublisher::publish(emf::Event event)
    {
        EMF_SCOPE_LOGGER(TAG, __func__);
        if(!mPublisher)
        {
            EMF_LOG(ERROR, TAG, "publisher is null");
            return EMF_ERROR;
        }

        std::string eventStr;
        bool result  = event.SerializeToString(&eventStr);
        if (false == result)
        {
            return EMF_ERROR;
        }

        // create a zmq message from the serialized string
        zmq::message_t zmqMsg (eventStr.size());
        memcpy ((void *) zmqMsg.data (), eventStr.c_str(), eventStr.size());
        std::lock_guard<std::recursive_mutex> lock(mPubLock);
        try
        {
            if (mPublisher)
            {
                result = mPublisher->send(zmqMsg);
            }
            else
            {
                return EMF_ERROR;
            }
        }
        catch(std::exception e)
        {
            EMF_LOG_V(ERROR, TAG, "caught exception: %s", e.what());
            return EMF_ERROR;
        }
        if (false == result)
        {
            EMF_LOG(ERROR, TAG, "Published without topic failed");
            return EMF_ERROR;
        }
        EMF_LOG(DEBUG, TAG, "Published without topic");
        return EMF_OK;
    }

    EMFErrorCode EMFPublisher::publish( std::string topic, emf::Event event)
    {
        EMF_SCOPE_LOGGER(TAG, "publish [Topic]");
        if(!mPublisher)
        {
            EMF_LOG(ERROR, TAG, "publisher is null");
            return EMF_ERROR;
        }

        //Validate Topic
        topic = sanitizeTopic(topic);
        if(topic.empty())
        {
            return EMF_INVALID_TOPIC;
        }

        //prepare a multi part message with topic
        zmq::multipart_t zmqMultipart(topic);

        // create a zmq message from the serialized string
        std::string eventStr;
        bool result  = event.SerializeToString(&eventStr);
         if (false == result)
        {
            return EMF_ERROR;
        }
        zmqMultipart.add(zmq::message_t(eventStr.c_str(), eventStr.size()));

        //publish on the socket
        std::lock_guard<std::recursive_mutex> lock(mPubLock);
        try
        {
            if (mPublisher)
            {
                result = zmqMultipart.send(*mPublisher);
            }
            else
            {
                return EMF_ERROR;
            }
        }
        catch(std::exception e)
        {
            EMF_LOG_V(ERROR, TAG, "caught exception: %s", e.what());
            return EMF_ERROR;
        }

        if (false == result)
        {
            EMF_LOG_V(ERROR, TAG, "Publish failed");
            return EMF_ERROR;
        }
        return EMF_OK;
    }

    EMFErrorCode EMFPublisher::publish( std::list<std::string> topics, emf::Event event)
    {
        EMF_SCOPE_LOGGER(TAG, "publish [List Topic]");
        if(!topics.size())
        {
            return EMF_INVALID_TOPIC;
        }

        EMFErrorCode result = EMF_OK;
        for (auto topic : topics)
        {
            result = publish(topic, event);
            if (result != EMF_OK)
            {
                return result;
            }
        }
        return result;
    }

    EMFErrorCode EMFPublisher::stop()
    {
        EMF_SCOPE_LOGGER(TAG, __func__);
        if (nullptr == mPublisher)
        {
            EMF_LOG(ERROR, TAG, "publisher is null");
            return EMF_ERROR;
        }

        EMFErrorCode result = EMF_ERROR;
        std::lock_guard<std::recursive_mutex> lock(mPubLock);
        if(mPublisher)
        {
            // Sync close
            result = syncClose();
        }
        else
        {
            return result;
        }
        delete mPublisher;
        mPublisher = nullptr;
        EMF_LOG(DEBUG, TAG, "publisher stopped");
        return result;
    }

    int EMFPublisher::getPort()
    {
        return mPort;
    }

    std::string EMFPublisher::getSocketAddress()
    {
        return PUB_TCP_PREFIX + std::to_string(mPort);
    }

    std::string EMFPublisher::sanitizeTopic(std::string topic)
    {
        if(topic.empty())
        {
            return topic;
        }

    //Regex support is supported from  gcc-4.9 and higher
#if defined(EMF_GCC_VERSION) && EMF_GCC_VERSION >= 40900
    std::regex pattern(TOPIC_PATTERN);
    if(!std::regex_match(topic, pattern))
    {
        return "";
    }
#endif

        if (topic.at(topic.length()-1) != '/')
        {
            topic = topic + "/";
        }
        return topic;
    }

    std::string getMonitorAddress()
    {
        std::string MONITOR_PREFIX = "inproc://monitor-";
        std::string address = MONITOR_PREFIX + std::to_string(std::rand());
        EMF_LOG_V(DEBUG, TAG, "monitor address is: %s", address.c_str());
        return address;
    }

    EMFErrorCode EMFPublisher::syncClose()
    {
        zmq::monitor_t monitor;
        monitor.init(*mPublisher, getMonitorAddress(), ZMQ_EVENT_CLOSED);
        try
        {
            mPublisher->close();
        }
        catch(std::exception e)
        {
            EMF_LOG_V(ERROR, TAG, "caught exception while closing publisher: %s", e.what());
            return EMF_ERROR;
        }

        if(true == monitor.check_event(1000))
        {
            EMF_LOG(DEBUG, TAG, "Received ZMQ_EVENT_CLOSED Event");
        }
        else
        {
            EMF_LOG(ERROR, TAG, "No ZMQ_EVENT_CLOSED event");
        }
        return EMF_OK;
    }
}
