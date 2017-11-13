
#include "UnitTestHelper.h"
#include "EMFAPI.h"
#include "EMFLogger.h"
#include "EMFPublisher.h"

#include <iostream>

#define TAG "EMF_PUB_TEST"
static int mPort = 5562;
using namespace emf;

void startCB(EMFErrorCode code)
{
    EMF_LOG(DEBUG, TAG, "Start callback");
}

void stopCB(EMFErrorCode code)
{
    EMF_LOG(DEBUG, TAG, "stop callback");
}

void errorCB(EMFErrorCode code)
{
    EMF_LOG(DEBUG, TAG, "error callback");
}

class EMFPublisherTest: public TestWithMock
{
    protected:
        void SetUp()
        {
            mTopic = "topic";
            mPort = mPort +1;
            apiInstance = EMFAPI::getInstance();
            ASSERT_NE(nullptr, apiInstance);
            EXPECT_EQ(EMF_OK, apiInstance->initialize());
            mPublisher = new EMFPublisher(mPort, startCB, stopCB, errorCB);
            TestWithMock::SetUp();
        }

        void TearDown()
        {
            mPublisher->stop();
            apiInstance->terminate();
            TestWithMock::TearDown();
        }

        EMFAPI *apiInstance;
        EMFPublisher *mPublisher;
        std::string  mTopic;
};

TEST_F(EMFPublisherTest, constructor)
{
    EMFPublisher *instance = new EMFPublisher(mPort, startCB, stopCB, errorCB);
    ASSERT_NE(nullptr, instance);
}

TEST_F(EMFPublisherTest, startstop)
{
    for( int i =1; i<=10; i++)
    {
        EXPECT_EQ(EMF_OK, mPublisher->start());
        EXPECT_EQ(EMF_OK, mPublisher->stop());
    }
}

TEST_F(EMFPublisherTest, start)
{
    EXPECT_EQ(EMF_OK, mPublisher->start());
}

TEST_F(EMFPublisherTest, publish)
{
    emf::Event event = getProtoBufEvent();
    EXPECT_EQ(EMF_OK, mPublisher->start());
    EXPECT_EQ(EMF_OK, mPublisher->publish(event));
}


TEST_F(EMFPublisherTest, publishOnTopic)
{
    emf::Event event = getProtoBufEvent();
    EXPECT_EQ(EMF_OK, mPublisher->start());
    EXPECT_EQ(EMF_OK, mPublisher->publish(mTopic, event));
}

TEST_F(EMFPublisherTest, publishOnTopic1)
{
    EXPECT_EQ(EMF_OK, mPublisher->start());
    emf::Event event = getProtoBufEvent();

    // Empty topic test
    std::string testingTopic = "";
    EXPECT_EQ(EMF_INVALID_TOPIC, mPublisher->publish(testingTopic, event));

    // Numeric test
    testingTopic = "123";
    EXPECT_EQ(EMF_OK, mPublisher->publish(testingTopic, event));

    // Alpha-Numeric test
    testingTopic = "1a2b3";
    EXPECT_EQ(EMF_OK, mPublisher->publish(testingTopic, event));

    // Alphabet forward slash test
    testingTopic = "topic/";
    EXPECT_EQ(EMF_OK, mPublisher->publish(testingTopic, event));

    // Alphabet-Numeric, forward slash test
    testingTopic = "topic/13/4jtjos/";
    EXPECT_EQ(EMF_OK, mPublisher->publish(testingTopic, event));

    // Alphabet-Numeric, forward slash test
    testingTopic = "123a/1this3/4jtjos";
    EXPECT_EQ(EMF_OK, mPublisher->publish(testingTopic, event));

    // Topic contain forward slash at last
    testingTopic = "topic/122/livingroom/";
    EXPECT_EQ(EMF_OK, mPublisher->publish(testingTopic, event));

    // Topic contain -
    testingTopic = "topic/122/livingroom/-";
    EXPECT_EQ(EMF_OK, mPublisher->publish(testingTopic, event));

    // Topic contain _
    testingTopic = "topic/122/livingroom_";
    EXPECT_EQ(EMF_OK, mPublisher->publish(testingTopic, event));

    // Topic contain .
    testingTopic = "topic/122.livingroom.";
    EXPECT_EQ(EMF_OK, mPublisher->publish(testingTopic, event));
}

TEST_F(EMFPublisherTest, publishNegative)
{
    EXPECT_EQ(EMF_OK, mPublisher->start());
    emf::Event event = getProtoBufEvent();
    EXPECT_EQ(EMF_INVALID_TOPIC, mPublisher->publish("", event));

    std::list<std::string> topicList;
    EXPECT_EQ(EMF_INVALID_TOPIC, mPublisher->publish(topicList, event));

    topicList.push_back("topic1");
    topicList.push_back("");
    EXPECT_EQ(EMF_INVALID_TOPIC, mPublisher->publish(topicList, event));

    topicList.clear();
    topicList.push_back("topic1");
    topicList.push_back("topic2");
    EXPECT_EQ(EMF_OK, mPublisher->publish(topicList, event));
}

TEST_F(EMFPublisherTest, getPort)
{
    EXPECT_EQ(mPort, mPublisher->getPort());
}