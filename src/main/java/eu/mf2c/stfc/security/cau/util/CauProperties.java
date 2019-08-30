/**
 Copyright 2018-20 UKRI Science and Technology Facilities Council

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License 
 */

package eu.mf2c.stfc.security.cau.util;

/**
 * Configuration Properties
 * <p> 
 * @author Shirley Crompton
 * email shirley.crompton@stfc.ac.uk
 * org Data Science and Technology Group, UKRI Science and Technology Council
 * @Created 12 June 2018
 */
public class CauProperties {
	
	///////////////default locations of other mF2C components///////
	/** IP of the Cloud CAU */
	public static String cloudCAUIP = ""; //built-in and controlled by distro??
	/** IP of the Cloud CIMI 
	public static String cloudCIMI = "https://dashboard.mf2c-project.eu/api/";*/
	/** IP of the Cloud CA */
	public static String cloudCA = "https://213.205.14.13:54443/certauths/rest/";	
	
	//////////////default CA resource path/////////////////////////
	/** default CA service path element */
	public static String CA = "it2untrustedca";		
	
	//////////////CIMI resource path///////////////////////
	/** base entry point *
	public static String CIMIAPI = "/api";	
	/** resource for registering a user 
	public static final String USER = "/user";
	/** resource for querying an IdKey
	public static final String IDKEY = "/idkey"; //:TODO need to swap it for the correct one!!! */
	
	//////////////cau rest service endpoints //////////////
	/** root context of the application */
	public static final String ROOT = "/cau";
	/** public key resource element */
	public static final String PUBKEY = "/publickey";
	/** cert resource element */
	public static final String CERT = "/cert";
	
	/////////////cau trust and keystore file path //////////
	/** file name of the persisted trustStore */
	public static final String CACERT_PATH = "/pkidata/cau/clientTrust.jks"/*"/var/lib/cau/clientTrust.jks"*/;
	/** file name of the persisted keyStore */
	public static final String STORE_PATH = "/pkidata/cau/cau-server.jks"/*"/var/lib/cau/server.p12"*/;
	//public static final String STORE_PATH = "/pkidata/cauTest/keystore/server.p12";
	
	
	

}
