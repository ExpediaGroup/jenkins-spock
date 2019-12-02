/*
 Copyright (c) 2019 Expedia Group.
 All rights reserved.  http://www.homeaway.com
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
   http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.homeaway.devtools.jenkins.testing.functions

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

/**
 * Verify that classes without methods don't trip up the "failed to dispatch a call" logic
 * 
 * @author awitt
 *
 */
public class ClassWithNoMethodsSpec extends JenkinsPipelineSpecification {
	
	def "calling nonexistent method"() {
		given:
			ClassToTest myVar = new ClassToTest()
		when:
			myVar.nonexistentMethod()
		then:
			thrown IllegalStateException
	}
}
