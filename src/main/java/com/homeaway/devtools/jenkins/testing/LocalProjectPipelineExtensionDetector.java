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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Search through classes in the local project for Jenkins pipeline extensions. Assumes
 * <ol>
 * <li>The local project is built with maven</li>
 * <li>This class is only used during test runs initiated by the <b>maven-surefire-plugin</b></li>
 * </ol>
 * Uses the <a target="_blank" href="https://github.com/ronmamo/reflections">Reflections</a> metadata analysis engine.
 *
 * @author awitt
 *
 */
public class LocalProjectPipelineExtensionDetector extends APipelineExtensionDetector {

	private static final Logger LOG = LoggerFactory.getLogger( LocalProjectPipelineExtensionDetector.class );

	@Override
	public Set<Class<?>> getClassesOfTypeInPackage(Class<?> _supertype, Optional<String> _package) {

		Set<Class<?>> classes = new HashSet<>();

		Map<String, Throwable> failures = new HashMap<>();

		Reflections reflector = new Reflections(
			new ConfigurationBuilder()
			.setScanners(new SubTypesScanner(false))
			.setUrls(ClasspathHelper.forPackage(_package.orElse(""))));

		Set<String> all_types = new HashSet<>();

		try {
			all_types = reflector.getAllTypes();
		} catch( ReflectionsException re ) {
			if( re.getMessage().contains( "Couldn't find subtypes of Object." ) ) {
				// this can happen if there are no classes local to the project.
				// the full error message is
				//     Couldn't find subtypes of Object.
				//     Make sure SubTypesScanner initialized to include Object class - new SubTypesScanner(false)
				// which we have done above, so that is NOT the actual cause.
				// If there are no classes local to the project, that's OK!
				// Just use an empty set instead of throwing an exception.
				LOG.info( "Looks like there aren't any classes compiled by this project." );
			} else {
				// We still do want to error with "real" exceptions, though.
				throw re;
			}
		}

		for(String classname : all_types ) {

			Class<?> clazz = null;

			try {
				clazz = Class.forName( classname );
			} catch( ClassNotFoundException | NoClassDefFoundError e ) {
				failures.put( classname, e );
				continue;
			}

			classes.add( clazz );
		}

		if( failures.size() > 0 ) {
			if( "true".equals( System.getProperty( "PipelineExtensionDetector.expandFailures" ) ) ) {
				for( Entry<String, Throwable> failure : failures.entrySet() ) {
					LOG.error( failure.getKey(), failure.getValue() );
				}
			} else {
				LOG.error(
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

		for( Class<?> annotated_class : new Reflections( _package.orElse("") ).getTypesAnnotatedWith( _annotation ) ) {
			if( _supertype.isAssignableFrom( annotated_class ) ) {
				annotated_classes.add( annotated_class );
			}
		}

		return annotated_classes;
	}

}
