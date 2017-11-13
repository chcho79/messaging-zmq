/**
 * @file   EMFErrorCodes.h
 *
 * @brief   This file contains error codes of EMF stack possibly return to application.
 */
#ifndef EMF_ERROR_CODES_H_
#define EMF_ERROR_CODES_H_

namespace emf
{
    /**
    * @enum EMFErrorCode
    * EMF stack error codes.
    */
    typedef enum
    {
        EMF_OK = 0,
        EMF_ERROR,
        EMF_INVALID_TOPIC
    } EMFErrorCode;

    /**
    * @enum EMFStatusCode
    * EMF Service status codes.
    */
    typedef enum
    {
        EMF_Unknown = 0,
        EMF_Constructed,
        EMF_Initialized,
        EMF_Terminated
    } EMFStatusCode;
}
#endif  // EMF_ERROR_CODES_H_
