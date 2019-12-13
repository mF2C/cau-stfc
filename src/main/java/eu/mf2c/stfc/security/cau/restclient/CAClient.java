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

package eu.mf2c.stfc.security.cau.restclient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mf2c.stfc.security.cau.exception.CauException;
import eu.mf2c.stfc.security.cau.util.CauProperties;
import eu.mf2c.stfc.security.cau.util.CredentialUtils;

/**
 * A REST client to interact over https with the cloud CA to get an Agent &#39;s
 * certificate signing request signed.
 * <p>
 * 
 * @author Shirley Crompton email shirley.crompton@stfc.ac.uk org Data Science
 *         and Technology Group, UKRI Science and Technology Facilities Council
 * @created 18 Jun 2019
 */
public class CAClient {
	/*
	 * :TODO once I swapped the tomcat self-signed server cert on the CA with one
	 * signed by the fogCA, I need to change the ssl trust strategy to use trust
	 * anchors
	 */
	/** Spring boot message logger */
	private static final Logger log = LoggerFactory.getLogger(CAClient.class);
	/** Certificate Signing Request attribute */
	private String csr;

	/**
	 * Construct an instance
	 * 
	 * @param csrString the Certificate Signing Request String to post to the CA
	 */
	public CAClient(String csrString) {
		this.csr = csrString;
	}

	/**
	 * Post operation to send a Certificate Signing Request to the untrusted CA for
	 * an Agent X.509 certificate.
	 * <p>
	 * 
	 * @return a {@link java.lang.String <em>String</em>} representation of the
	 *         X.509 certificate.
	 * @throws {@link CauException <em>CauException</em>} on error
	 */
	public String httpsPost() throws CauException {
		CloseableHttpClient client = null;
		// prepare the post
		// HttpPost post = new
		// HttpPost("https://213.205.14.13:54443/certauths/rest/it2untrustedca");
		HttpPost post = new HttpPost(CauProperties.cloudCA + CauProperties.CA);
		System.out.println(new Date().toString() + ": cau: CAClient posting to CA(IP: " + CauProperties.cloudCA + CauProperties.CA + ")" );
		//
		String cert = "";
		StringEntity str;
		CloseableHttpResponse resp = null;
		try {
			client = getCloseableHttpClient();
			//debug
			System.out.println(new Date().toString() + ": about to post csr: " + this.csr);
			str = new StringEntity(this.csr);
			post.setEntity(str);
			resp = client.execute(post);
			log.debug(resp.getStatusLine().getReasonPhrase());
			int status = resp.getStatusLine().getStatusCode();
			long length = resp.getEntity().getContentLength();
			System.out.println(new Date().toString() + ": RC " + status + " length: " + String.valueOf(length));
			log.debug("RC " + status + " length: " + String.valueOf(length));
			
			if (status >= 200 && status < 300) {
				System.out.println(new Date().toString() + ": About to get content using EntityUtils...");
				log.debug("About to get content using EntityUtils...");
				if (length != -1 && length < 2048) {
			        cert = EntityUtils.toString(resp.getEntity());
					//System.out.println(cert);			        
			    } else {
			        // read in stream, but cert normally under 2k
			    	System.out.println(new Date().toString() + ": About to get content as stream...");
			    	log.debug("About to get content as stream...");
			    	cert = getCertStr(resp.getEntity().getContent());
			    }		
			}else {
				throw new Exception("CA reported error : " + String.valueOf(status));
			}
		} catch (Exception e) {			
			if (e instanceof CauException) {
				throw (CauException) e;
			} else {
				throw new CauException(e);
			}
		} finally {
			if (resp != null) {
				try {
					resp.close();
				} catch (IOException e) {
					log.warn("Error closing response object : " + e.getMessage());
				}
			}
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					log.warn("Error closing client object : " + e.getMessage());
				}
			}
		}
		return cert;
	}
	/**
	 * Read in the CA response as an inputstream object
	 * <p>
	 * @param is	the response input stream
	 * @return		a {@link java.lang.String <em>String</em>} representation of the 
	 * 				X.509 certificate object
	 * @throws		{@link CauException <em>CauException</em>} on error
	 */
	public String getCertStr(InputStream is) throws CauException{
		String cert = "";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedInputStream in = null;
		try {
			baos = new ByteArrayOutputStream();
			in = new BufferedInputStream(is);
			// Create buffer: typical cert is about 2KB, not sure about underlying capability, use a small buffer
			byte[] buffer = new byte[1024]; //
			int bytesRead = 0;
			//log.debug("waiting for CA response....");
			while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
				//
				baos.write(buffer, 0, bytesRead); //keep adding to the buffer
				log.info("written " + bytesRead + " bytes");
			}
			baos.flush();
			cert = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		}catch(Exception e) {
			throw new CauException(e);
		}finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.warn("Error closing response stream : " + e.getMessage());
				}
			}			
		}		
		return cert;
	}


	/**
	 * Configure a Http client for interacting with the cloud Certificate Authority
	 * <p>
	 * @return the configured client
	 * @throws CauException on error
	 */
	private CloseableHttpClient getCloseableHttpClient() throws CauException {
		log.debug("about to create closeable http client ...");
		CloseableHttpClient httpClient = null;
		String ksType = (CauProperties.STORE_PATH.endsWith("jks") ? "JKS" : "PKCS12");
		try {
			// use the mf2c certs as the trust anchor
			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
					 .loadTrustMaterial(CredentialUtils.loadKeyStore(CauProperties.CACERT_PATH,
					 "trustStore"), new org.apache.http.conn.ssl.TrustSelfSignedStrategy())
					// trust self-signed cert, and Verification of all other certificates is done by
					// the trust manager configured in the SSL context./
					//.loadTrustMaterial(null, new TrustStrategy() {
					//	public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					//		return true;
					//	}
					//}) // accept all cert
					.setKeyStoreType(ksType)
					.loadKeyMaterial(CredentialUtils.loadKeyStore(CauProperties.STORE_PATH, "serverStore"),
							"serverStore".toCharArray())
					.build();
			log.debug("about to build http client using TLSv1.2 protocol...");
			/*******************6Dec2019 fix
			// turn off hostname verification
			httpClient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
					.setSSLContext(sslContext). // set context and trust all cert
					setDefaultHeaders(getHeadersAsList()). // set headers
					build();*/
			// force the use of TLS1.2
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
	                sslContext,
	                new String[] { "TLSv1.2" }, //protocols
	                null, //ciphers
	                NoopHostnameVerifier.INSTANCE);
			// turn off hostname verification
			httpClient = HttpClients.custom()
					.setSSLSocketFactory(sslsf) 
					.setDefaultHeaders(getHeadersAsList()) // set headers
					.build();
			////////////end 5Dec2019 fix
		} catch (Exception e) {
			throw new CauException("Error getting http client : " + e.getMessage());
		}
		return httpClient;
	}

	/**
	 * Set header parameters for the client.
	 * <p>
	 * 
	 * @return a list of the header parameters.
	 */
	private List<Header> getHeadersAsList() {
		//
		final List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/plain"));
		headers.add(new BasicHeader(HttpHeaders.ACCEPT, "text/plain"));
		headers.add(new BasicHeader(HttpHeaders.CONTENT_ENCODING, "utf-8"));
		return headers;

	}

}
