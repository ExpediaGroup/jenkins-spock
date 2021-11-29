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
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import groovy.lang.Closure;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
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

		List<String> classnames;
		try (
			ScanResult scanResult = new ClassGraph()
				.acceptPackages(_package.orElse(""))
				.disableJarScanning()
				.scan()
		) {
			classnames = scanResult
				.getAllClasses()
				.filter(ci -> !ci.extendsSuperclass(Closure.class.getName()))
				.filter(ci -> !ci.extendsSuperclass("spock.lang.Specification"))
			 	.filter(ci -> !(ci.extendsSuperclass(InvalidlyNamedScriptWrapper.class.getName()) || ci.getName().equals(InvalidlyNamedScriptWrapper.class.getName())))
				.filter(ci -> ci.extendsSuperclass(_supertype.getName()))
				.getNames();
		}

		Set<Class<?>> classes = new HashSet<>();
		Map<String, Throwable> failures = new HashMap<>();
		for(String classname : classnames ) {
			Class<?> clazz;

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

		Set<Class<?>> annotated_classes;
		try (
			ScanResult scanResult = new ClassGraph()
				.acceptPackages(_package.orElse(""))
				.enableAnnotationInfo()
				.scan()
		) {
			annotated_classes = new HashSet<>(scanResult
				.getClassesWithAnnotation(_annotation.getName())
				.filter(ci -> ci.extendsSuperclass(_supertype.getName()))
				.loadClasses(true));
		}

		return annotated_classes;
	}

}
