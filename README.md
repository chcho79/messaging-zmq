## Edge Message Framwork (EMF)

EMF is data transmission functionality based on ZeroMQ for direct messaging.

EMF library based java/cpp/go is provided. The main fearues are as followings.

1) Pub/Sub messaging with ZeroMQ
2) Topic based data streaming
3) Serialization / deserialization with protobuf

### Add Certificate your envorionments
1. get certificate file using browser

   1) Access https://nexus.edgexfoundry.org/ using browser(ex. firefox, google Chrome)
   
   2) Extract certificate file (DST Root CA X3)

2. add certificate file to jre using keytool

   1) `sudo keytool -importcert -noprompt -trustcacerts -alias ALIASNAME -file /PATH/TO/YOUR/DESKTOP/CertificateName.cer -keystore /PATH/TO/YOUR/JDK/jre/lib/security/cacerts -storepass changeit`
<br></br>

