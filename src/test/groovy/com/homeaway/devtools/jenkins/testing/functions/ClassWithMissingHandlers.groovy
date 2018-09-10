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

package com.homeaway.devtools.jenkins.testing.functions

/**
 * A regular Groovy class that also implements <code>methodMissing</code> and <code>propertyMissing</code> handlers.
 * 
 * @author awitt
 *
 */
class ClassWithMissingHandlers {
	
	public Map helloNode(String _label, Closure _body) {
		return node( _label ) {
			echo( "Hello from a [${_label}] node!" )
			_body()
			echo( "Goodbye from a [${_label}] node!" )
		}
	}
	
	def methodMissing(String _name, _args) {

		if( _name == "sh" ) {
			return "myCustomShReturnValue"
		}
		
		throw new MissingMethodException( _name, getClass(), _args )
	}
	
	def propertyMissing(String _name) {
		
		if( _name == "dynamicProperty" ) {
			return "myCustomDynamicPropertyValue"
		}
		
		throw new MissingPropertyException( _name, getClass() )
	}
}
