package org.edgexfoundry.emf.subscriber;

import org.edgexfoundry.emf.EMFAPI;
import org.edgexfoundry.emf.EMFErrorCode;
import org.edgexfoundry.emf.EMFStatusCode;
import org.edgexfoundry.emf.EMFSubscriber;
import org.edgexfoundry.emf.EMFSubscriber.EMFSubCallback;

/**
 * This class is singleton style wrapper of EMF Subscribe function.
 */
public class Subscriber {
	private static Subscriber singleton;
	private static EMFAPI apiInstance;
	private static EMFSubscriber subInstance;
	private static EMFErrorCode result = EMFErrorCode.EMF_ERROR;
	private static EMFStatusCode status = EMFStatusCode.EMF_Unknown;

	private Subscriber() {
		apiInstance = EMFAPI.getInstance();
		if (apiInstance == null) {
			throw new NullPointerException("apiInstance is null");
		}
		status = apiInstance.getStatus();
		if (status != EMFStatusCode.EMF_Initialized) {
			apiInstance.initialize();
		}
	}

	/**
	 * Get Subscriber class instance
	 *
	 * @return {@link Subscriber}
	 */
	public static Subscriber getInstance() {
		if (singleton == null) {
			singleton = new Subscriber();
		}
		return singleton;
	}

	/**
	 * Initialize EMF stack and subscribe with host:port and topic
	 *
	 * @param host
	 *            host for subscribe
	 * @param port
	 *            port for subscribe
	 * @param @Nullable
	 *            topic topic for subscribe, it can be null
	 * @param cb
	 *            {@link EMFSubCallback}
	 * @return {@link EMFErrorCode}
	 *
	 * @throws {@link NullPointerException}
	 */
	public EMFErrorCode startSubscriber(String host, int port, String topic, EMFSubCallback cb) {
		if (host == null || cb == null) {
			throw new NullPointerException("Callback is null");
		}

		result = stopSubscriber();
		if (result != EMFErrorCode.EMF_OK) {
			return result;
		}

		subInstance = new EMFSubscriber(host, port, cb);
		if (subInstance == null) {
			throw new NullPointerException("subInstance is null");
		}
		result = subInstance.start();
		if (result != EMFErrorCode.EMF_OK) {
			return result;
		}

		if (topic == null) {
			result = subInstance.subscribe();
		} else {
			result = subInstance.subscribe(topic);
		}

		return result;
	}

	/**
	 * Initialize EMF stack and subscribe with host:port and topic
	 *
	 * @param serviceName
	 *            serviceName for subscribe
	 * @param @Nullable
	 *            topic topic for subscribe, it can be null
	 * @param cb
	 *            {@link EMFSubCallback}
	 * @return {@link EMFErrorCode}
	 *
	 * @throws {@link NullPointerException}
	 */
	public EMFErrorCode startSubscriber(String serviceName, String topic, EMFSubCallback cb) {
		if (serviceName == null || cb == null) {
			throw new NullPointerException("Callback is null");
		}

		result = stopSubscriber();
		if (result != EMFErrorCode.EMF_OK) {
			return result;
		}

		subInstance = new EMFSubscriber(serviceName, cb);
		if (subInstance == null) {
			throw new NullPointerException("subInstance is null");
		}
		result = subInstance.start();
		if (result != EMFErrorCode.EMF_OK) {
			return result;
		}

		if (topic == null) {
			result = subInstance.subscribe();
		} else {
			result = subInstance.subscribe(topic);
		}

		return result;
	}

	/**
	 * Stop EMF stack
	 *
	 * @return {@link EMFErrorCode}
	 */
	public EMFErrorCode stopSubscriber() {
		if (subInstance != null) {
			result = subInstance.stop();
			if (result != EMFErrorCode.EMF_OK) {
				System.out.println("already stopped");
				return result;
			} else {
				subInstance = null;
				return EMFErrorCode.EMF_OK;
			}
		}
		return EMFErrorCode.EMF_OK;
	}
}
