/*
 Copyright (c) 2020 Expedia Group.
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

import javax.servlet.ServletContext

import org.jvnet.hudson.reactor.ReactorException

import jenkins.model.Jenkins

/**
 * Verifies that jenkins-spock can run specifications when a Jenkins {@link Extension}'s {@link Descriptor}
 * tries to access the Jenkins singleton instance in the Descriptor's constructor.
 *
 * @author awitt
 * @since 2.1.4
 *
 */
public class DescriptorTimeJenkinsAccessingStepSpec extends JenkinsPipelineSpecification {

	/**
	 * Nothing actually needs to happen in this test.
	 * The "test" is whether jenkins-spock can even get to the point where it can try to run this test,
	 * because the jenkins-spock behavior being verified happens in jenkins-spock's setupSpec() method.
	 */
	def "test specifications can run when a Jenkins extension Descriptor tries to access the Jenkins singleton instance"() {
		when:
			true
		then:
			true
	}
	
}