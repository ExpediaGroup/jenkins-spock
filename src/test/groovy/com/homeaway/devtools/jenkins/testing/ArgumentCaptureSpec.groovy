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
 * Verifies the idioms for capturing arguments delivered to mocked pipeline steps.
 * Steps with multiple arguments must have the captured arguments unwrapped once.
 * See <a target="_blank" href="https://github.com/spockframework/spock/issues/603">spockframework/spock issue #603</a>.
 * 
 * @author awitt
 *
 */
public class ArgumentCaptureSpec extends JenkinsPipelineSpecification {
	
	def "single-argument capture" () {
		when:
			getPipelineMock("echo")("hello")
		then:
			1 * getPipelineMock("echo")(_) >> { _arguments ->
				assert "hello" == _arguments[0]
			}
	}
	
	def "multi-argument capture" () {
		when:
			getPipelineMock("stage")("label") {
				echo("body")
			}
		then:
			1 * getPipelineMock("stage")(_) >> { _arguments ->
				def args = _arguments[0]
				assert "label" == args[0]
			}
	}
	
}
