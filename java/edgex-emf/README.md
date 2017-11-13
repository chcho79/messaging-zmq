=========== Edge-messaging framework guide ==========

--------- Build instructions ---------

Pre-requisite:
Built core-domain service

(A)Using eclipse IDE:

1. Import the EMF sdk [edgex-emf]:
   File -> Import -> Maven -> Existing Maven Projects -> Browse to "edgex-emf" -> Finish

2. Build the EMF jar:
   Right Click on the project -> Run as -> Maven install

3. After step 2, it will create a jar:
   ~/EMF/edgex-emf/target/edgex-emf-0.0.1-SNAPSHOT.jar

(B)Using Linux command line:

1. Pre-requisutes:
   Maven should be installed on linux machine. check it using : mvn -version

2. Goto: ~/EMF/edgex-emf/

3. Build command:
   $ mvn build

4. On successful build it will create a jar:
   ~/EMF/edgex-emf/target/edgex-emf-0.0.1-SNAPSHOT.jar

---------- Using EMF SDK [For micro-services] -------------
1. The micro-service whoever wants to use EMF APIs has to build core-domain service and EMF SDK.
2. Follow EMF sample app guide to use EMF SDK APIs. [~/EMF/samples]