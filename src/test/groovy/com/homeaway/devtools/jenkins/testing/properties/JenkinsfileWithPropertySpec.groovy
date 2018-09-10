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
 * Given a whole pipeline script that attempts to access a variable from an extension point
 * (likely a {@link org.jenkinsci.Symbol Symbol} or {@link org.jenkinsci.plugins.workflow.cps.GlobalVariable GlobalVariable}), verify that the correct mock object
 * is present and connected.
 * 
 * @author awitt
 *
 */
public class JenkinsfileWithPropertySpec extends JenkinsPipelineSpecification {
	
	protected Script Jenkinsfile
	
	def setupSpec() {
		JenkinsPipelineSpecification.metaClass = null
		PipelineVariableImpersonator.metaClass = null
	}
	
	def setup() {
		// Load the Jenkinsfile
		Jenkinsfile = loadPipelineScriptForTest("com/homeaway/devtools/jenkins/testing/properties/JenkinsfileWithProperty.groovy")
	}
	
	def "Jenkinsfile"() {
		when:
			Jenkinsfile.run()
		then:
			1 * getPipelineMock("node")("legacy", _)
			1 * getPipelineMock("echo")("the end")
		expect:
			null != mocks
			
			null != mocks.get("docker")
			null != getPipelineMock("docker")
			mocks.get("docker") instanceof PipelineVariableImpersonator
	}
	
	def "expected method invocation on global variable" () {
		when:
			Jenkinsfile.run()
		then:
			1 * getPipelineMock("stage")("expected", _)
			1 * getPipelineMock("docker.expectedInvocation")("expected")
		expect:
			null != mocks.get("docker.expectedInvocation")
			null != getPipelineMock("docker.expectedInvocation")
			mocks.get("docker.expectedInvocation") instanceof Closure
			"Mock for type 'Closure' named '(implicit-expected) getPipelineMock(\"docker.expectedInvocation\")'" == mocks.get("docker.expectedInvocation").toString()
	}
	
	def "unexpected method invocation on global variable" () {
		when:
			Jenkinsfile.run()
		then:
			1 * getPipelineMock("stage")("unexpected", _)
		expect:
			null != mocks.get("docker.unexpectedInvocation")
			null != getPipelineMock("docker.unexpectedInvocation")
			mocks.get("docker.unexpectedInvocation") instanceof Closure
			"Mock for type 'Closure' named '(implicit-runtime) getPipelineMock(\"docker.unexpectedInvocation\")'" == mocks.get("docker.unexpectedInvocation").toString()
	}
	
	def "stubbed method invocation on global variable" () {
		when:
			Jenkinsfile.run()
		then:
			1 * getPipelineMock("stage")("stubbed", _)
			1 * getPipelineMock("docker.stubbedInvocation")("original input from script") >> "stubbed"
			1 * getPipelineMock("docker.stubbedInvocation")("stubbed")
		expect:
			null != mocks.get("docker.stubbedInvocation")
			null != getPipelineMock("docker.stubbedInvocation")
			mocks.get("docker.stubbedInvocation") instanceof Closure
			"Mock for type 'Closure' named '(implicit-expected) getPipelineMock(\"docker.stubbedInvocation\")'" == mocks.get("docker.stubbedInvocation").toString()
	}
}
