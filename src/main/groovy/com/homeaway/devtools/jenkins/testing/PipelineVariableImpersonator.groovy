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

package com.homeaway.devtools.jenkins.testing

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Takes the place of a {@link GlobalVariable} in a {@link JenkinsPipelineSpecification} context.
 * Intercepts every method call and forwards it to a mock for the current test-run.
 * Mocks are named with the pattern <code>GlobalVariableName.methodName</code> - for example, in the following pipeline script under test:
 * <pre><code>
SomeVariable.someFunction("foo")
 * </code></pre>
 * The <b>someFunction</b> call could be verified by
 * <pre><code>
then:
	1 * getPipelineMock("SomeVariable.someFunction")("foo")
 * </code></pre>
 * 
 * @author awitt
 *
 */
public class PipelineVariableImpersonator {
	
	private static final Logger LOG = LoggerFactory.getLogger(PipelineVariableImpersonator.class)
	
	/**
	 * The "Global Variable" property name to automatically generate mocks for.
	 */
	protected String for_property
	
	/**
	 * The test suite that in which to automatically generate mocks.
	 */
	protected JenkinsPipelineSpecification for_spec
	
	/**
	 * Create a stand-in for _property in the Spock _spec
	 * @param _property The "Global Variable" property name to automatically generate mocks for.
	 * @param _spec The test suite that in which to automatically generate mocks.
	 */
	public PipelineVariableImpersonator(String _property, JenkinsPipelineSpecification _spec) {
		for_property = _property
		for_spec = _spec
	}
	
	/**
	 * Intercept a missing method invocation on this "Global Variable" and try calling a mock with that name, instead.
	 * 
	 * @param _name The name of the method that didn't exist
	 * @param _args The arguments to that method
	 * 
	 * @return The results of invoking {@link #for_spec}.getPipelineMock( "{@link #for_property}._name" )( _args )
	 */
	def methodMissing(String _name, _args) {
		
		def prepared_args = _args
		
		if( ! ( prepared_args instanceof Object[] ) ) {
			// this method took one argument. Later on, unwrapping it will be WRONG, so, just pre-wrap it.
			prepared_args = [_args].toArray()
		}
		
		String key = "${for_property}.${_name}"
		
		Closure mock = for_spec.explicitlyMockPipelineStep( key, "(implicit-runtime) getPipelineMock(\"${key}\")" )

		JenkinsPipelineSpecification.LOG_CALL_INTERCEPT(
			"debug",
			"method call on pipeline variable [${for_property}]",
			this,
			"methodMissing",
			_name,
			_args,
			mock,
			"call",
			prepared_args,
			true )
		
		// use the actual closure Mock
		Object result = mock( *prepared_args )
		
		if( _args != null && _args.length >= 1 && _args[_args.length-1] instanceof Closure ) {
			// there was at least one argument, and the last argument was a Closure
			// this almost certainly means that the user's trying to pass the closure to the function as a "body"
			// they probably want it to execute right now.
			_args[_args.length-1]()
		}
		
		return result
	}
	
	def propertyMissing(String _name) {
		return for_spec.getPipelineMock( "${for_property}.getProperty" )( _name )
	}
	
	public String toString() {
		return "Mock Generator for [${for_property}]"
	}
}
