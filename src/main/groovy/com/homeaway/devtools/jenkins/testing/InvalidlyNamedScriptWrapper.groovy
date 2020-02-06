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

import org.codehaus.groovy.runtime.InvokerHelper
 
/**
 * Wraps a {@link Script} whose filename would not be a valid JVM class name.
 * <p>
 * Redirects all {@link Script} interface methods to the wrapped Script, calling them
 * indirectly to prevent their names from causing problems with Java classloaders.
 * </p>
 * 
 * @see <a href='https://issues.apache.org/jira/browse/GROOVY-7670' target='_blank'>GROOVY-7670</a>
 * 
 * @author awitt
 * @since 2.1.1
 */
public class InvalidlyNamedScriptWrapper extends Script {
	
	protected Script script
	
	public InvalidlyNamedScriptWrapper(Script _script) {
		script = _script
	}
	
	@Override
	public Binding getBinding() {
		return script.getBinding()
	}

	@Override
	public void setBinding(Binding _binding) {
		script.setBinding( _binding )
	}

	public Object getProperty(String _property) {
		return script.getProperty( _property )
	}

	public void setProperty(String _property, Object _newValue) {
		script.setProperty( _property, _newValue )
	}
	
	@Override
	public Object run() {
		return InvokerHelper.invokeMethod( script, "run", null )
	}
	
	@Override
	public Object invokeMethod(String _name, Object _args) {
		return InvokerHelper.invokeMethod( script, _name, _args )
	}
}