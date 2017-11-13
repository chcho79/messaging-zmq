// EMF package which provides simplified APIs for Publisher and Subscriber.
package emf

import (
	zmq "github.com/pebbe/zmq4"
)

// Structure represents EMFAPI
type EMFAPI struct {
	context *zmq.Context
	status  EMFStatusCode
}

var instance *EMFAPI

// Get EMFAPI instance.
func GetInstance() *EMFAPI {
	if nil == instance {
		instance = &EMFAPI{}
		instance.status = EMF_Constructed
		InitLogger()
	}
	return instance
}

// Initialize required EMF components. This API should be called first,
// before using any EMF APIs.
func (emfInstance *EMFAPI) Initialize() EMFErrorCode {
	if nil == emfInstance.context {
		var err error
		emfInstance.context, err = zmq.NewContext()
		if err != nil {
			logger.Error("EMF initialization failed")
			return EMF_ERROR
		}
		zmq.SetIoThreads(1)
	}
	logger.Debug("EMF initialized")

	emfInstance.status = EMF_Initialized
	return EMF_OK
}

// Perform cleanup of EMF components.
func (emfInstance *EMFAPI) Terminate() EMFErrorCode {
	if emfInstance.context != nil {
		err := emfInstance.context.Term()
		if nil != err {
			logger.Error("EMF termination failed")
			return EMF_ERROR
		}
		emfInstance.context = nil
		logger.Debug("EMF terminated")
	}
	emfInstance.status = EMF_Terminated
	return EMF_OK
}

func (emfInstance *EMFAPI) GetStatus() EMFStatusCode {
	return emfInstance.status
}

func (emfInstance *EMFAPI) getContext() *zmq.Context {
	return emfInstance.context
}
