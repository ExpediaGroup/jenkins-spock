/*
 Copyright (c) 2020 Electronic Arts Inc.
 All rights reserved.
 
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
 * Verifies that the <code>retry(...)</code> pipeline step is special-cased and
 * that any closures passed to it as arguments are executed up to <code>count</code> times.
 *
 * @author stuartrowe
 *
 */
public class RetryClosureExecutionSpec extends JenkinsPipelineSpecification {
	
	def "retry() closure is only executed once when the first execution succeeds"() {
		when:
			retry(2) {
				sh( "echo hello" )
			}
		then:
			1 * getPipelineMock("sh")(*_)
	}
	
	def "retry() closure is executed twice when the first execution fails"() {
		when:
			retry(2) {
				sh( "echo hello" )
			}
		then:
			1 * getPipelineMock("sh")(*_) >> { throw new Exception() }
		then:
			1 * getPipelineMock("sh")(*_)
	}
	
	def "retry() propagates the exception when retries are exhausted"() {
		when:
			retry(2) {
				sh( "echo hello" )
			}
		then:
			2 * getPipelineMock("sh")(*_) >> { throw new Exception() }
		then:
			thrown Exception
	}
}