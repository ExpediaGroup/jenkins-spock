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
import com.homeaway.devtools.jenkins.testing.PipelineVariableImpersonator

/**
 * When a class-under-test defines its own <code>methodMissing</code> or <code>propertyMissing</code>
 * methods, verify {@link JenkinsPipelineSpecification}'s best-effort attempt to delegate
 * to those handlers while still enabling mock pipeline steps and variables to be accessed
 * 
 * @author awitt
 *
 */
public class ClassWithMissingHandlersSpec extends JenkinsPipelineSpecification {
	
	ClassWithMissingHandlers clazz
	
	def setup() {
		clazz = new ClassWithMissingHandlers()
	}
	
	def "normal methods work normally" () {
		when:
			clazz.helloNode( "nodeType" ) {
				echo( "inside node" )
			}
		then:
			1 * getPipelineMock( "node" )("nodeType", _)
			1 * getPipelineMock( "echo" )("Hello from a [nodeType] node!")
			1 * getPipelineMock( "echo" )("Goodbye from a [nodeType] node!")
			1 * getPipelineMock( "echo" )("inside node")
	}
	
	def "truly missing methods raise exception" () {
		when:
			clazz.madeUpMethod( "foo" )
		then:
			thrown IllegalStateException
	}
	
	def "missing pipeline steps hit the mock" () {
		when:
			clazz.stage("someStage") {
				"cats"
			}
		then:
			1 * getPipelineMock("stage")( "someStage", _ )
	}
	
	def "class' own methodMissing is preferred" () {
		given:
			def retval = clazz.sh( "foo" )
		expect:
			"myCustomShReturnValue" == retval
	}
	
	def "truly missing properties raise exception" () {
		when:
			clazz.madeUpProperty
		then:
			thrown IllegalStateException
	}
	
	def "missing pipeline vars hit the mock" () {
		setup:
			explicitlyMockPipelineVariable( "DefinedGlobalVariable" )
			def real_global_var = clazz.DefinedGlobalVariable
		expect:
			real_global_var != null
			real_global_var instanceof PipelineVariableImpersonator
	}
	
	def "class' own propertyMissing is preferred" () {
		given:
			def retval = clazz.dynamicProperty
		expect:
			"myCustomDynamicPropertyValue" == retval
	}
	
}
