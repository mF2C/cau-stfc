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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mf2c.stfc.security.cau.exception.CauException;
import eu.mf2c.stfc.security.cau.model.Pubkey;

/**
 * Utilities for processing credentials
 * <p>
 * @author Shirley Crompton
 * email shirley.crompton@stfc.ac.uk
 * org Data Science and Technology Group, UKRI Science and Technology Facilities Council
 * @created 18 Jun 2019
 */
public class CredentialUtils {
	
	/** Spring boot message logger */
	private static final Logger log = LoggerFactory.getLogger(CredentialUtils.class);
	/**
	 * Load an X.509 certificate from a PEM file.
	 * <p>
	 * @param path 	full path to the PEM file
	 * @return	the loaded certificate object.
	 */
	public static X509Certificate loadX509(String path) {
		X509Certificate certificate = null;
		try {
			FileInputStream fis = new FileInputStream(path);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			certificate = (X509Certificate) cf.generateCertificate(fis);
			// System.out.println("Type : " + certificate.getType());
			// System.out.println("Issuer Principal Name : " +
			// certificate.getIssuerX500Principal().getName());
			// System.out.println("Principal Name : " +
			// certificate.getSubjectX500Principal().toString());
			log.debug("Principal Name : " + certificate.getSubjectX500Principal().toString());
		} catch (Exception e) {
			//
			log.error("Error loading the certificate: " + e.getMessage());
		}
		return certificate;
	}
	/**
	 * Load a X.509 certificate from the provided input stream
	 * <p>
	 * @param inStream 	an input stream to the PEM file
	 * @return	the generated X.509 certificate or null if there is an error.
	 */
	public static X509Certificate generateCertfromPEM(InputStream inStream) {
		X509Certificate ca = null;
		//
		try {
	        CertificateFactory cf = CertificateFactory.getInstance("X.509");
			//
			ca = (X509Certificate) cf.generateCertificate(inStream);
		} catch (CertificateException e) {
			// 
			log.error("CertificateException generating certificate from file: " + e.getMessage());
		} finally{
			try {
				inStream.close();
			}catch (IOException e) {
				//too bad
				log.error("IOException generating certificate from file: " + e.getMessage());
			}
		}
        return ca;
		
	}
	
	/**
	 * Generate a 2048 bit RSA {@link java.security.KeyPair <em>KeyPair</em>}.
	 * <p>
	 * @return the {@link java.security.KeyPair <em>KeyPair</em>} or null if error
	 */
	public static KeyPair generateKP() {
		KeyPairGenerator keyGen;
		KeyPair keypair = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048, new SecureRandom());
			keypair = keyGen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			log.error("Error generating RSA keypair: " + e.getMessage());			
		} 
		return keypair;
	}
	/**
	 * Process the trusted CA out and extract the certificate String.
	 * <p>
	 * @param caOutput	String output from the trusted CA
	 * @return	the X.509 certificate object
	 * @throws CauException 	on error
	 */
	public static X509Certificate extractCert(String caOutput)
			throws CauException {
		
		try {
			//split the output from the trusted ca
			//String pkString = caOutputubstring(0, caOutput.indexOf("-----BEGIN CERTIFICATE"));
			String certString = caOutput.substring(caOutput.indexOf("-----BEGIN CERTIFICATE"));
			// System.out.println("private key String : " + pkString);
			// System.out.println("cert String : " + certString);
			// loads the credential and check
			//PrivateKey privKey = getKeyFromStr(pkString);
			// System.out.println("about to get certficiate from String ...");
			return getCertFromString(certString);
		}catch(CertificateException e) {
			throw new CauException("Error extracting certificate : " + e.getMessage());
		}
		// System.out.println("about to verify key...");		// verify key
		//RSAPrivateCrtKey priv = (RSAPrivateCrtKey) privKey;
		//RSAPublicKeySpec rsaSpec = new RSAPublicKeySpec(priv.getModulus(), priv.getPublicExponent());
		//PublicKey pubKey = keyFactory.generatePublic(rsaSpec);
		//
	}
	/**
	 *	Convert a {@link java.lang.String <em>String</em>} representation of an X.509 certificate
	 * 	to an X.509 certificate object.
	 *  <p>
	 *  @param cert	the certificate String
	 *  @return the converted certificate object
	 *  @throws  {@link java.security.cert.CertificateException <em>CertificateException</em>} on error
	 * 
	 */
	public static X509Certificate getCertFromString(String cert) throws CertificateException {
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
	    ByteArrayInputStream bytes = new ByteArrayInputStream(cert.getBytes());
	    return (X509Certificate) certFactory.generateCertificate(bytes);
	}
	/**
	 * Load a java keystore from file.  If the input file
	 * has a p12 file suffice, a PKCS12 keystore is generated.
	 * Otherwise, a JKS keystore is generated.
	 * <p>
	 * @param path	the full path to the keystore file
	 * @param pass	the password to the store
	 * @return	the loaded keystore
	 */
	public static KeyStore loadKeyStore(String path, String pass) {
		KeyStore ks = null;

		try {
			if(path.endsWith("p12")) {
				ks = KeyStore.getInstance("PKCS12");
			}else
				ks = KeyStore.getInstance("JKS");
			//
			InputStream is = Files.newInputStream(Paths.get(path));
			ks.load(is, pass.toCharArray());
		} catch (Exception e) {
			log.error("Error loading " + path + " : " + e.getMessage());
		}
		return ks; //you either get the one from file or an empty one 
	}
	/**
	 * Create a {@link java.lang.String <em>String</em>} representation 
	 * of a RSA public key object associated with an Agent&#39;s X509.Certificate
	 * <p>
	 * @param certStr	A  {@link java.lang.String <em>String</em>} representation of
	 * 					the Agent&#39;s X509.Certificate
	 * @return	A  {@link java.lang.String <em>String</em>} representation of the RSA 
	 * 					public key	
	 * @throws CauException	on error
	 */
	public static String getPubKeyString(String certStr) throws CauException {
		String pk = null;
		
		try {
			X509Certificate cert = getCertFromString(certStr);
			PublicKey pubKey = cert.getPublicKey();
			pk = getPubKeyString(pubKey);			
			//			
		}catch(Exception e) {
			throw new CauException("Error creating public key String : " + e.getMessage());
		}	
		
		return pk;
	}
	/**
	 * Get a {@link java.lang.String <em>String</em>} representation 
	 * of a RSA public key object
	 * <p>
	 * @param pubKey	the {@link java.security.PublicKey <em>PublicKey</em>} object
	 * @return	A  {@link java.lang.String <em>String</em>} representation of the RSA 
	 * 					public key	
	 */
	public static String getPubKeyString(PublicKey pubKey) {
		return "-----BEGIN RSA PUBLIC KEY-----" + java.util.Base64.getEncoder().encodeToString(pubKey.getEncoded()) + "-----END RSA PUBLIC KEY-----";
	}
	
	
	

}
