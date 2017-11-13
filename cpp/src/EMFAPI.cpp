
#include "EMFAPI.h"
#include "EMFLogger.h"

#include <iostream>

#define TAG "EMFAPI"

namespace emf
{
    EMFAPI *EMFAPI::getInstance()
    {
        static EMFAPI instance;
        return &instance;
    }

    EMFErrorCode EMFAPI::initialize()
    {
        EMF_SCOPE_LOGGER(TAG, __func__);
        if(nullptr == mContext)
        {
            mContext = std::make_shared<zmq::context_t>(1);
        }

        if(nullptr == mContext)
        {
            EMF_LOG(ERROR, TAG, "context is null");
            return EMF_ERROR;
        }
        mStatus = EMF_Initialized;
        return EMF_OK;
    }

    EMFErrorCode EMFAPI::terminate()
    {
        EMF_SCOPE_LOGGER(TAG, __func__);
        mContext.reset();
        mStatus = EMF_Terminated;
        return EMF_OK;
    }

    EMFStatusCode EMFAPI::getStatus()
    {
        return mStatus;
    }

    std::shared_ptr<zmq::context_t>  EMFAPI::getContext()
    {
        return mContext;
    }
}
