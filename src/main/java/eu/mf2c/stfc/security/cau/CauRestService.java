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

package eu.mf2c.stfc.security.cau;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.mf2c.stfc.security.cau.exception.CauException;
import eu.mf2c.stfc.security.cau.model.Pubkey;
import eu.mf2c.stfc.security.cau.model.PubkeyRepo;
import eu.mf2c.stfc.security.cau.util.CauProperties;
import eu.mf2c.stfc.security.cau.util.Utils;

/**
 * Rest services offered by the CAU
 * <p>
 * @author Shirley Crompton
 * email shirley.crompton@stfc.ac.uk
 * org Data Science and Technology Group, UKRI Science and Technology Facilities Council
 * @created 12 Jun 2019
 */
@RestController
@RequestMapping(value = CauProperties.ROOT)
public class CauRestService {
	/** Spring boot message logger */
	private static final Logger log = LoggerFactory.getLogger(CauRestService.class);
	
	/** the Gemfire interface to the public key repo*/
	PubkeyRepo pubkeyRepo;
	
	public CauRestService(PubkeyRepo pubkeyRepo) {

        this.pubkeyRepo = pubkeyRepo;
    }
	/**
	 * Rest service to handle get public key requests.
	 * <p>
	 * @param did	A {@link java.lang.String <em>String</em>} representation of the 
	 * 				Agent&#39;s device id
	 * @return		A {@link java.lang.String <em>String</em>} representation of the RSA public key object
	 */
	@GetMapping(value = CauProperties.PUBKEY, produces = MediaType.TEXT_PLAIN_VALUE)
    public String publickey(@RequestParam(value="deviceid", defaultValue="0") String did) {
		log.debug("get publickey method called ....");
		System.out.println("get publickey method called ....");
		/*<ip:port>/cau/publickey?deviceid=http://localhost:8080/cau/publickey
		 * ?deviceid=0f848d8fb78cbe5615507ef5a198f660ac89a3ae03b95e79d4ebfb3466c20d54e9a5d9b9c41f88c782d1f67b32231d31b4fada8d2f9dd31a4d884681b784ec5a 
		 */
		//no param case is handled by Spring, it returns a 400
		if(did.equals("0")) {
			log.error("default device id received....");
			System.out.println("default device id recived....");
			return HttpStatus.BAD_REQUEST.name() + " : no device id value";
		}			
		try {
			//
			Pubkey pubkey = pubkeyRepo.findByDeviceId(did);//use the interface to findBy
			if(pubkey == null) {
				throw new CauException("Public key not found");
			}
			return pubkey.getPubkey();
			
		} catch(Exception ce) {//in prod code, catch not found exception{
			if(ce.getMessage().contains("Public key not found")) {
				return HttpStatus.NOT_FOUND.toString() + " " + did + " not found";
			}else {
				return HttpStatus.INTERNAL_SERVER_ERROR.toString() + " : " + ce.getMessage();
			}
		} 
    }
	/**
	 * REST service to handle post requests from CAU&#45;Clients to register an Agent.  The registration 
	 * process involves&#58;
	 * <ul>
	 * <li>forward the Certificate Signing Request to the Cloud CA to get a X.509 certificate back</li>
	 * <li>storing the {@link java.lang.String <em>String</em>} representation of the RSA public key
	 * 		against the Agent&#39;s unique device id.</li>
	 * <li>return a {@link java.lang.String <em>String</em>} representation of the X.509 certificate 
	 * 		to the CAU&#45;Client</li>
	 * </ul>
	 * <p>
	 * @param request 		a comma separated list of {@link java.lang.String <em>String</em>} representations of 
	 * 						the following request parameters.  Each parameter is a key&#45;value separated by &#39;&#58;&#39;&#58;
	 * 				<ul>
	 * 				<li>deviceID &#58; the Agent&#39;s unique device id</li>
	 * 				<li>detectedLID &#58; the detected leader#39;s device id</li>
	 * 				<li>csr &#58; a {@link java.lang.String <em>String</em>} representation of the Certificate Signing Request</li>
	 * 				</ul>
	 * @return	a {@link java.lang.String <em>String</em>} representation of the X.509 Certificate
	 */
	@PostMapping(value = CauProperties.CERT, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	//@PostMapping(value = Properties.CERT)
	public String cert(@RequestBody String request) {
		log.debug("post cert method called ....");
		System.out.println("post cert method called ....");
		String pem = "should have been a cert string";
		try {
			/*
			 * 1. parse the request from CAU-client, 
			 * request msg params : csr=,deviceID=,detectedLID=
			 * 2. post csr to CA
			 * 3. save device id and public key to repo
			 * 4. return cert to CAU-client
			 */
			log.debug("request:\n" + request);
			System.out.println("request\n" + request);
			CertificateHandler ch = new CertificateHandler(Utils.getValues(request)); //step 1
			//if missing params, ch throws 'missing input params!' exception
			pem = ch.handle(pubkeyRepo); //steps 2 and 3		
			//
		} catch (CauException e) {
			log.error("Error handling post message: " + e.getMessage());
			return HttpStatus.INTERNAL_SERVER_ERROR.toString() + " : " + e.getMessage();
		} catch (IllegalArgumentException e) {
			log.error("Error handling post message: " + e.getMessage());
			return HttpStatus.BAD_REQUEST.toString() + " : " + e.getMessage();
		} 		
		return pem; //step 4
	}	
}
