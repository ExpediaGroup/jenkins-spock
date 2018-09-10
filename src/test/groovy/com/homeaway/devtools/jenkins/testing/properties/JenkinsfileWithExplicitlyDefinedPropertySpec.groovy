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
 * Given a whole pipeline script that attempts to access a variable that has been explicitly mocked with
 * {@link JenkinsPipelineSpecification#explicitlyMockPipelineVariable(java.lang.String)}, verify
 * that a mock for that variable is successfully created and connected.
 *  
 * @author awitt
 *
 */
public class JenkinsfileWithExplicitlyDefinedPropertySpec extends JenkinsPipelineSpecification {
	
	protected Script Jenkinsfile
	
	def setupSpec() {
		JenkinsPipelineSpecification.metaClass = null
		PipelineVariableImpersonator.metaClass = null
	}
	
	def setup() {

		// mock the variable "env"
		explicitlyMockPipelineVariable("env")
		
		// Load the Jenkinsfile
		Jenkinsfile = loadPipelineScriptForTest("com/homeaway/devtools/jenkins/testing/properties/JenkinsfileWithExplicitlyDefinedProperty.groovy")
	}

	def "variable.propertyAccess" () {
		when:
			Jenkinsfile.run()
		then:
			1 * getPipelineMock("stage")("property access", _)
			1 * getPipelineMock("env.getProperty")("someEnvVar") >> "EXPECTED ENVVAR VALUE"
			1 * getPipelineMock("echo")("EXPECTED ENVVAR VALUE")
	}
	
	def "inline property accesses" () {
		when:
			String someEnvvar = env.someEnvvar
			String otherEnvvar = env.otherEnvvar
		then:
			1 * getPipelineMock( "env.getProperty" )( "someEnvvar" )
			1 * getPipelineMock( "env.getProperty" )( "otherEnvvar" ) >> "expected"
		expect:
			"expected" == otherEnvvar
	}
}
