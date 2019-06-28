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

import org.springframework.data.gemfire.repository.query.annotation.Trace;
import org.springframework.data.repository.CrudRepository;

/**
 * Interface to work with the Gemfire Agent public key repository
 * <p>
 * @author Shirley Crompton
 * email shirley.crompton@stfc.ac.uk
 * org Data Science and Technology Group, UKRI Science and Technology Facilities Council
 * @created 26 Jun 2019
 */
public interface PubkeyRepo extends CrudRepository<Pubkey, String> {
	/** 
	 * Retrieve RSA public key by the associated device id 	 * 
	 * */
	@Trace
	Pubkey findByDeviceId(String deviceId);

}
