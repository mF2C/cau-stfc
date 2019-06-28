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

package eu.mf2c.stfc.security.cau.model;

//import java.io.IOException;
import java.io.Serializable;
//import java.nio.file.Files;
//import java.nio.file.Paths;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.gemfire.mapping.annotation.Region;

/**
 * Data model for working with the Spring
 * Gemfire Agent RSA public key repository
 * <p>
 * @author Shirley Crompton
 * email shirley.crompton@stfc.ac.uk
 * org Data Science and Technology Group, UKRI Science and Technology Facilities Council
 * @created 17 Jun 2019
 */
@Region(value="Pubkeys")
public class Pubkey implements Serializable {
	
	/**
	 * Default serial id
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Key
	 */
	@Id
	private final String deviceId;
	/**
	 * Value
	 */
	private String pubkey;
	/**
	 * Constructor for the model
	 * @param deviceId	An Agent&#39;s device id
	 * @param pubkey	The RSA public key associated with an Agent&#39;s mF2C X.509 certificate.
	 */
	@PersistenceConstructor
    public Pubkey(String deviceId, String pubkey) {
        this.deviceId = deviceId;
        this.pubkey = pubkey;
    }
	/**
	 * Getter
	 * @return the pubkey
	 */
	public String getPubkey() {
		return pubkey;
	}
	/**
	 * Getter
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}
	/*
	public String getPublicKey(String did) throws Exception {
		//caller guard for null & empty device id
		//use a stub for the moment
		String publickey = new String(Files.readAllBytes(Paths.get("/pkidata/cauTest/public.key")));
		if(publickey == null || publickey.isEmpty()) {
			throw new CauException("Public key not found for " + did);
		}else {
			System.out.println("device id : " + did + "has public Key: " + publickey);
			return publickey;
		}
		
	}*/
	/**
	 * Return a plain text representation of the object
	 */
	@Override
    public String toString() {
        return "Pubkey :\ndeviceId=" + deviceId + "\npubkey=" + pubkey;
    }

}
