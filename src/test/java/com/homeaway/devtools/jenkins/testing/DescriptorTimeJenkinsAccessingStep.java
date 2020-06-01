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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

import hudson.Extension;
import hudson.model.Item;
import jenkins.model.Jenkins;

/**
 * A step that tries to call {@link Jenkins#getInstanceOrNull()} when its descriptor is instantiated.
 * <p>
 * This means that a Jenkins must exist in order for code to even figure out the name of this step.
 * Any Jenkins-Spock specification should fail with this class on the classpath, unless Jenkins-Spock
 * can successfully handle the situation.
 * </p>
 * <p>
 * This step further tries to interact with a result of a method call on the Jenkins object,
 * if the system property <code>"jenkins.test-access.descriptor</code> is equal to the String <code>true</code>.
 * This allows test cases in jenkins-spock to demonstrate techniques for stubbing interactions with
 * this static Jenkins, while allowing test-cases that aren't related to the static Jenkins to continue
 * without issue.
 * </p>
 * 
 * @author awitt
 * @since 2.1.4
 *
 */
@Extension
public class DescriptorTimeJenkinsAccessingStep extends Step {

	@Extension
	public static class DescriptorImpl extends StepDescriptor {

		public DescriptorImpl() {
			Jenkins maybe_jenkins = Jenkins.getInstanceOrNull();
			assert maybe_jenkins != null;

			maybe_jenkins = Jenkins.get();
			assert maybe_jenkins != null;

			List<Item> items = maybe_jenkins.getAllItems();

			// set this property during tests if you want this class to mimic
			// "problematic" Jenkins extensions and try to actually interact with
			// the Jenkins object before any test specifications run.
			if (System.getProperty("jenkins.test-access.descriptor", "false").equals("true")) {
				// a test suite must have successfully stubbed getAllItems() in order for this
				// assertion to pass
				assert items != null;
			}
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return new HashSet<>();
		}

		@Override
		public String getFunctionName() {
			return "DescriptorTimeJenkinsAccessingStep";
		}

	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		throw new UnsupportedOperationException("Not implemented in test step.");
	}

}
