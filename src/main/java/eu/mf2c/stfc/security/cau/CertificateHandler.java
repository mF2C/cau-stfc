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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mf2c.stfc.security.cau.exception.CauException;
import eu.mf2c.stfc.security.cau.model.Pubkey;
import eu.mf2c.stfc.security.cau.model.PubkeyRepo;
import eu.mf2c.stfc.security.cau.restclient.CAClient;
import eu.mf2c.stfc.security.cau.util.CredentialUtils;

/**
 * Handler for the authentication and certification process.
 * <p>
 * @author Shirley Crompton
 * email shirley.crompton@stfc.ac.uk
 * org Data Science and Technology Group, UKRI Science and Technology Facilities Council
 * @created 18 Jun 2019
 */
public class CertificateHandler {
	/** the Gemfire interface to the public key repo
	@Autowired
	PubkeyRepo pubkeyRepo;*/
	/** Spring boot message logger */
	private static final Logger log = LoggerFactory.getLogger(CertificateHandler.class);
	/** user idKey attribute */
	private String idKey;
	/** agent device id attribute */
	private String deviceID;
	/** leader agent device id attribute */
	private String leaderID;
	/** leader agent IP attribute 
	private String leaderIP;*/
	/** agent type: full or micro 
	private String agentTyp;*/
	/** certificate request string attribute */
	private String csr;
	
	/*
	 * 1. parse the request from CAU-client, check agent type: full v micro; child v leader
	 * request msg params : csr=,deviceID=,detectedLID=,detectedLIP=,IDKey=,type=
	 * 2. call cloud cimi/dc to validate idKey (for full agent)
	 * 3. post csr to CA
	 * 4. (19Jun19 not required!)register cimi user for the agent in the Leader CIMI instance (cau-client 
	 * 	  registers the leader in local CIMI (for full agent)
	 * 5. save device id and public key to repo
	 * 6. return cert to CAU-client
	 */		
	/**
	 * Construct an instance
	 * <p>
	 * @param params	a {@link java.util.Map <em>Map</em>} of the input parameters
	 * @throws IllegalArgumentException if any requisite parameters are missing
	 */
	public CertificateHandler(Map<String,String> params) throws IllegalArgumentException {
		//	csr=,deviceID=,detectedLID=,detectedLIP=,IDKey=,type=
		this.idKey = params.get("IDKey");
		this.deviceID = params.get("deviceID");
		this.leaderID = params.get("detectedLID");
		//this.leaderIP = params.get("detectedLIP"); //actually not critical if no CIMI user matching
		this.csr = params.get("csr");
		//this.agentTyp = params.get("type");
		//26June19 given the latest change, we really only need idKey, deviceId and csr for minimum operation
		if(this.idKey == null || this.deviceID == null || this.leaderID == null /*
				|| this.leaderIP == null || this.agentTyp == null*/ || this.csr == null ) {
			throw new IllegalArgumentException("missing input params!");
		}
	}
	/**
	 * Validate the idKey against the cloud CIMI instance
	 * <p>
	 * @return true if valiated, else false
	 * @throws CAUException 	on error
	 */
	public boolean validate() throws CauException {
		//Step2
		//:TODO not sure what this method is yet as CIMI hasn't got the value yet
		boolean res = false;
		try {
			
			log.info("CIMI method not yet available.... this step does nothing...");
			res = true;
			
		}catch(Exception e) {
			throw new CauException("Validate IDKey(" + this.idKey + ") error : " + e.getMessage());
		}
		
		return res;
	}
	/**
	 * Post CSR to CA to request an X.509 certificate.
	 * <p>
	 * @return a {@link java.lang.String <em>String</em>} representation of the certificate
	 * @throws CauException	on error
	 */
	public String getCertificate() throws CauException {
		CAClient client = new CAClient(this.csr);
		return client.httpsPost();
	}	
	
	/**
	 * Proceed with the steps to authenticate and get an mF2C certificate
	 * for the Agent.
	 * <p>
	 * @param	the gemfire DAO to the repository
	 * @return	a {@link java.lang.String <em>String</em>} representation 
	 * 				of the X.509 certificate issued to the Agent
	 * @throws CauException  on error
	 */
	public String handle(PubkeyRepo pubkeyRepo) throws CauException {
		//go ahead and deal with the request 
		/*
		if(this.agentTyp.equals("full")) {
			validate();
		}else {
			log.info("agent(" + this.deviceID + ") is a micro agent, skipping validate idKey step!");
		}*/
		//according the Cris, we validate both full and micro agent's idKey against the cloud CIMI
		validate(); //step 2
		//
		String pem = getCertificate(); //step 3
		/* 19June19 SixQ has not implemented user matching in their code during session creation,
		 * so no need to register user
		if(this.agentTyp.equals("full")) {
			registerAgent();
		}else {
			log.info("agent(" + this.deviceID + ") is a micro agent, skipping register agent step!");
		}*/
		try {
			
			String pkPEM = CredentialUtils.getPubKeyString(pem);
			//persist new record
			Pubkey newPK = new Pubkey(this.deviceID, pkPEM);
			pubkeyRepo.save(newPK);		 //step 5
			//			
		}catch(Exception e) {
			throw new CauException(this.deviceID + ": Error saving public key : " + e.getMessage());
		}		
		//
		return pem;		
	}
}
