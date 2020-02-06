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

import groovy.lang.Script;

/**
 * Enables calling methods on Groovy scripts whose names form invalid JVM classes.
 * <p>
 * Groovy scripts don't explicitly compile to classes, so their names do not have to follow JVM class naming rules.
 * As described in <a target='_blank' href='https://issues.apache.org/jira/browse/GROOVY-7670'>GROOVY-7670</a>,
 * this becomes a problem in some cases when a JVM tries to load a {@link Script} that was created based on such
 * a file, as the generated class uses the file name.
 * 
 * @see <a target='_blank' href='https://issues.apache.org/jira/browse/GROOVY-7670'>GROOVY-7670</a>
 * 
 * @author awitt
 * @since 2.1.1
 *
 */
public class InvalidlyNamedScriptInvoker {
	
	public static Object run(Script _script) {
		return _script.run();
	}
	
	public static Object invokeMethod(Script _script, String _method, Object... args) {
		return _script.invokeMethod(_method, args);
	}

}
