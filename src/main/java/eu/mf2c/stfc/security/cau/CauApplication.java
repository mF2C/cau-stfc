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

import org.apache.geode.cache.client.ClientRegionShortcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

import eu.mf2c.stfc.security.cau.model.Pubkey;
import eu.mf2c.stfc.security.cau.model.PubkeyRepo;
import eu.mf2c.stfc.security.cau.util.CauProperties;
/**
 * Entry point to the mF2C CAU application.
 * <p> 
 * @author Shirley Crompton
 * email shirley.crompton@stfc.ac.uk
 * org Data Science and Technology Group, UKRI Science and Technology Facilities Council
 * @created 12 Jun 2019
 */
@SpringBootApplication
@ClientCacheApplication(name="Cau", logLevel = "error")
//creates PGF/Geode regions base don the application persistent entities
//scan for region in the classes defined
@EnableEntityDefinedRegions(basePackageClasses = Pubkey.class, clientRegionShortcut=ClientRegionShortcut.LOCAL_PERSISTENT)
@EnableGemfireRepositories
public class CauApplication implements ApplicationRunner {
	/** Spring boot message logger */
	private static final Logger log = LoggerFactory.getLogger(CauApplication.class);
	/** The GemFire cache object for persisting public keys
	@Autowired
    PubkeyRepo pubkeyRepo;	*/
	
	/**
	 * Entry point to the application
	 * <p>
	 * @param args	application params
	 */
	public static void main(String[] args) {
		SpringApplication.run(CauApplication.class, args);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(ApplicationArguments appArgs) {		
		//initialise application, e.g. set up data access etc.
		String cloudca = "";
		String cloudcimi = "";
		
		for (String name : appArgs.getOptionNames()) {
	         if (name.equals("cloudca"))
	            cloudca = appArgs.getOptionValues(name).get(0);
	         if (name.equals("cloudcimi"))
	        	 cloudcimi = appArgs.getOptionValues(name).get(0);
	    }
		if(!cloudca.isEmpty()) {
			CauProperties.cloudCA = cloudca;
		}
		if(!"cloudcimi".isEmpty()) {
			CauProperties.CIMIAPI = cloudcimi;
			//debug
			System.out.println("Sent cloudcimi to: " + cloudcimi);
		}
		
		
	}

}
