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
 * tries to access the Jenkins singleton instance in the Descriptor's constructor,
 * then tries to interact with the result of calling a method on that singleton.
 * This requires that the test-suite has successfully stubbed the interaction with Jenkins that happens
 * <i>outside</i> of normal Spock test cases. 
 *
 * @author awitt
 * @since 2.1.4
 *
 */
public class DescriptorTimeJenkinsInteractingSpec extends JenkinsPipelineSpecification {
	
	static { 
		/* Even though setupSpec() happens "once, before all tests" as if it were static,
		 * it's still actually an instance method.
		 * Therefore, the constructor of the test specification must be called before that time
		 * which is as good a time as any to set up the system property that will activate the
		 * problematic behavior that this test case verifies jenkins-spock can handle.
		 *
		 * Unfortunately, constructors are not allowed in Spock specifications.
		 * So, we'll use static {} setup to ensure that the property is set
		 * before any jenkins-spock code tries to run.
		 */
		
		System.setProperty( "jenkins.test-access.descriptor", "true" )
	}
	
	def cleanupSpec() {
		System.setProperty( "jenkins.test-access.descriptor", "false" )
	}
	
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