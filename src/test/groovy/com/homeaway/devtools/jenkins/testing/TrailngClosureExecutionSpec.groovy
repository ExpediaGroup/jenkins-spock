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

package com.homeaway.devtools.jenkins.testing;

/**
 * Verifies that "trailng" closures are executed when passed to mocks.
 * It's overwhelmingly common for these to be executed that way in Jenkins pipelines,
 * so much so that it's reasonable for the framework to stub this behavior for all mocks.
 * 
 * @author awitt
 *
 */
public class TrailingclosureExecutionSpec extends JenkinsPipelineSpecification {
	
	def setup() {
		explicitlyMockPipelineVariable("someSymbol")
	}
	
	def "STEP: trailing closure is executed when passed implicitly & is the only argument"() {
		when:
			node() {
				echo( "payload" )
			}
		then:
			1 * getPipelineMock("echo")("payload")
	}
	
	def "STEP: trailing closure is executed when passed implicitly & is not the only argument"() {
		when:
			node("label") {
				echo( "payload" )
			}
		then:
			1 * getPipelineMock("echo")("payload")
	}
	
	def "STEP: trailing closure is executed when passed explicitly & is the only argument"() {
		when:
			node({ echo("payload")})
		then:
			1 * getPipelineMock("echo")("payload")
	}
	
	def "STEP: trailing closure is executed when passed explicitly & is not the only argument"() {
		when:
			node("label", { echo("payload")})
		then:
			1 * getPipelineMock("echo")("payload")
	}
	
	def "SYMBOL: trailing closure is executed when passed implicitly & is the only argument"() {
		when:
			someSymbol() {
				echo( "payload" )
			}
		then:
			1 * getPipelineMock("echo")("payload")
	}
	
	def "SYMBOL: trailing closure is executed when passed implicitly & is not the only argument"() {
		when:
			someSymbol("label") {
				echo( "payload" )
			}
		then:
			1 * getPipelineMock("echo")("payload")
	}
	
	def "SYMBOL: trailing closure is executed when passed explicitly & is the only argument"() {
		when:
			someSymbol({ echo("payload")})
		then:
			1 * getPipelineMock("echo")("payload")
	}
	
	def "SYMBOL: trailing closure is executed when passed explicitly & is not the only argument"() {
		when:
			someSymbol("label", { echo("payload")})
		then:
			1 * getPipelineMock("echo")("payload")
	}
}
