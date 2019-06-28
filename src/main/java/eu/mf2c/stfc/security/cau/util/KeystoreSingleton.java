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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mf2c.stfc.security.cau.exception.CauException;

/**
 * A singleton for handling key and truststores 
 * for the application.
 * <p>
 * @author Shirley Crompton
 * email shirley.crompton@stfc.ac.uk
 * org Data Science and Technology Group, UKRI Science and Technology Facilities Council
 * @created 18 Jun 2019
 */
public class KeystoreSingleton {
	
	/** Spring boot message logger */
	private static final Logger log = LoggerFactory.getLogger(KeystoreSingleton.class);
	/** An instance of the class */
	private static KeystoreSingleton instance = null;
	/** status flag **/
	public static boolean initialised = false;
	/** keystore **/
	public static KeyStore keystore;
	/** truststore **/
	public static KeyStore truststore;
	/** A password String to protected the keystore */
	private static final String STOREPASS = "serverStore";
	/** A password String to protected the truststore */
	private static final String TRUSTPASS = "trustStore";
	/** RSA keypair attribute*/
	private static KeyPair keypair = null;
	/** Secure random number generator attribute */
	private static SecureRandom random = new SecureRandom();
	
	

	/**
	 * Private constructor
	 */
	private KeystoreSingleton() {
		initialise();
	}
	
	/**
	 * Get an instance.  Create a new one if not yet instantiated.
	 * <p>
	 * @return an instance of the class.
	 */
	public static KeystoreSingleton getInstance() {
		if(instance == null) {
			instance = new KeystoreSingleton();
		}
		return instance;				
	}
	/**
	 * Initialise the global variables.  
	 */
	public static void initialise() {
		//
		if(initialised == false) {
			//
			try {
				
				
			}catch(Exception e) {
				log.error("Error getting agent's credential from shared docker volume: " + e.getMessage());
			}
			initialised = true;
			//log.debug("AgentSingleton initialised...");
		}		
	}
	/**
	 * Create a TrustStore using the predefined file name.  If the file exists, load it.  Else,
	 * create a new one and write it to file.
	 * The method also loads the bundled certificate PEMs to the store.
	 * <p>
	 * @throws CauException on error creating the keystore or on loading the 
	 * 					certificate PEMs.
	 */
	public void createTrustStore() throws CauException {
		File file = new File(CauProperties.CACERT_PATH);
		try {
			truststore = KeyStore.getInstance("JKS");		
		    if (file.exists()) {
		        // if exists, load		        
				truststore.load(new FileInputStream(file), TRUSTPASS.toCharArray());
		    } else {
		        // if not exists, create it
		    	log.debug("Creating the new truststore(" + CauProperties.CACERT_PATH + ")");
		        truststore.load(null, TRUSTPASS.toCharArray()); //initialise
		        truststore.store(new FileOutputStream(file), TRUSTPASS.toCharArray());
		    }
		    //9May18 updated to use the new CA cert 14May loaded untrust and fog ca public keys
		    storeCertificate("fog-sub",CredentialUtils.generateCertfromPEM(this.getClass().getResourceAsStream("/trust.pem")));
		    storeCertificate("ut-sub",CredentialUtils.generateCertfromPEM(this.getClass().getResourceAsStream("/untrust.pem")));
		    
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			log.error("Error writing keystore file : " + e.getMessage());
			throw new CauException("Error writing keystore file : " + e.getMessage());
		}
	}
	
	/**
	 * Create a KeyStore using the predefined file name.  If the file exists, load it.  Else,
	 * create a new one and write it to file.
	 * <p>
	 * @throws CauException on error creating the keystore.
	 */
	public void createKeyStore() throws CauException {
		File file = new File(CauProperties.STORE_PATH);
		try {
			keystore = KeyStore.getInstance("JKS");		
		    if (file.exists()) {
		        // if exists, load
		    	log.debug("keystore exists: loading file from " + CauProperties.STORE_PATH);
		    	//log.debug("storepass: " + STOREPASS);
				keystore.load(new FileInputStream(file), STOREPASS.toCharArray());
		    } else {
		        // if not exists, create it
		    	log.debug("Creating the new Keystore(" + CauProperties.STORE_PATH + ")");
		        keystore.load(null, STOREPASS.toCharArray()); //initialise
		        keystore.store(new FileOutputStream(file), STOREPASS.toCharArray());
		    }
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			log.error("Error writing keystore file : " + e.getMessage());
			throw new CauException("Error writing keystore file : " + e.getMessage());
		}
	}
	/**
	 * Store a X.509 certificate, along with its alias, the private key and the certificate chain.
	 * <p>
	 * @param alias		A {@link java.lang.String <em>String</em>} representation of the certificate alias
	 * @param cert		The certificate associated with the private key for the entry.
	 * @throws CauException	On error storing the key entry.
	 */
	public void storeKeyEntry(String alias, X509Certificate cert) throws KeyStoreException {
		log.debug("about to store key entry with alias: " + alias);		
		//hard coding the certificate chain for IT2 demo
		List<X509Certificate> mylist = new ArrayList<X509Certificate>();
		mylist.add(cert); //the certificate associated with the private key last (entity cert)
		mylist.add((X509Certificate) truststore.getCertificate("fog-sub")); //cau has cert issued by trusted ca
		X509Certificate[] chain = (X509Certificate[]) mylist.toArray(new X509Certificate[mylist.size()]);
		log.debug("About to store the end-entity cert with the fog ca cert as chain....");
		//keypass is the passphrase to the cert
		keystore.setKeyEntry(alias, this.keypair.getPrivate(), (STOREPASS).toCharArray(), chain);
		
	}
	
	/**
	 * Store a X.509 certificate with the alias.
	 * <p>
	 * @param alias	A {@link java.lang.String <em>String</em>} representation of the certificate alias
	 * @param cert	An X.509 certificate
	 * @throws CauException	if certificate is null or on storing the certificate
	 */
	public void storeCertificate(String alias, X509Certificate cert) throws CauException {
		if(cert == null) {
			throw new CauException("Cannot load null certificate with alias " + alias + "!");
		}else {
			try {
				truststore.setCertificateEntry(alias, cert);
			}catch(KeyStoreException ke) {				
				throw new CauException("KeystoreException loading certificate with alias " + alias + "! " + ke.getMessage());
			}
		}
				
	}
	

}
