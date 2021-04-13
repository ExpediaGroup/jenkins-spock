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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.ClassGraph;

/**
 * Search through classes in the entire classpath for Jenkins Pipeline extensions.
 * Uses the <a target="_blank" href="https://github.com/lukehutch/fast-classpath-scanner">FastClasspathScanner</a> metadata analysis engine.
 *
 * @author awitt
 *
 */
public class WholeClasspathPipelineExtensionDetector extends APipelineExtensionDetector {

	private static final Logger LOG = LoggerFactory.getLogger( WholeClasspathPipelineExtensionDetector.class );

	@Override
	public Set<Class<?>> getClassesOfTypeInPackage(Class<?> _supertype, Optional<String> _package) {

		Set<Class<?>> classes = new HashSet<>();

		List<String> classnames = new ClassGraph()
			.enableAnnotationInfo()
			.enableClassInfo()
			.acceptPackages(_package.orElse(""))
			.scan().getAllStandardClasses().getNames();

		HashMap<String, Throwable> failures = new HashMap<>();

		for(String classname: classnames) {

			Class<?> clazz = null;

			try {
				clazz = Class.forName( classname );
			} catch( ClassNotFoundException e ) {
				failures.put( classname, e );
				continue;
			} catch( Throwable t ) {
				// probably BS static initialization; hope you don't need to mock this class...
				failures.put( classname, t );
				continue;
			}

			if( _supertype.isAssignableFrom( clazz ) ) {
				classes.add( clazz );
			}
		}

		if( failures.size() > 0 ) {
			if( "true".equals( System.getProperty( "PipelineExtensionDetector.expandFailures" ) ) ) {
				for( Entry<String, Throwable> failure : failures.entrySet() ) {
					LOG.error( failure.getKey(), failure.getValue() );
				}
			} else {
				LOG.warn(
					"Failed to get some classes of type [{}] in package [{}]. For detailed error messages, set the system property PipelineExtensionDetector.expandFailures=true. Failures: [{}]",
					_supertype,
					_package,
					failures.keySet() );
			}
		}

		return classes;
	}

	@Override
	public Set<Class<?>> getClassesWithAnnotationOfTypeInPackage( Class<? extends Annotation> _annotation, Class<?> _supertype, Optional<String> _package) {

		Set<Class<?>> annotated_classes = new HashSet<>();

		HashMap<String, Throwable> failures = new HashMap<>();

		List<String> annotated_classnames = new ClassGraph()
			.enableAnnotationInfo()
			.enableClassInfo()
			.acceptPackages(_package.orElse(""))
			.scan().getClassesWithAnnotation( _annotation.getName() ).getNames();

		for(String classname: annotated_classnames) {

			Class<?> clazz = null;

			try {
				clazz = Class.forName( classname );
			} catch( ClassNotFoundException e ) {
				failures.put( classname, e );
				continue;
			} catch( Throwable t ) {
				// probably BS static initialization; hope you don't need to mock this class...
				failures.put( classname, t );
				continue;
			}

			if( _supertype.isAssignableFrom( clazz ) ) {
				annotated_classes.add( clazz );
			}
		}

		if( failures.size() > 0 ) {
			if( "true".equals( System.getProperty( "PipelineExtensionDetector.expandFailures" ) ) ) {
				for( Entry<String, Throwable> failure : failures.entrySet() ) {
					LOG.error( failure.getKey(), failure.getValue() );
				}
			} else {
				LOG.warn(
					"Failed to get some classes annotated with [{}] of type [{}] in package [{}]. For detailed error messages, set the system property PipelineExtensionDetector.expandFailures=true. Failures: [{}]",
					_annotation,
					_supertype,
					_package,
					failures.keySet() );
			}
		}

		return annotated_classes;
	}
}
