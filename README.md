# Control Area Unit (CAU)

## Description

This application provides a loosely coupled identity management service to mF2C fog clusters.  An Agent is assigned 
a unique 128 char length device id by the Cloud Registration service.  This id uniquely identifies each mF2C agent.
The CAU stores this device id alongside the Agent's RSA public key to help an Agent attest its mF2C identity 
via signed JSON Web Tokens, and to protect mF2C communication through non-repudiation and the encryption of confidential 
messages (see the AC-Lib for more information).  Each Agent has a built-in [CAU-client](https://github.com/mF2C/cau-client.git) 
which acts as a gateway to the loosely-coupled CAU middleware.

The CAU application uses the Spring boot REST framework which offers two key functions:

*get public key* - this retrieves the RSA public key mapped to the an Agent's device id.  

*get Agent cert* - this forms part of the Discovery, Authentication and Categorisation workflow that
bootstrap an Agent.  During the process, the CAU-client sends a Certificate Signing Request (CSR)
alongside additional metadata including the device id of the new Agent and the detected Leader Agent.
The CAU forwards the CSR to the mF2C Cloud Certification Authority to request an mF2C X.509 
Agent Certificate.  This certificate together with the associated RSA public and private key pair 
represent an Agent's digital credentials which are trusted by other components in the mF2C Public 
Key Infrastructure.  On receipt of the certificate from the CA, CAU saves the Agent's public key and 
device id as a key-value record to the local Spring Data Gemfire cache to facilitate identity management.
(:TODO the Spring Data Gemfire currently persists the records to disk locally.  In a future version, 
a distributed data cluster based on Apache Goede will be configured to enable sharing of data between CAU nodes.) 

##The CAU REST service API:

`get Public key` - https://{host:port}/cau/publickey
`get Agent cert` - https://{host:port}/cau/cert

A *cloud* CAU is available at "https://213.205.14.13:55443/cau/" for demonstration purposes.

##Example usage with curl:

We are using the -k switch to prevent hostname verification.

`curl -vk --get https://213.205.14.13:55443/cau/publickey?deviceid=3b95e79d4ebfb3466c20d54e5615507ef5a198f660ac89a3ae03b95e79d4ebfb3466c20d54e9a5d9b9c41f88c782d1f67b32231d31b4fada8d2f9dd31a4d8846 -H "Content-Type: text/plain" -H "Accept: text/plain"`

`curl -X POST -vk --data-binary @post.txt https://213.205.14.13:55443/cau/cert -H "Content-Type: text/plain" -H "Accept: text/plain"`

Here is an example of the post request data to the CAU:

`csr:{CSRString},deviceID:{deviceIDString},detectedLID:{detectedLIDString}`

 

##Building the Java library

The library is packaged with a self-contained fat jar with all depended libraries.  The jar is located in the target folder and the javadoc in the target\site\apidocs folder.
You can use Maven to build a fat jar with all dependencies using the Maven goal:

		package

It is recommended that you select the skip test option as the tests may not run correctly in your own environment. 


## Building and Running the Docker container

This project provides a self-contained fat jar with all depended libraries, including an embedded Tomcat server.
The installation shell script (cau-startup.sh) runs on the container startup stage.  The script performs these steps:

 - gets the certificates of the trusted and untrusted (or fog) CAs to use as trust anchors
 - gets a certificate from the trusted CA to use as the CAU server certificate
 - generates a p12 keystore for the embedded Tomcat server using the server certificate obtained in the previous step
 - generates a trust store in JKS format for SSL communication.  The trust store contains the trust anchors obtained above
 - generates a key store in JKS format for SSL communication.  The key store contains the private key and trust anchor
   associated with the server p12 certificate
 - launch the CAU java application
 
## Running as A Standalone Application

You can also run the library from the command line (see example below), the two arguments are mandatory.  You need to provide the 
relevant credentials (keystores, certificate and key files) manually.

	java -jar mf2c-cau.jar <cloudca=https://213.205.14.13:54443/certauths/rest/> <caservice=it2untrustedca>

## CHANGELOG

### 1.1 (28/08/2019)

#### Added

 - shell script to obtain and install server certificate, key- and trust-stores.

#### Changed

 - Removed the IDKey parameter from the get Agent cert operation.  IDKey validation is now handled by the Identification block.
 
### 1.2 (30/08/2019)

#### Changed

 - Fixed a bug in the startup shell script, aligned code to work with the application arguments and updated README.md and Dockerfile.

## Contributors

**Contributors to this repository agree to release their code under
the Apache 2.0 license.**

## License

Copyright by various contributors.  See individual source files for
copyright information.  

DISTRIBUTED under the [Apache License, Version 2.0 (January
2004)](http://www.apache.org/licenses/LICENSE-2.0).