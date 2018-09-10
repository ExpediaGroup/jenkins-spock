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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.Extension;

/**
 * Utility class for detecting extension points to Jenkins' Pipeline API.
 *
 * @author awitt
 *
 */
public abstract class APipelineExtensionDetector {

	private static final Logger LOG = LoggerFactory.getLogger( APipelineExtensionDetector.class );

	/**
	 * Find classes of the provided _supertype in the provided _package tree.
	 *
	 * @param _supertype The type of class to look for.
	 * @param _package The package tree to look in
	 *
	 * @return classes of the provided _supertype in the provided _package tree.
	 */
	public abstract Set<Class<?>> getClassesOfTypeInPackage(Class<?> _supertype, Optional<String> _package);

	/**
	 * Find classes annotated with the given _annotation that are subtypes of the provided _supertype, in the provided _package tree.
	 *
	 * @param _annotation The type of annotation to look for.
	 * @param _supertype The type of class to look for.
	 * @param _package The package tree to look in
	 *
	 * @return classes annotated with the given _annotation that are subtypes of the provided _supertype, in the provided _package tree.
	 */
	public abstract Set<Class<?>> getClassesWithAnnotationOfTypeInPackage(Class<? extends Annotation> _annotation, Class<?> _supertype, Optional<String> _package);

	/**
	 * Find all of the {@link StepDescriptor} extensions in the _package.
	 *
	 * @param _package The package to look in for {@link StepDescriptor} extensions.
	 *
	 * @return all of the {@link StepDescriptor} extensions in the _package.
	 */
	public Set<String> getPipelineSteps(Optional<String> _package) {

		Set<String> names = new HashSet<>();

		Map<String, Throwable> failures = new HashMap<>();

		for( Class<?> step_descriptor_class : getClassesWithAnnotationOfTypeInPackage(Extension.class, StepDescriptor.class, _package ) ) {
			try {
				StepDescriptor descriptor = (StepDescriptor) step_descriptor_class.newInstance();

				names.add( descriptor.getFunctionName() );
			} catch( InstantiationException e ) {
				failures.put( step_descriptor_class.getName(), e );
				continue;
			} catch( IllegalAccessException e ) {
				failures.put( step_descriptor_class.getName(), e );
				continue;
			}
		}

		if( failures.size() > 0 ) {
			if( "true".equals( System.getProperty( "PipelineExtensionDetector.expandFailures" ) ) ) {
				for( Entry<String, Throwable> failure : failures.entrySet() ) {
					LOG.error( failure.getKey(), failure.getValue() );
				}
			} else {
				LOG.error(
					"Failed to get the function names of the following StepDescriptor classes. For detailed error messages, set the system property PipelineExtensionDetector.expandFailures=true. Failures: [{}]",
					failures.keySet() );
			}
		}

		return names;
	}

	/**
	 * Find all of the {@link GlobalVariable} extensions in the _package.
	 *
	 * @param _package The package to look in for {@link GlobalVariable} extensions.
	 *
	 * @return all of the {@link GlobalVariable} extensions in the _package.
	 */
	public Set<String> getGlobalVariables(Optional<String> _package) {

		Set<String> names = new HashSet<>();

		Map<String, Throwable> failures = new HashMap<>();

		for( Class<?> global_variable_class : getClassesWithAnnotationOfTypeInPackage(Extension.class, GlobalVariable.class, _package ) ) {
			try {
				GlobalVariable variable = (GlobalVariable) global_variable_class.newInstance();

				names.add( variable.getName() );
			} catch( InstantiationException e ) {
				failures.put( global_variable_class.getName(), e );
				continue;
			} catch( IllegalAccessException e ) {
				failures.put( global_variable_class.getName(), e );
				continue;
			}
		}

		if( failures.size() > 0 ) {
			if( "true".equals( System.getProperty( "PipelineExtensionDetector.expandFailures" ) ) ) {
				for( Entry<String, Throwable> failure : failures.entrySet() ) {
					LOG.error( failure.getKey(), failure.getValue() );
				}
			} else {
				LOG.error(
					"Failed to get the names of the following GlobalVariable classes. For detailed error messages, set the system property PipelineExtensionDetector.expandFailures=true. Failures: [{}]",
					failures.keySet() );
			}
		}

		return names;
	}

	/**
	 * Find all of the {@link Symbol} extensions in the _package.
	 *
	 * @param _package The package to look in for {@link Symbol} extensions.
	 *
	 * @return all of the {@link Symbol} extensions in the _package.
	 */
	public Set<String> getSymbols(Optional<String> _package) {

		Set<String> names = new HashSet<>();

		for( Class<?> symbol_class : getClassesWithAnnotationOfTypeInPackage( Symbol.class, Object.class, _package ) ) {

			Symbol symbol = symbol_class.getAnnotation( Symbol.class );
			names.addAll( Arrays.asList( symbol.value() ) );

		}

		return names;
	}

	public Set<String> getPipelineExtensions(Optional<String> _package) {

		Set<String> names = new HashSet<>();

		names.addAll( getPipelineSteps( _package ) );
		names.addAll( getGlobalVariables( _package ) );
		names.addAll( getSymbols( _package ) );

		return names;
	}

}
