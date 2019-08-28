#############################################################################
# Copyright 2019-20 UKRI Science and Technology Facilities Council
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License 
#############################################################################
#use apline base image, Oracle one has problem with licence
FROM openjdk:8-jdk-alpine
#
LABEL author="Shirley Crompton" \
      vendor="UK RI STFC" \
      eu.mf2c-project.version="0.0.1-beta" \
      eu.mf2c-project.version.is-production="false" 
#
# Cloud CA
ENV TRUSTCA="https://213.205.14.13:54443/certauths/rest"

##creates folders
RUN mkdir -p "/var/app"
##for credentials
RUN mkdir -p "/pkidata/cau"

##copies p12 and jks files
ADD ./credentials /pkidata/cau/
ADD ./cau-stfc/mf2c-cau.jar /var/app/cau.jar
WORKDIR /var/app
# 
EXPOSE 55443
#run the application
CMD exec java -jar cau.jar --cloudca=${TRUSTCA}

