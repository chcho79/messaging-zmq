package emf

import (
	proto "github.com/golang/protobuf/proto"
	zmq "github.com/pebbe/zmq4"
	"go.uber.org/zap"

	List "container/list"
	"strconv"
)

// Address prefix to bind subscriber.
const SUB_TCP_PREFIX = "tcp://"

// Callback to get all the subscribed events.
type EMFSubCB func(event Event)

// Callback to get all the subscribed events for a specific topic.
type EMFSubTopicCB func(topic string, event Event)

// Structure represents EMFSubscriber.
type EMFSubscriber struct {
	ip               string
	port             int
	subCallback      EMFSubCB
	subTopicCallback EMFSubTopicCB

	subscriber *zmq.Socket
	context    *zmq.Context

	isReceiverStarted bool
}

// Contructs EMFSubscriber.
func GetEMFSubscriber(ip string, port int, subCallback EMFSubCB, subTopicCallback EMFSubTopicCB) *EMFSubscriber {
	var instance *EMFSubscriber
	instance = &EMFSubscriber{}
	instance.ip = ip
	instance.port = port
	instance.subCallback = subCallback
	instance.subTopicCallback = subTopicCallback
	instance.context = GetInstance().getContext()
	InitLogger()
	if nil == instance.context {
		logger.Error("Context is null")
	}
	instance.subscriber = nil
	instance.isReceiverStarted = false
	return instance
}

func receive(subInstance *EMFSubscriber) {
	var data []byte
	var event Event
	var err error
	var more bool
	var topic string

	for subInstance.isReceiverStarted {
		if nil == subInstance.subscriber {
			logger.Error("subscriber or poller is null")
			break
		}
		data, err = subInstance.subscriber.RecvBytes(0)
		if err != nil {
			break
		}
		more, err = subInstance.subscriber.GetRcvmore()
		if err != nil {
			break
		}
		if more {
			topic = string(data[:])
			data, err = subInstance.subscriber.RecvBytes(0)
		}

		//change byte array to Event
		err := proto.Unmarshal(data, &event)
		if nil != err {
			logger.Error("Error in unmarshalling data")
		}

		if more {
			subInstance.subTopicCallback(topic, event)
		} else {
			subInstance.subCallback(event)
		}
	}
	logger.Debug("Received the shut down request")
}

// Starts SUB instance.
func (subInstance *EMFSubscriber) Start() EMFErrorCode {
	if nil == subInstance.context {
		logger.Error("Context is null")
		return EMF_ERROR
	}

	if nil == subInstance.subscriber {
		var err error
		subInstance.subscriber, err = zmq.NewSocket(zmq.SUB)
		if nil != err {
			logger.Error("Subscriber Socket creation failed")
			return EMF_ERROR
		}
		var address string = getSubSocketAddress(subInstance.ip, subInstance.port)
		err = subInstance.subscriber.Connect(address)
		if nil != err {
			logger.Error("Subscriber Socket connect failed")
			return EMF_ERROR
		}
		logger.Debug("Starting subscriber", zap.String("Address", address))
	}

	//call a go routine [new thread] for receiver
	if false == subInstance.isReceiverStarted {
		subInstance.isReceiverStarted = true
		go receive(subInstance)
	}
	return EMF_OK
}

func (subInstance *EMFSubscriber) subscribeInternal(topic string) EMFErrorCode {
	if nil != subInstance.subscriber {
		err := subInstance.subscriber.SetSubscribe(topic)
		if nil != err {
			logger.Error("subscribeInternal error occured")
			return EMF_ERROR
		}
	} else {
		logger.Error("subscriber is null")
		return EMF_ERROR
	}
	logger.Debug("subscribed for events")
	return EMF_OK
}

// Subscribe for event/messages.
func (subInstance *EMFSubscriber) Subscribe() EMFErrorCode {
	return subInstance.subscribeInternal("")
}

// Subscribe for event/messages on a particular topic.
func (subInstance *EMFSubscriber) SubscribeForTopic(topic string) EMFErrorCode {
	//validate the topic
	validTopic := sanitizeTopic(topic)
	if validTopic == "" {
		return EMF_INVALID_TOPIC
	}
	logger.Debug("subscribing for events", zap.String("Topic", validTopic))
	return subInstance.subscribeInternal(validTopic)
}

// Subscribe for event/messages on given list of topics. On any of the topic
// in list, if it failed to subscribe events it will return
// EMF_ERROR/EMF_INVALID_TOPIC.
//
// Note:
// (1) Topic name should be as path format. For example:home/livingroom/
// (2) Topic name can have letters [a-z, A-z], numerics [0-9] and special characters _ - / and .
func (subInstance *EMFSubscriber) SubscribeForTopicList(topicList List.List) EMFErrorCode {
	if topicList.Len() == 0 {
		return EMF_INVALID_TOPIC
	}
	for topic := topicList.Front(); topic != nil; topic = topic.Next() {
		result := subInstance.SubscribeForTopic(topic.Value.(string))
		if result != EMF_OK {
			return result
		}
	}
	return EMF_OK
}

func (subInstance *EMFSubscriber) unSubscribeInternal(topic string) EMFErrorCode {
	if nil != subInstance.subscriber {
		err := subInstance.subscriber.SetUnsubscribe(topic)
		if nil != err {
			logger.Error("subscriber is null")
			return EMF_ERROR
		}
	} else {
		return EMF_ERROR
	}
	return EMF_OK
}

// Un-subscribe all the events from publisher.
func (subInstance *EMFSubscriber) UnSubscribe() EMFErrorCode {
	return subInstance.unSubscribeInternal("")
}

// Un-subscribe specific topic events.
//
// Note:
// (1) Topic name should be as path format. For example:home/livingroom/
// (2) Topic name can have letters [a-z, A-z], numerics [0-9] and special characters _ - / and .
func (subInstance *EMFSubscriber) UnSubscribeForTopic(topic string) EMFErrorCode {
	//validate the topic
	validTopic := sanitizeTopic(topic)
	if validTopic == "" {
		return EMF_INVALID_TOPIC
	}
	logger.Debug("Unsubscribe for events", zap.String("Topic", validTopic))
	return subInstance.unSubscribeInternal(validTopic)
}

// Un-subscribe event/messages on given list of topics. On any of the topic
// in list, if it failed to unsubscribe events it will return
// EMF_ERROR/EMF_INVALID_TOPIC.
//
// Note:
// (1) Topic name should be as path format. For example:home/livingroom/ .
// (2) Topic name can have letters [a-z, A-z], numerics [0-9] and special characters _ - / and .
func (subInstance *EMFSubscriber) UnSubscribeForTopicList(topicList List.List) EMFErrorCode {
	if topicList.Len() == 0 {
		return EMF_INVALID_TOPIC
	}
	for topic := topicList.Front(); topic != nil; topic = topic.Next() {
		result := subInstance.UnSubscribeForTopic(topic.Value.(string))
		if result != EMF_OK {
			return result
		}
	}
	return EMF_OK
}

// Stops SUB instance.
func (subInstance *EMFSubscriber) Stop() EMFErrorCode {
	if nil != subInstance.subscriber {
		err := subInstance.subscriber.Close()
		if nil != err {
			logger.Error("Error while stopping subscriber")
			return EMF_ERROR
		}
	}
	subInstance.subscriber = nil
	subInstance.isReceiverStarted = false
	logger.Debug("Subscriber stopped")
	return EMF_OK
}

// Get Ip of publisher to which subscribed.
func (subInstance *EMFSubscriber) GetIP() string {
	return subInstance.ip
}

// Get Port of publisher to which subscribed.
func (subInstance *EMFSubscriber) GetPort() int {
	return subInstance.port
}

func getSubSocketAddress(ip string, port int) string {
	return string(SUB_TCP_PREFIX) + ip + ":" + strconv.Itoa(port)
}
