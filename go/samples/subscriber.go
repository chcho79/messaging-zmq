package main

import (
	emf "go/emf"

	"fmt"
	"os"
)

func printEvent(event emf.Event) {
	fmt.Printf("\n--------------------------------------")
	fmt.Printf("\nDevice: %s", event.GetDevice())
	fmt.Printf("\nReadings:")

	var readings []*emf.Reading = event.GetReading()
	for i := 0; i < len(readings); i++ {
		fmt.Printf("\nKey: %s", readings[i].GetName())
		fmt.Printf("\nValue: %s", readings[i].GetValue())
	}
	fmt.Printf("\n--------------------------------------\n")
}

func main() {
	var ip string = "localhost"
	var port int = 5562
	var result emf.EMFErrorCode
	var subscriber *emf.EMFSubscriber

	//callbacks
	subCB := func(event emf.Event) { printEvent(event) }
	subTopicCB := func(topic string, event emf.Event) {
		fmt.Printf("\nTopic: %s", topic)
		printEvent(event)
	}

	//get singleton instance
	var instance *emf.EMFAPI = emf.GetInstance()

	//Initilize the EMF SDK
	result = instance.Initialize()
	fmt.Printf("\n[Initialize] Error code is: %d", result)

	//User choice
	var choice int
	var topic string
	fmt.Printf("\nEnter 1 for General Event testing")
	fmt.Printf("\nEnter 2 for Topic Based delivery\n")
	fmt.Scanf("%d", &choice)

	switch choice {
	case 1:
		subscriber = emf.GetEMFSubscriber(ip, port, subCB, subTopicCB)
	case 2:
		subscriber = emf.GetEMFSubscriber(ip, port, subCB, subTopicCB)
		fmt.Printf("\nEnter the topic: ")
		fmt.Scanf("%s", &topic)
		fmt.Printf("Topic is: %s\n", topic)
	default:
		fmt.Printf("\nInvalid choice..[Re-run application]\n")
		os.Exit(-1)
	}

	//start subscriber
	result = subscriber.Start()
	if result != 0 {
		fmt.Printf("Error while starting subscriber")
	}
	fmt.Printf("\n[Start] Error code is: %d\n", result)

	if topic == "" {
		result = subscriber.Subscribe()
	} else {
		result = subscriber.SubscribeForTopic(topic)
	}

	if result != 0 {
		fmt.Printf("Error while Subscribing")
	}
	fmt.Printf("\nSuscribed to publisher.. -- Waiting for Events --\n")

	// infinite loop for receiving messages....
	for {
	}
}
