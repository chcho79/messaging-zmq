
#include "UnitTestHelper.h"
#include "EMFAPI.h"

#include <iostream>

using namespace emf;

class EMFAPITest: public TestWithMock
{
protected:
    void SetUp()
    {
        TestWithMock::SetUp();
    }

    void TearDown()
    {
        TestWithMock::TearDown();
    }
};

TEST_F(EMFAPITest, getEMFInstance)
{
    EMFAPI *obj = EMFAPI::getInstance();
    EXPECT_EQ(EMF_Constructed, obj->getStatus());
    ASSERT_NE(nullptr, obj);
}

TEST_F(EMFAPITest, initialize)
{
    EMFAPI *obj = EMFAPI::getInstance();
    EXPECT_EQ(EMF_OK, obj->initialize());
}

TEST_F(EMFAPITest, terminate)
{
    EMFAPI *obj = EMFAPI::getInstance();
    EXPECT_EQ(EMF_OK, obj->initialize());
    EXPECT_EQ(EMF_OK, obj->terminate());
}

TEST_F(EMFAPITest, getStatus)
{
    EMFAPI *obj = EMFAPI::getInstance();
    obj->initialize();
    EXPECT_EQ(EMF_Initialized, obj->getStatus());
    obj->terminate();
    EXPECT_EQ(EMF_Terminated, obj->getStatus());
}

