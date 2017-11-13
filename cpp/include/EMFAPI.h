/**
  * @file   EMFAPI.h
  *
  * @brief This file provides APIs for EMF: initialize, terminate.
  */

#ifndef EMF_API_H
#define EMF_API_H

#include "EMFErrorCodes.h"

//ZeroMQ header file
#include "zmq.hpp"

#include <memory>

namespace emf
{
    /**
    * @class  EMFAPI
    * @brief   This class Contains the APIs related to initialization, termination
    *               of EMF stack.
    */
    class EMFAPI
    {
        public:
            /**
            * Get instance of EMFAPI Class.
            *
            * @return Instance of EMFAPI.
            */
            static EMFAPI *getInstance();

            /**
            * Initialize required EMF components.
            * This API should be called first, before using any EMF APIs.
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            */
            EMFErrorCode initialize();

            /**
            * Perform cleanup of EMF components.
            *
            * @return EMFErrorCode - EMF_OK on success, otherwise appropriate error code.
            */
            EMFErrorCode terminate();

            /**
            * Get status of EMF service.
            *
            * @return EMFStatusCode - Current status of EMF Service.
            */
            EMFStatusCode getStatus();

            // For EMF internal use
            std::shared_ptr<zmq::context_t>  getContext();

        private:
            EMFAPI() { mStatus = EMF_Constructed; }
            EMFStatusCode mStatus;
            std::shared_ptr<zmq::context_t> mContext;
    };
}
#endif  //EMF_API_H