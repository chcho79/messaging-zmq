package org.edgexfoundry.emf;

import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import org.zeromq.ZMQ;

/**
 * Contains EMF APIs related to initialization, termination of EMF stack.
 */
public class EMFAPI {

    private static EMFAPI mInstance;
    public EMFStatusCode status = EMFStatusCode.EMF_Unknown;
    private ZMQ.Context mContext;

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }
    private final static EdgeXLogger logger = EdgeXLoggerFactory.getEdgeXLogger(EMFAPI.class);

    private EMFAPI() {
        status = EMFStatusCode.EMF_Constructed;
    }

    /**
     * Get instance of EMFAPI Class.
     *
     * @return Instance of EMFAPI.
     */
    public static synchronized EMFAPI getInstance() {
        if (null == mInstance) {
            mInstance = new EMFAPI();
        }
        return mInstance;
    }

    /**
     * Initialize required EMF components. This API should be called first,
     * before using any EMF APIs.
     *
     * @return {@link EMFErrorCode}
     */
    public EMFErrorCode initialize() {
        mContext = ZMQ.context(1);
        status = EMFStatusCode.EMF_Initialized;
        logger.debug("EMF initialized");
        return EMFErrorCode.EMF_OK;
    }

    /**
     * Perform cleanup of EMF components.
     *
     * @return {@link EMFErrorCode}
     */
    public EMFErrorCode terminate() {
        if (null != mContext) {
            mContext.term();
            mContext = null;
            status = EMFStatusCode.EMF_Terminated;
            logger.debug("EMF terminated");
        }
        return EMFErrorCode.EMF_OK;
    }

    /**
     * Return status of EMF service.
     *
     * @return {@link EMFStatusCode}
     */
    public EMFStatusCode getStatus() {
        return status;
    }

    // For EMF internal use
    public ZMQ.Context getContext() {
        return mContext;
    }
}
