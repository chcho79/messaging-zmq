
#include "UnitTestHelper.h"
#include "EMFAPI.h"
#include "EMFLogger.h"
#include "EMFSubscriber.h"

#include <iostream>

#define TAG "EMF_PUB_TEST"

using namespace emf;

void subCB(emf::Event event)
{
    EMF_LOG(DEBUG, TAG, "Event received");
}

void subTopicCB(std::string topic, emf::Event event)
{
    EMF_LOG(DEBUG, TAG, "Event received");
    EMF_LOG_V(DEBUG, TAG, "Topic: %s", topic.c_str());
}

class EMFSubscriberTest: public TestWithMock
{
protected:
    void SetUp()
    {
        mTopic = "topic";
        mIp = "localhost";
        mPort = 5562;
        apiInstance = EMFAPI::getInstance();
        ASSERT_NE(nullptr, apiInstance);
        EXPECT_EQ(EMF_OK, apiInstance->initialize());
        mSubscriber = new EMFSubscriber(mIp, mPort,  subCB,  subTopicCB);
        TestWithMock::SetUp();
    }

    void TearDown()
    {
        mSubscriber->stop();
        apiInstance->terminate();
        TestWithMock::TearDown();
    }

    EMFAPI *apiInstance;
    EMFSubscriber *mSubscriber;
    std::string  mTopic;
    std::string mIp;
    int mPort;
};

TEST_F(EMFSubscriberTest, start)
{
   EXPECT_EQ(EMF_OK, mSubscriber->start());
}

TEST_F(EMFSubscriberTest, startstop)
{
    for( int i =1; i<=10; i++)
    {
        EXPECT_EQ(EMF_OK, mSubscriber->start());
        EXPECT_EQ(EMF_OK, mSubscriber->stop());
    }
}

TEST_F(EMFSubscriberTest, subscribe)
{
    EXPECT_EQ(EMF_OK, mSubscriber->start());
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe());
}

TEST_F(EMFSubscriberTest, subscribeTopic)
{
    EXPECT_EQ(EMF_OK, mSubscriber->start());
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(mTopic));
}

TEST_F(EMFSubscriberTest, subscribeTopicList)
{
    EXPECT_EQ(EMF_OK, mSubscriber->start());
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(mTopic));

    std::list<std::string> topicList;
    EXPECT_EQ(EMF_INVALID_TOPIC, mSubscriber->subscribe(topicList));

    topicList.push_back("topic1");
    topicList.push_back("");
    EXPECT_EQ(EMF_INVALID_TOPIC, mSubscriber->subscribe(topicList));

    topicList.clear();
    topicList.push_back("topic1");
    topicList.push_back("topic2");
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(topicList));
}

TEST_F(EMFSubscriberTest, subscribeTopics)
{
    EXPECT_EQ(EMF_OK, mSubscriber->start());
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(mTopic));

      // Empty topic test
    std::string testingTopic = "";
    EXPECT_EQ(EMF_INVALID_TOPIC, mSubscriber->subscribe(testingTopic));

    // Numeric test
    testingTopic = "123";
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(testingTopic));

    // Alpha-Numeric test
    testingTopic = "1a2b3";
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(testingTopic));

    // Alphabet forward slash test
    testingTopic = "topic/";
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(testingTopic));

    // Alphabet-Numeric, forward slash test
    testingTopic = "topic/13/4jtjos/";
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(testingTopic));

    // Alphabet-Numeric, forward slash test
    testingTopic = "123a/1this3/4jtjos";
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(testingTopic));

    // Topic contain forward slash at last
    testingTopic = "topic/122/livingroom/";
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(testingTopic));

    // Topic contain -
    testingTopic = "topic/122/livingroom/-";
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(testingTopic));

    // Topic contain _
    testingTopic = "topic/122/livingroom_";
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(testingTopic));

    // Topic contain .
    testingTopic = "topic/122.livingroom.";
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(testingTopic));
}

TEST_F(EMFSubscriberTest, unSubscribe)
{
    EXPECT_EQ(EMF_OK, mSubscriber->start());
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe());
    EXPECT_EQ(EMF_OK, mSubscriber->unSubscribe());
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(mTopic));
    EXPECT_EQ(EMF_OK, mSubscriber->unSubscribe(mTopic));
}


TEST_F(EMFSubscriberTest, unSubscribeTopicList)
{
    EXPECT_EQ(EMF_OK, mSubscriber->start());

    std::list<std::string> topicList;
    topicList.push_back("topic1");
    topicList.push_back("");
    EXPECT_EQ(EMF_INVALID_TOPIC, mSubscriber->subscribe(topicList));
    EXPECT_EQ(EMF_INVALID_TOPIC, mSubscriber->unSubscribe(topicList));

    topicList.clear();
    topicList.push_back("topic1");
    topicList.push_back("topic2");
    EXPECT_EQ(EMF_OK, mSubscriber->subscribe(topicList));
    EXPECT_EQ(EMF_OK, mSubscriber->unSubscribe(topicList));
}

TEST_F(EMFSubscriberTest, getIp)
{
    EXPECT_EQ(mIp, mSubscriber->getIp());
}

TEST_F(EMFSubscriberTest, getPort)
{
    EXPECT_EQ(mPort, mSubscriber->getPort());
}