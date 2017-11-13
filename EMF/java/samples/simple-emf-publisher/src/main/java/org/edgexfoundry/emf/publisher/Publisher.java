package org.edgexfoundry.emf.publisher;

import java.util.List;

import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.emf.EMFAPI;
import org.edgexfoundry.emf.EMFCallback;
import org.edgexfoundry.emf.EMFErrorCode;
import org.edgexfoundry.emf.EMFPublisher;
import org.edgexfoundry.emf.EMFStatusCode;

/**
 * This class is singleton style wrapper of EMF Publish function.
 */
public class Publisher {
	private static Publisher singleton;
	private static EMFAPI apiInstance;
	private static EMFPublisher pubInstance;
	private static EMFErrorCode result = EMFErrorCode.EMF_ERROR;
	private static EMFStatusCode status = EMFStatusCode.EMF_Unknown;

	private Publisher() {
		apiInstance = EMFAPI.getInstance();
		if (apiInstance == null) {
			throw new NullPointerException("apiInstance is null");
		}
		apiInstance.initialize();
		status = apiInstance.getStatus();
		if (status != EMFStatusCode.EMF_Initialized) {
			apiInstance.initialize();
		}
	}

	/**
	 * Get Publisher class instance
	 *
	 * @return {@link Publisher}
	 */
	public static Publisher getInstance() {
		if (singleton == null) {
			singleton = new Publisher();
		}
		return singleton;
	}

	/**
	 * Initialize EMF stack for publish
	 *
	 * @param port
	 *            port for publish
	 * @param cb
	 *            {@link EMFCallback}
	 *
	 * @return {@link EMFErrorCode}
	 *
	 * @throws {@link NullPointerException}
	 */
	public EMFErrorCode startPublisher(int port, EMFCallback cb) {
		if (cb == null) {
			throw new NullPointerException("Callback is null");
		}

		pubInstance = new EMFPublisher(port, cb);
		if (pubInstance == null) {
			throw new NullPointerException("pubInstance is null");
		}
		result = pubInstance.start();

		if (result != EMFErrorCode.EMF_OK) {
			pubInstance = null;
			System.out.println("Could not start EMF...");
		}
		return result;
	}

	/**
	 * Stop EMF stack
	 *
	 * @return {@link EMFErrorCode}
	 */
	public EMFErrorCode stopPublisher() {
		if (pubInstance != null) {
			result = pubInstance.stop();
			if (result != EMFErrorCode.EMF_OK) {
				return result;
			} else {
				pubInstance = null;
				return EMFErrorCode.EMF_OK;
			}
		}

		return EMFErrorCode.EMF_OK;
	}

	/**
	 * Initialize EMF stack for publish
	 *
	 * @param event
	 *            EdgeX Event class instance for publish
	 *
	 * @return {@link EMFErrorCode}
	 *
	 * @throws {@link NullPointerException}
	 */
	public EMFErrorCode publishEvent(Event event) {
		EMFErrorCode ret = EMFErrorCode.EMF_ERROR;
		if (event == null) {
			throw new NullPointerException("Delivered argument is null");
		} else {
			ret = pubInstance.publish(event);
		}

		return ret;
	}

	/**
	 * Initialize EMF stack for publish
	 *
	 * @param topic
	 *            Topic on which event needs to be published
	 *
	 * @param event
	 *            EdgeX Event class instance for publish
	 *
	 * @return {@link EMFErrorCode}
	 *
	 * @throws {@link NullPointerException}
	 */
	public EMFErrorCode publishEvent(String topic, Event event) {
		EMFErrorCode ret = EMFErrorCode.EMF_ERROR;
		if (event == null || topic == null) {
			throw new NullPointerException("Delivered arguments are null");
		} else {
			ret = pubInstance.publish(topic, event);
		}

		return ret;
	}

	/**
	 * Initialize EMF stack for publish
	 *
	 * @param topics
	 *            Topics on which event needs to be published
	 *
	 * @param event
	 *            EdgeX Event class instance for publish
	 *
	 * @return {@link EMFErrorCode}
	 *
	 * @throws {@link NullPointerException}
	 */
	public EMFErrorCode publishEvent(List<String> topics, Event event) {
		EMFErrorCode ret = EMFErrorCode.EMF_ERROR;
		if (event == null || topics == null) {
			throw new NullPointerException("Delivered arguments are null");
		} else {
			ret = pubInstance.publish(topics, event);
		}

		return ret;
	}

}
