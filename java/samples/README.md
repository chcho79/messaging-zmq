============= Edge-messaging framework Samples guide ==================

Pre-requisites:
:- Built EMF SDK "edgex-emf" [~/EMF/edgex-emf]

---- Build & Run instructions for EMF Client sample----

(A)Using eclipse IDE:

1. Import the EMF Client sample [edgex-emf-client]:
   File -> Import -> Maven -> Existing Maven Projects -> Browse to "edgex-emf-client" -> Finish

2. Build sample:
   Right Click on the project -> Run as -> Maven install

3. Run sample:
   (i) Navigate to org.edgexfoundry.emf.client
   (ii)Right click on App.java -> Run As -> Java application

4. On successful run, it will show the following options on console:

    EMF initialized
    Enter 1 for General Event testing
    Enter 2 for Topic Based delivery

    Follow the instructions on the screen.

(B)Using Linux command line:

1. Pre-requisutes:
   Maven should be installed on linux machine. check it using : mvn -version

2. Go to ~/EMF/samples/edgex-emf-client

3. Build commands:
   $ mvn clean
   $ mvn compile
   $ mvn package
   $ java -cp target/edgex-emf-subscriber-sample.jar org.edgexfoundry.emf.client.App

4. On successful run, it will show the following options on console:

    EMF initialized
    Enter 1 for General Event testing
    Enter 2 for Topic Based delivery

    Follow the instructions on the screen.

---- Build & Run instructions for EMF Server sample-----

(A)Using eclipse IDE:

1. Import the EMF Server sample [edgex-emf-server]:
   File -> Import -> Maven -> Existing Maven Projects -> Browse to "edgex-emf-server" -> Finish

2. Build sample:
   Right Click on the project -> Run as -> Maven install

3. Run sample:
   (i) Navigate to org.edgexfoundry.emf.server
   (ii)Right click on App.java -> Run As -> Java application

4. On successful run, it will show the following options on console:

    EMF initialized
    Enter 1 for General Event testing
    Enter 2 for Topic Based delivery

    Follow the instructions on the screen.

(B)Using Linux command line:

1. Pre-requisutes:
   Maven should be installed on linux machine. check it using : mvn -version

2. Go to ~/EMF/samples/edgex-emf-server

3. Build commands:
   $ mvn clean
   $ mvn compile
   $ mvn package
   $ java -cp target/edgex-emf-publisher-sample.jar org.edgexfoundry.emf.server.App

4. On successful run, it will show the following options on console:

    EMF initialized
    Enter 1 for [EdgeX meessage]
    Enter 2 for [ProtoBuf meessage]
    Enter 3 for [EdgeX meessage: topic based]
    Enter 4 for [ProtoBuf meessage: topic based]

    EMF initialized
    Enter 1 for General Event testing
    Enter 2 for Topic Based delivery

    Follow the instructions on the screen.

Note: Logs and snapshot version is subject to change.
