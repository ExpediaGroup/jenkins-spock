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

package com.homeaway.devtools.jenkins.testing.jobs

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

/**
 * Given a classpath resource that is a whole pipeline script,
 * verify that {@link JenkinsPipelineSpecification#loadPipelineScriptForTest(java.lang.String)}
 * can successfully load that script and instrument its code to hit the mocks.
 * 
 * @author awitt
 *
 */
public class JenkinsfileSpec extends JenkinsPipelineSpecification {
	def "Jenkinsfile"() {
	setup:
		def Jenkinsfile = loadPipelineScriptForTest("com/homeaway/devtools/jenkins/testing/jobs/Jenkinsfile.groovy")
		getPipelineMock("node")(_ as String, _ as Closure ) >> { String _label, Closure _body ->
			_body()
		}
	when:
		Jenkinsfile.run()
	then:
		1 * getPipelineMock("node")("legacy", _)
		1 * getPipelineMock("echo")("hello world")
	}
}
