#include "EMFAPI.h"
#include "EMFSubscriber.h"
#include "EMFErrorCodes.h"
#include "Event.pb.h"

#include <iostream>
#include <unistd.h>
#include <signal.h>

using namespace std;
using namespace emf;

EMFSubscriber *subscriber = nullptr ;

void printEvent(emf::Event event)
{
    cout<<"--------------------------------------"<<endl;
    cout<<"Device:  " << event.device()<<endl;
    cout<<"Readings: "<<endl;

    int size = event.reading_size();
    int i=0;
    while (i<size)
    {
        Reading reading = event.reading(i);
        cout<<"Key: " + reading.name()<<endl;
        cout<<"Value: " + reading.value()<<endl;
        i++;
    }
    cout<<"----------------------------------------"<<endl;
}
void subCB(emf::Event event)
{
    cout<<"App: Event received "<<endl;
    printEvent(event);
}

void subTopicCB(std::string topic, emf::Event event)
{
    cout<<"App: Event received "<<endl;
    cout<<"Topic: "<<topic<<endl;
    printEvent(event);
}

void sigint(int signal)
{
    if(nullptr!=subscriber)
    {
        cout<<"callig stop API: "<<endl;
        EMFErrorCode result = subscriber->stop();
        cout<<"stop API: [Result]: "<<result<<std::endl;
    }
    exit(0);
}

int main()
{
    std::string ip = "localhost";
    int port = 5562;
    int choice =0;
    std::string topic="";

    //this handler is added to check stop API
    signal(SIGINT, sigint);

    //Initialize EMF stack
    EMFAPI *obj = EMFAPI::getInstance();
    std::cout<<"Initialize API [result]: "<<obj->initialize()<<endl;

    cout<<"Enter 1 for General Event testing"<<endl;
    cout<<"Enter 2 for Topic Based delivery"<<endl;
    cout<<"ctrl+c to terminate the program"<<endl<<endl;
    cin>>choice;

    //Create EMF Subscriber
    switch (choice)
    {
        case 1:
            subscriber =  new EMFSubscriber(ip, port,  subCB,  subTopicCB);
            break;
        case 2:
            subscriber =  new EMFSubscriber(ip, port,  subCB,  subTopicCB);
            cout<<"Enter the topic: ";
            cin>>topic;
            cout<<"Topic is: "<<topic<<endl;
            break;
        default:
            cout<<"Invalid choice..[Re-run application]"<<endl;
            return -1;
    }

    if(nullptr ==subscriber )
    {
        cout<<"Subscriber is null "<<endl;
        return 0;
    }
    std::cout<<"Subscriber created !!"<<endl;

    //Start EMF Subscriber
    EMFErrorCode result = subscriber->start();
    cout<<"Subscriber start [Result] : "<<result<<endl;

    //subscribe for events
    if (topic.empty())
    {
        result = subscriber->subscribe();
    }
    else
    {
        result = subscriber->subscribe(topic);
    }

    if (result != EMF_OK)
    {
        cout<<"subscribe API: error occured"<<endl;
        return -1;
    }

    cout<<"Suscribed to publisher.. -- Waiting for Events --"<<endl;

    // infinite loop for receiving messages....
    while (true) { }
return 0;
}
