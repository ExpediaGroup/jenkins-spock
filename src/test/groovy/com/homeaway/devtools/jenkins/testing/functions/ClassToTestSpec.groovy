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

package com.homeaway.devtools.jenkins.testing.functions

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

/**
 * Given a regular Groovy class that tries to call pipeline steps (here {@link ClassToTest}),
 * verify that those calls can be mocked.
 * 
 * @author awitt
 *
 */
public class ClassToTestSpec extends JenkinsPipelineSpecification {
	def "helloNode" () {
		given:
			ClassToTest myVar = new ClassToTest()
		when:
			myVar.helloNode( "nodeType" ) {
				echo( "inside node" )
			}
		then:
			1 * getPipelineMock( "node" )("nodeType", _)
			1 * getPipelineMock( "echo" )("Hello from a [nodeType] node!")
			1 * getPipelineMock( "echo" )("Goodbye from a [nodeType] node!")
			1 * getPipelineMock( "echo" )("inside node")
	}
}
