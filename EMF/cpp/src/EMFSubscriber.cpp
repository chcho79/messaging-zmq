
#include "EMFAPI.h"
#include "EMFSubscriber.h"
#include "EMFLogger.h"

#include <iostream>
#include <string>
#include <sstream>
#include <regex>

#define TCP_PREFIX "tcp://"
#define INPROC_PREFIX "inproc://shutdown-"
#define TOPIC_PATTERN "[a-zA-Z0-9-_./]+"
#define TAG "EMFSubscriber"

#ifdef __GNUC__
#define EMF_GCC_VERSION (__GNUC__ * 10000 + __GNUC_MINOR__ * 100 + __GNUC_PATCHLEVEL__)
#endif

namespace emf
{
    EMFSubscriber::EMFSubscriber(std::string ip, int port, EMFSubCB subCallback, EMFSubTopicCB topicCallback):
        mIp(ip), mPort(port), mSubCallback(subCallback), mSubTopicCallback(topicCallback)
    {
        mContext = EMFAPI::getInstance()->getContext();
        if(nullptr == mContext)
        {
            EMF_LOG(ERROR, TAG, "Context is null");
        }
        mShutdownServer = nullptr;
        mShutdownClient = nullptr;
        mSubscriber = nullptr;
        isReceiverStarted = false;
        std::srand(std::time(0));
    }

    EMFSubscriber::EMFSubscriber(std::string serviceName, EMFSubCB subCallback, EMFSubTopicCB topicCallback):
        mServiceName(serviceName), mSubCallback(subCallback), mSubTopicCallback(topicCallback)
    {
        //TOOD
    }

    EMFSubscriber::~EMFSubscriber()
    {
        stop();
    }

    void EMFSubscriber::receive()
    {
        zmq::message_t zMsg;
        zmq::message_t zTopic;
        emf::Event event;
        std::string topic;
        bool more = false;
        while(isReceiverStarted)
        {
            if (nullptr == mSubscriber || mPollItems.empty())
            {
                EMF_LOG(ERROR, TAG, "subscriber or poller is null");
                return;
            }

            zmq::poll(mPollItems);
            if (mPollItems[1].revents & ZMQ_POLLIN)
            {
                std::lock_guard<std::recursive_mutex> lock(mSubLock);
                if(mSubscriber)
                {
                    try
                    {
                        mSubscriber->recv(&zMsg);
                        more = false;
                        if(zMsg.more())
                        {
                            more = true;
                            std::string topicStr(static_cast<char*>(zMsg.data()), zMsg.size());
                            topic = topicStr;
                            mSubscriber->recv(&zMsg);
                        }
                    }
                    catch (std::exception e)
                    {
                        EMF_LOG_V(ERROR, TAG, "caught exception: %s", e.what());
                        isReceiverStarted = false;
                        break;
                    }
                }

                //convert data to emf event
                std::string msgStr(static_cast<char*>(zMsg.data()), zMsg.size());
                event.ParseFromString(msgStr);

                //call  application callback
                if(more)
                {
                    mSubTopicCallback(topic, event);
                }
                else
                {
                    mSubCallback(event);
                }
            }
            else if(mPollItems[0].revents & ZMQ_POLLIN)
            {
                EMF_LOG(DEBUG, TAG, "Received the shut down request");
                break;
            }
        }
    }

    EMFErrorCode EMFSubscriber::start()
    {
        EMF_SCOPE_LOGGER(TAG, __func__);
        if (nullptr == mContext)
        {
            EMF_LOG(ERROR, TAG, "Context is null");
            return EMF_ERROR;
        }
        try
        {
            std::lock_guard<std::recursive_mutex> lock(mSubLock);
            std::string address = getInProcUniqueAddress();
            // Shutdown server sockets
            if (!mShutdownServer)
            {
                mShutdownServer =  new zmq::socket_t(*mContext, ZMQ_PAIR);
                mShutdownServer->bind(address);
            }

            // Shutdown client sockets
            if (!mShutdownClient)
            {
                mShutdownClient = new zmq::socket_t(*mContext, ZMQ_PAIR);
                mShutdownClient->connect(address);
                zmq_pollitem_t shutDownPoller;
                shutDownPoller.socket = *mShutdownClient;
                shutDownPoller.events = ZMQ_POLLIN;
                mPollItems.push_back(shutDownPoller);
            }

            // Subscriber socket
            if(!mSubscriber)
            {
                mSubscriber = new zmq::socket_t(*mContext, ZMQ_SUB);
                mSubscriber->connect(getSocketAddress());
                EMF_LOG_V(DEBUG, TAG, "Starting subscriber [Address]: %s", getSocketAddress().c_str());

                 // Register sockets to poller
                zmq_pollitem_t subscriberPoller;
                subscriberPoller.socket = *mSubscriber;
                subscriberPoller.events = ZMQ_POLLIN;
                mPollItems.push_back(subscriberPoller);
            }
        }
        catch (std::exception e)
        {
            EMF_LOG_V(ERROR, TAG, "caught exception: %s", e.what());
            stop();
            return EMF_ERROR;
        }

        std::lock_guard<std::recursive_mutex> lock(mSubLock);
        //receiver Thread
        if(!isReceiverStarted)
        {
            isReceiverStarted = true;
            mThread = std::thread(&EMFSubscriber::receive, this);
        }
        return EMF_OK;
    }

    EMFErrorCode EMFSubscriber::subscribe()
    {
        EMF_SCOPE_LOGGER(TAG, __func__);
        return subscribeInternal("");
    }

    EMFErrorCode EMFSubscriber::subscribe(std::string topic)
    {
        EMF_SCOPE_LOGGER(TAG, "subscribe [Topic]");
        //Validate Topic
        topic = sanitizeTopic(topic);
        if(topic.empty())
        {
            return EMF_INVALID_TOPIC;
        }
        EMF_LOG_V(DEBUG, TAG, "Topic: %s", topic.c_str());
        return subscribeInternal(topic);
    }

    EMFErrorCode EMFSubscriber::subscribeInternal(std::string topic)
    {
        EMF_SCOPE_LOGGER(TAG, __func__);
        if(!mSubscriber)
        {
            EMF_LOG(ERROR, TAG, "subscriber is null");
            return EMF_ERROR;
        }

        std::lock_guard<std::recursive_mutex> lock(mSubLock);
        try
        {
            if(mSubscriber)
            {
                mSubscriber->setsockopt(ZMQ_SUBSCRIBE, topic.c_str(), topic.size());
            }
            else
            {
                return EMF_ERROR;
            }
        }
        catch (std::exception e)
        {
            EMF_LOG_V(ERROR, TAG, "caught exception: %s", e.what());
            return EMF_ERROR;
        }
        EMF_LOG(DEBUG, TAG, "subscribed for events");
        return EMF_OK;
    }

    EMFErrorCode EMFSubscriber::subscribe(std::list<std::string> topics)
    {
        EMF_SCOPE_LOGGER(TAG, "subscribe [Topic List]");
        if(!topics.size())
        {
            EMF_LOG(ERROR, TAG, "Topic list is empty");
            return EMF_INVALID_TOPIC;
        }

        EMFErrorCode result = EMF_OK;
        for (auto topic : topics)
        {
            result = subscribe(topic);
            if (result != EMF_OK)
            {
                return result;
            }
        }
        return result;
    }

    EMFErrorCode EMFSubscriber::unSubscribe()
    {
        EMF_SCOPE_LOGGER(TAG, __func__);
        return unSubscribeInternal("");
    }

    EMFErrorCode EMFSubscriber::unSubscribe(std::string topic)
    {
        EMF_SCOPE_LOGGER(TAG, "unSubscribe [Topic]");
        //Validate Topic
        topic = sanitizeTopic(topic);
        if(topic.empty())
        {
            return EMF_INVALID_TOPIC;
        }
        EMF_LOG_V(DEBUG, TAG, "Topic: %s", topic.c_str());
        return unSubscribeInternal(topic);
    }

    EMFErrorCode EMFSubscriber::unSubscribeInternal(std::string topic)
    {
        EMF_SCOPE_LOGGER(TAG, __func__);
        if(!mSubscriber)
        {
            EMF_LOG(ERROR, TAG, "subscriber is null");
            return EMF_ERROR;
        }

        std::lock_guard<std::recursive_mutex> lock(mSubLock);
        try
        {
            if(mSubscriber)
            {
                mSubscriber->setsockopt(ZMQ_UNSUBSCRIBE ,  topic.c_str(), topic.size());
            }
            else
            {
                return EMF_ERROR;
            }
        }
        catch (std::exception e)
        {
            EMF_LOG_V(ERROR, TAG, "caught exception: %s", e.what());
            return EMF_ERROR;
        }
        return EMF_OK;
    }

    EMFErrorCode EMFSubscriber::unSubscribe(std::list<std::string> topics)
    {
        EMF_SCOPE_LOGGER(TAG, "unSubscribe [Topic list]");
        if(!topics.size())
        {
            return EMF_INVALID_TOPIC;
        }

        EMFErrorCode result = EMF_OK;
        for (auto topic : topics)
        {
            result = unSubscribe(topic);
            if (result != EMF_OK)
            {
                return result;
            }
        }
        return result;
    }

    EMFErrorCode EMFSubscriber::stop()
    {
        EMF_SCOPE_LOGGER(TAG, __func__);
        std::lock_guard<std::recursive_mutex> lock(mSubLock);
        try
        {
            // Send a shutdown message to receiver thread
            if (mShutdownServer)
            {
                std::string msg = "shutdown";
                zmq::message_t zMsg (msg.size());
                memcpy ((void *) zMsg.data (), msg.c_str(), msg.size());
                bool result =  mShutdownServer->send(zMsg);
                UNUSED(result);
                EMF_LOG_V(DEBUG, TAG, "Shut down request sent[Result]: %d", result);
            }

            //wait for shutdown msg to be sent to receiver thread
            mThread.join();

            // close shut down client socket
            if (mShutdownClient)
            {
                mShutdownClient->close();
                delete mShutdownClient;
                mShutdownClient = nullptr;
            }

            // close shut down server socket
            if (mShutdownServer)
            {
                mShutdownServer->close();
                delete mShutdownServer;
                mShutdownServer = nullptr;
            }

            // close subscriber socket
            if (mSubscriber)
            {
                mSubscriber->close();
                delete mSubscriber;
                mSubscriber = nullptr;
            }
        }
        catch(std::exception e)
        {
            EMF_LOG_V(ERROR, TAG, "caught exception: %s", e.what());
        }

        //clear the poll item vector
        mPollItems.clear();

        //Reset receiver flag
        isReceiverStarted = false;

        EMF_LOG(DEBUG, TAG, "Subscriber stopped");
        return EMF_OK;
    }

        std::string EMFSubscriber::getIp()
        {
            return mIp;
        }

        int EMFSubscriber::getPort()
        {
            return mPort;
        }

        std::string  EMFSubscriber::getServiceName()
        {
            return mServiceName;
        }

    std::string EMFSubscriber::getSocketAddress()
    {
        return TCP_PREFIX + mIp + ":"  + std::to_string(mPort);
    }

    std::string EMFSubscriber::getInProcUniqueAddress()
    {
        std::string address = INPROC_PREFIX + std::to_string(std::rand());
        EMF_LOG_V(DEBUG, TAG, "The in proc address is:: %s", address.c_str());
        return address;
    }

    std::string EMFSubscriber::sanitizeTopic(std::string topic)
    {
        if(topic.empty())
        {
            return "";
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
}
