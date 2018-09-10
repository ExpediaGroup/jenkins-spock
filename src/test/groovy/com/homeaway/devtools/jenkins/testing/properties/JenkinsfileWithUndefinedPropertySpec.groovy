/*
 Copyright (c) 2018 Expedia Group.
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

package com.homeaway.devtools.jenkins.testing.properties

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification
import com.homeaway.devtools.jenkins.testing.PipelineVariableImpersonator

/**
 * Given a whole pipeline script that attempts to access a variable that is truly not defined,
 * verify that the appropriate exception is thrown.
 * 
 * @author awitt
 *
 */
public class JenkinsfileWithUndefinedPropertySpec extends JenkinsPipelineSpecification {
	
	protected Script Jenkinsfile
	
	def setupSpec() {
		JenkinsPipelineSpecification.metaClass = null
		PipelineVariableImpersonator.metaClass = null
	}
	
	def setup() {
		// Load the Jenkinsfile
		Jenkinsfile = loadPipelineScriptForTest("com/homeaway/devtools/jenkins/testing/properties/JenkinsfileWithUndefinedProperty.groovy")
	}
	
	def "fails"() {
		when:
			Jenkinsfile.run()
		then:
			thrown IllegalStateException
	}
}
