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

package com.homeaway.devtools.jenkins.testing

import com.example.TestGroovyClass

public class ClassloaderIdentitySpec extends JenkinsPipelineSpecification {
	
	// setupAdvisory = groovy_class_returning_script
	// errata = test_groovy_class
	// advisories = returned_test_groovy_class
	 
	def groovy_class_returning_script
	def expected_object = new TestGroovyClass()
	Class<?> expected_class = expected_object.getClass()
	 
	def "Groovy scrips' classes are loaded with the same classloader as tests' classes"() {
	 
		when:
			groovy_class_returning_script = loadPipelineScriptForTest( "com/homeaway/devtools/jenkins/testing/GroovyClassReturningScript.groovy" )
			def returned_object = groovy_class_returning_script()
			def returned_class = returned_object.getClass()
			
			def reflected_class = Class.forName(
				"com.example.TestGroovyClass",
				true,
				expected_class.getClassLoader() )
				.newInstance()
				.getClass()
		then:
			// The "same class" should've been loaded with the same classloader...
			expected_class.getClassLoader() == reflected_class.getClassLoader()
			expected_class.getClassLoader() == returned_class.getClassLoader()
		
			// The "same class" should obviously have the same name
			expected_class.getName() == reflected_class.getName()
			expected_class.getName() == returned_class.getName()
			
			// The "same class" should also be identity-equal.
			expected_class == reflected_class
			expected_class == returned_class
	}
	 
}