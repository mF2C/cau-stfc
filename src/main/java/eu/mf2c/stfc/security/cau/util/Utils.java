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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.mf2c.stfc.security.cau.exception.CauException;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
/**
 * Miscellaneous utilities.
 * <p>
 * @author Shirley Crompton
 * email shirley.crompton@stfc.ac.uk
 * org Data Science and Technology Group, UKRI Science and Technology Facilities Council
 * @created 18 Jun 2019
 */
public class Utils {
	/** Spring boot message logger */
	//private static final Logger log = LoggerFactory.getLogger(Utils.class);
	private static final Logger log = Logger.getLogger(Utils.class.getName());
	
	/**
     * Tokenise a comma&#45;separated {@link java.lang.String <em>String</em>} for
     * the individual parameters. The tokens are key&#45;value pairs, with each 
     * key&#45;value separated by a &#58; 
     * <p>
     * @param message  incoming message {@link java.lang.String <em>String</em>}
     * @return	a {@link java.util.Map <em>Map</em>} of the extracted request parameters
     * @throws  {@link eu.mf2c.stfc.security.cau.exception.CauException <em>CauException</em>} 
     * 			on error	
     */
    public static Map<String, String> getValues(String message) throws CauException {
    	//request msg params : csr=,deviceID=,detectedLID=,detectedLIP=,IDKey=,type=
    	//log.debug("about to getValues from : " + message);
    	log.log(Level.ALL,"about to getValues from : " + message);
    	//System.out.println("about to getValues from : " + message);
    	Map<String, String> map = new HashMap<String, String>();
    	try {
	    	String[] msgList = message.split(",");
	    	for (String entry : msgList) {
	    		//find the first ":"
	    		int i = entry.indexOf(":");
	    		//System.out.println(": position " + i);
	    		  String key = entry.substring(0,i);
	    		  String value = entry.substring(i+1);
	    		  map.put(key.trim(), value.trim()/*.replaceAll("\n","").replaceAll("\r","")*/);
	    		  //String[] keyValue = entry.split(i);
	    		  //map.put(keyValue[0],keyValue[1]);    		  
	    	}
	    	//map.forEach((k,v)->log.debug(k + " : " + v));
	    	map.forEach((k,v)->System.out.println(k + " : " + v));
    	}catch(Exception e) {
    		throw new CauException("Error extracting cert request params : " + e.getMessage());
    	}
    	return map;
    }
    

}
