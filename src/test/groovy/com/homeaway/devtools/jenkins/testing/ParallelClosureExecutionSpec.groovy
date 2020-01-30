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

/**
 * Verifies that the <code>parallel(...)</code> pipeline step is special-cased and
 * that any closures passed to it as arguments are executed.
 *
 * @author awitt
 *
 */
public class ParallelClosureExecutionSpec extends JenkinsPipelineSpecification {
	
	def "parallel() closures are executed when there is only one closure"() {
		when:
			parallel(
				"stream1": {
					echo( "stream1" )
				}
			)
		then:
			1 * getPipelineMock("echo")("stream1")
	}
	
	def "parallel() closures are executed when there are multiple closures"() {
		when:
			parallel(
				"stream1": {
					echo( "stream1" )
				},
				"stream2": {
					echo( "stream2" )
				}
			)
		then:
			1 * getPipelineMock("echo")("stream1")
			1 * getPipelineMock("echo")("stream2")
	}
	
	def "parallel() does not error when no closures are provided"() {
		when:
			parallel()
		then:
			0 * getPipelineMock("echo")(_)
	}
	
	def "parallel() handles non-closure arguments"() {
		when:
			parallel(
				"failFast": true,
				"stream 1" : {
					echo( "hello 1" )
				},
				"stream 2" : {
					echo( "hello 2" )
				}
			)
		then:
			1 * getPipelineMock("echo")("hello 1")
			1 * getPipelineMock("echo")("hello 2")
	}
	
	def "parallel() mock is called even when its closures are subsequently executed"() {
		when:
			parallel(
				"stream1": {
					echo( "stream1" )
				},
				"stream2": {
					echo( "stream2" )
				}
			)
		then:
			1 * getPipelineMock("parallel")(_)
	}
	
	def "parallel() arguments can be captured"() {
		when:
			parallel(
				"stream1": {
					echo( "stream1" )
				},
				"stream2": {
					echo( "stream2" )
				}
			)
		then:
			1 * getPipelineMock("parallel")(_ as Map) >> { _parallel_args ->
				Map parallel_map = _parallel_args[0]
				
				assert parallel_map instanceof Map
				assert parallel_map.size() == 2
				assert parallel_map.containsKey( "stream1" )
				assert parallel_map.containsKey( "stream2" )
			}
	}
}