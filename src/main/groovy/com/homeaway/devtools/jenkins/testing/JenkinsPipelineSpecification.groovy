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

import org.jenkinsci.plugins.workflow.cps.CpsScript
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import hudson.Extension
import hudson.ExtensionList
import jenkins.model.Jenkins
import jenkins.model.Jenkins.JenkinsHolder
import spock.lang.Specification

/**
 * Parent for Spock specs that test Jenkins pipeline code.
 * <p>
 * Such specifications need all of the Pipeline symbols that might exist at runtime (e.g. node, sh, echo, ws, stash, etc) to exist, or else
 * the pipeline code won't run.
 * </p>
 * <p>
 * This class ensures that all pipeline extension points exist as Spock Mock objects so that the calls will succeed and that interactions
 * can be inspected, stubbed, and verified. Just extend this instead of the regular {@link Specification} and test your pipeline scripts.
 * You can access a Spock mock for any pipeline step that would exist by using {@link #getPipelineMock(String)}.
 * </p>
 * <ol>
 * <li><a href="#testing-groovy-functions">Testing Groovy Functions</a></li>
 * <li><a href="#testing-pipeline-scripts">Testing Pipeline Scripts</a></li>
 * <li><a href="#mock-pipeline-steps">Mock Pipeline Steps</a></li>
 * <li><a href="#mock-pipeline-variables">Mock Pipeline Variables</a></li>
 * <li><a href="#mock-shared-library-variables">Mock Shared Library Variables</a></li>
 * <li><a href="#preparing-additional-objects">Preparing Additional Objects</a></li>
 * <li><a href="#mocking-additional-pipeline-steps">Mocking Additional Pipeline Steps</a></li>
 * <li><a href="#mocking-additional-pipeline-variables">Mocking Additional Pipeline Variables</a></li>
 * <li><a href="#mock-jenkins">Mock Jenkins</a></li>
 * <li><a href="#mock-pipeline-execution">Mock Pipeline Execution</a></li>
 * <li><a href="#metaprogramming">Metaprogramming</a></li>
 * <li><a href="#custom-classpath">Define Custom Classpath For Script Loading</a></li>
 * </ol>
 * <a name="testing-groovy-functions"></a>
 * <h1>Testing Groovy Functions</h1>
 * <p>
 * For example, the following Groovy class:
 * </p>
 * <code><pre>
class MyJenkinsPipelineHelper {
	public Map helloNode(String _label, Closure _body) {
		return node( _label ) {
			echo( "Hello from a [${_label}] node!" )
			_body()
			echo( "Goodbye from a [${_label}] node!" )
		}
	}
}
 * </code></pre>
 * <p>
 * can be tested like this:
 * </p>
 * <code><pre>
public class MyJenkinsPipelineHelperSpec extends JenkinsPipelineSpecification {
	def "helloNode" () {
		given:
			MyJenkinsPipelineHelper myVar = new MyJenkinsPipelineHelper()
		when:
			myVar.helloNode( "nodeType" ) {
				echo( "inside node" )
			}
		then:
			1 * getPipelineMock( "node" )("nodeType", _)
			1 * getPipelineMock( "echo" )("Hello from a [nodeType] node!")
			1 * getPipelineMock( "echo" )("Goodbye from a [nodeType] node!")
			1 * getPipelineMock( "echo" )("inside node")
	}
}
 * </pre></code>
 * 
 * <a name="testing-pipeline-scripts"></a>
 * <h1>Testing Pipeline Scripts</h1>
 * <p>
 * {@link #loadPipelineScriptForTest(java.lang.String)}
 * </p>
 * <p>
 * If you have pipeline scripts (i.e. whole Jenkinsfiles) in <b>src/main/resources</b>,
 * you can load them as {@link Script}s and run them during unit tests with {@link #loadPipelineScriptForTest(java.lang.String)}.
 * Just call {@link Script#run()} on the returned object, e.g.
 * </p>
 * <b>CoolJenkinsfile.groovy</b>
 * <pre><code>
node( "legacy" ) {
	echo( "hello world" )
}
 * </code></pre>
 * <b>CoolJenkinsfileSpec.groovy</b>
 * <pre><code>
def "Jenkinsfile"() {
	setup:
		def Jenkinsfile = loadPipelineScriptForTest("com/homeaway/CoolJenkinsfile.groovy")
	when:
		Jenkinsfile.run()
	then:
		1 * getPipelineMock("node")("legacy", _)
		1 * getPipelineMock("echo")("hello world")
}
 * </code></pre>
 * 
 * <a name="mock-pipeline-steps"></a>
 * <h1>Mock Pipeline Steps</h1>
 * <p>
 * Mock pipeline steps are available at <code>getPipelineMock("stepName")</code>.
 * You can verify interactions with them and stub them:
 * </p>
 * <pre><code>
...
then:
	1 * getPipelineMock( "echo" )( "hello world" )
	1 * getPipelineMock( "sh" )( [returnStdout: true, script: "echo hi"] ) >> "hi"
...
 * </code></pre>
 * 
 * <h2>Argument Capture</h2>
 * 
 * <p>
 * A quirk of Groovy's implementation of Closures and the choice of Closures as the interface
 * to mock to provide pipeline step mocks changes Spock's argument-capture idiom:
 * When a pipeline step is called with more than one argument, captured arguments
 * are wrapped in an extra layer of <code>Object[]</code> and must be unwrapped.
 * </p>
 * <pre><code>
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
 * </code></pre>
 * 
 * <a name="mock-pipeline-variables"></a>
 * <h1>Mock Pipeline Variables</h1>
 * <p>
 * Method calls on variables are available as mocks at <code>getPipelineMock("VariableName.methodName")</code>.
 * Because it can be impossible to tell which methods will be valid on a given variable at runtime <b>all method calls on variables are allowed during tests</b> and captured by a Spock Mock. 
 * </p>
 * <p>
 * You can expect and stub interactions with these mocks normally:
 * </p>
 * <pre><code>
...
then:
	1 * getPipelineMock( "docker.inside" )( "maven:latest" )
	1 * getPipelineMock( "JenkinsPluginSapling.hello" )( [who: "bob", greeting: "hi"] ) >> "hi bob" 
...
 * </code></pre>
 * <p>
 * <b>Implementation Note:</b> If you access <code>getPipelineMock("VariableName")</code> you will find a {@link PipelineVariableImpersonator} that will create mocks on-the-fly (if necessary) for every method called on it.
 * You shouldn't need to interact directly with these objects.
 * </p>
 * 
 * <h2>Properties of Mock Pipeline Variables</h2>
 * <p>
 * All property access attempts on pipeline variables will be forwarded to <code>getPipelineMock("VariableName.getProperty")(propertyName)</code>.
 * You can expect and stub these accesses normally:
 * </p>
 * <pre><code>
...
when:
	String someEnvvar = env.someEnvvar
	String otherEnvvar = env.otherEnvvar
then:
	1 * getPipelineMock( "env.getProperty" )( "someEnvvar" )
	1 * getPipelineMock( "env.getProperty" )( "otherEnvvar" ) >> "expected"
expect:
	"expected" == otherEnvvar
...
 * </code></pre>
 * <p>
 * <b>Exception:</b> You cannot stub <code>.toString()</code> of the Mock Pipeline Variable itself; <code>getPipelineMock("someVar.toString") >> "hello"</code> won't work,
 * meaning if you've written <code>String s = "${someVar} something"</code> in your code-under-test, that can't be correctly stubbed.
 * The immediate cause is that a useful {@link PipelineVariableImpersonator#toString()} is already defined on {@link PipelineVariableImpersonator}, for understanding its presence
 * in log messages. However, due to <a target="_blank" href="https://issues.apache.org/jira/browse/GROOVY-3493">GROOVY-3493</a>, we can't "fix" this with metaprogramming at test-time either.
 * </p>
 * 
 * <h2>Defining Pipeline Variables</h2>
 * <p>
 * Not every pipeline variable needs to be mocked. Sometimes you might just need to define a dummy value.
 * You can just do this in your test class if that's where you use the variable.
 * If the variable is used in a pipeline script that's being tested (as may be the case for variables that Jenkins automatically sets for you),
 * set the variable on the script's {@link Binding}: 
 * </p>
 * <pre><code>
setup:
	def Jenkinsfile = loadPipelineScriptForTest("Jenkinsfile")
	Jenkinsfile.getBinding().setVariable( "BRANCH_NAME", "master" )
 * </code></pre>
 * 
 * <a name="mock-shared-library-variables"></a>
 * <h1>Mock Shared Library Variables</h1>
 * <p>
 * Any Groovy scripts on the classpath at <code>/vars</code> will be treated as Pipeline Shared Library Global Variables, and mocked as described above.
 * </p>
 * <h2>Unit-Testing PSL Variables</h2>
 * <p>
 * Pipeline Shared Library Global Variables are just pipeline scripts on the classpath, so they can be unit-tested with {@link #loadPipelineScriptForTest(java.lang.String)}:
 * </p>
 * <pre><code>
setup:
	def MyFunction = loadPipelineScriptForTest("vars/MyFunction.groovy")
when:
	MyFunction("...")
then:
	1 * getPipelineMock("echo")("Hello World")
 * </code></pre>
 * <h2>PSL Variable Mocks</h2>
 * <p>
 * The most common way to use Pipeline Shared Library Global Variables is to implicitly <code>.call(...)</code> them, e.g. 
 * </p>
 * <pre><code>
stage( "Do Something" )  {
	MyFunction("...")
}
 * </code></pre>
 * <p>
 * To expect or stub that interaction, use <code>getPipelineMock("FileName.call")(...)</code>, e.g.
 * </p>
 * <b>Stubbing</b>
 * <pre><code>
setup:
	getPipelineMock("MyFunction.call")("Hello") >> "Hello World"
when:
	Jenkinsfile.run()
then:
	1 * getPipelineMock("echo")("Hello World")
 * </code></pre>
 * <b>Expecting</b>
 * <pre><code>
when:
	Jenkinsfile.run()
then:
	1 * getPipelineMock("MyFunction.call")(_)
 * </code></pre>
 * 
 * <a name="preparing-additional-objects"></a>
 * <h1>Preparing Additional Objects</h1>
 * <p>
 * By default, all non-interface classes in your project's source directories are instrumented
 * to be able to call pipeline steps during each Specification test suite (see {@link #setupSpec()} and {@link #setup()}),
 * along with the class that the specification is for (determined by class name, e.g. <code>MyClassSpec</code> is "for" <code>MyClass</code>).
 * If you need to instrument additional objects, you can use {@link #addPipelineMocksToObjects(java.lang.Object)}.
 * If you provide a {@link Class} object, all subsequent instances of that class will be instrumented.
 * </p>
 * 
 * <p>
 * <b>Warning:</b> Attempting to instrument an Interface class may have frustratingly permanent side-effects. Do not do that. Instead, read <a target="_blank" href="https://issues.apache.org/jira/browse/GROOVY-3493">GROOVY-3493</a>.
 * </p>
 * 
 * <a name="mocking-additional-pipeline-steps"></a>
 * <h1>Mocking Additional Pipeline Steps</h1>
 * <p>
 * {@link #explicitlyMockPipelineStep(java.lang.String,java.lang.String)}
 * </p>
 * <p>
 * If for some reason you need to mock a pipeline step that does not come from a detectable plugin, you can use
 * {@link #explicitlyMockPipelineStep(java.lang.String,java.lang.String)} to add the ability to call that pipeline step and get a Mock object
 * to all currently-instrumented classes and objects.
 * </p>
 *
 * <a name="mocking-additional-pipeline-variables"></a>
 * <h1>Mocking Additional Pipeline Variables</h1>
 * <p>
 * {@link #explicitlyMockPipelineVariable(java.lang.String)}
 * </p>
 * <p>
 * If for some reason you need to capture interactions with a variable that is not detected on the classpath and automatically mocked, you can use
 * {@link #explicitlyMockPipelineVariable(java.lang.String)} to create and return a {@link PipelineVariableImpersonator} that will intercept all subsequent interactions with that variable
 * and forward them to appropriately-named Spock mocks, creating the mocks on-the-fly if necessary.
 * </p>
 * 
 * <a name="mock-jenkins"></a>
 * <h1>Mock Jenkins</h1>
 * There is a Spock Mock of type {@link Jenkins} available at <code>getPipelineMock("Jenkins")</code>.
 * It is set up as follows:
 * <ol>
 * <li>{@link Jenkins#HOLDER} is implemented to return the mock {@link Jenkins}.</li>
 * <li>{@link Jenkins#getInstance()} is stubbed to return the mock {@link Jenkins}</li>
 * <li>{@link Jenkins#getInstanceOrNull()} is stubbed to return the mock {@link Jenkins}</li>
 * <li>{@link Jenkins#getExtensionList(Class)} is stubbed to return instances of the {@literal @}{@link Extension}s of the provided type (actually, only the {@link ExtensionList#iterator()} method is stubbed, so use a for/each loop).</li>
 * </ol>
 * 
 * <a name="mock-pipeline-execution"></a>
 * <h1>Mock Pipeline Execution</h1>
 * <p>
 * A Mock of the pipeline execution's {@link Binding} will be available at <code>getPipelineMock("getBinding")</code>.
 * Code-under-test might access this mock by calling {@link CpsScript#getBinding()}.
 * Usually, this is done by {@link GlobalVariable} implementations.
 * </p>
 * <p>
 * There is a Spock Spy of type {@link CpsScript} available at <code>getPipelineMock("CpsScript")</code>. 
 * It represents the cps-transformed execution that would exist if Jenkins were running your code for real.
 * You should never have to interact directly with this object.
 * </p>
 * <p>
 * <b>Architecture Note:</b> {@link CpsScript} overrides {@link CpsScript#invokeMethod(String, Object)} and tries to invoke a CPS-transformed equivalent.
 * Spock <i>mocks</i> won't override this particular method, and so any method invocations on a <i>mock</i> <code>CpsScript</code>
 * will result in the real <code>invokeMethod</code> being called and CPS-transformed execution attempted...
 * But during unit tests, we haven't CPS-transformed anything so this will fail unless we manually override <code>invokeMethod</code>
 * on a "real" CpsScript object and redirect those method calls back to some "normal" {@link GroovyObject#invokeMethod(String, Object)}.
 * </p>
 * 
 * <a name="metaprogramming"></a>
 * <h1>Metaprogramming</h1>
 * <p>
 * <a target="_blank" href="">Groovy's metaprogramming capabilities</a> are used heavily to make this class work.
 * A <code>methodMissing</code> and <code>propertyMissing</code> method is dynamically created for every class that needs
 * to be able to call pipeline steps. What happens if that class already had one of those methods defined? Trouble, that's what.
 * </p>
 * <p>
 * This class will detect such a situation, and call the other class' appropriate "missing" handler first.
 * If that handler throws either a {@link MissingMethodException} or {@link MissingPropertyException}, then this class
 * will proceed with the regular "maybe it was a pipeline step" logic. However, a class that "chomps" all missing events, like so:
 * </p>
 * <pre><code>
def methodMissing(String _name, _args) {
	LOG.warn( "Called a missing method: ${_name}(${_args.toString()})" )
}
 * </code></pre>
 * <p>	
 * simply cannot be enabled to call pipeline steps during tests. Avoid writing classes like that.
 * </p>
 * 
 * <a name="custom-classpath"></a>
 * <h1>Define Custom Classpath For Script Loading</h1>
 *
 * When your project defined custom source sets you need to set the classpath for the loading of your scripts manually. 
 * E.g. when you want to load your custom pipeline script that is located in `test/resources` you can do:
 *
 * <pre><code> 
class PipelineTest extends JenkinsPipelineSpecification {*
	def setup() {
		scriptClassPath = ["test/resources"] //Note that this is a collection and you can define multiple paths.
	}

	def "Some test) {
		def pipeline = loadPipelineScriptForTest("Jenkinsfile") // Will test/resources/Jenkinsfile
		[...]
	}
}
 * </code></pre>
 *
 * The default paths are:
 * <ul>
 * <li>src/main/resources</li>
 * <li>src/test/resources</li>
 * <li>target/classes</li>
 * <li>target/test-classes</li>
 * </ul>
 * 
 * @author awitt
 * @author mld-ger
 *
 */
public abstract class JenkinsPipelineSpecification extends Specification {
	
	protected static final Logger LOG = LoggerFactory.getLogger( JenkinsPipelineSpecification.class )
	
	/**
	 * All pipeline symbols ({@link org.jenkinsci.Symbol Symbol} and {@link org.jenkinsci.plugins.workflow.cps.GlobalVariable GlobalVariable}) that exist from classes on the classpath.
	 * This may include Pipeline Shared Library global variables, if the project being tested is a Pipeline Shared Library.
	 * <p>
	 * You could modify this in your specification's <code>setupSpec</code>
	 * to change the symbols available during the test suite.
	 * </p>
	 */
	protected static Set<String> PIPELINE_SYMBOLS = new HashSet<>()
	
	/**
	 * All pipeline steps ({@link org.jenkinsci.plugins.workflow.steps.StepDescriptor#getFunctionName() StepDescriptor}s) that exist from classes on the classpath.
	 * <p>
	 * You could modify this in your specification's <code>setupSpec</code>
	 * to change the pipeline steps available during the test suite.
	 * </p>
	 */
	protected static Set<String> PIPELINE_STEPS = new HashSet<>()
	
	/**
	 * All of the classes that should be instrumented to be able to access mocks of pipeline extensions.
	 * <p>
	 * You could modify this in your specification's <code>setupSpec</code>
	 * to change the classes that were instrumented to be able to call pipeline extensions during a test suite.
	 * </p> 
	 * @see #setupSpec()
	 */
	protected static Set<Class<?>> DEFAULT_TEST_CLASSES = new HashSet<>()
	
	/**
	 * Additional pipeline steps that have been dynamically mocked during a test fixture.
	 * <p>
	 * Usually these correspond to method invocations on Global Variables.
	 * </p>
	 * <p>
	 * This is part of the internal workings of {@link JenkinsPipelineSpecification} and probably should not be modified by test suites.
	 * </p>
	 */
	protected Set<String> pipeline_steps = new HashSet<>()
	
	/**
	 * Additional pipeline symbols that have been dynamically mocked during a test fixture.
	 * <p>
	 * Usually these correspond to Global Variables.
	 * </p>
	 * <p>
	 * This is part of the internal workings of {@link JenkinsPipelineSpecification} and probably should not be modified by test suites.
	 * </p>
	 */
	protected Set<String> pipeline_symbols = new HashSet<>()
	
	/**
	 * The mock objects created for this test suite.
	 * <p>
	 * This is part of the internal workings of {@link JenkinsPipelineSpecification} and probably should not be modified by test suites.
	 * </p>
	 * 
	 * @see #getPipelineMock(String)
	 */
	protected Map<String, Object> mocks = new HashMap<>()
	
	/**
	 * An internal record of objects that have bee instrumented to call the {@link #mocks} when one of the pipeline mocks are invoked.
	 * <p>
	 * This list is purely for debugging and optimization purposes; modifying it does not affect the behavior of the test suite.
	 * </p>
	 */
	protected Set<Class<?>> instrumented_objects = new HashSet<>()
	
	/**
	 * In case {@link Jenkins#getExtensionList(Class)} is called, matching {@literal @}{@link Extension}s are created.
	 * <p>
	 * Subsequent calls to that method should return the exact same objects (as would happen in real Jenkins)
	 * so the objects must be cached test-suite-wide for re-use.
	 * </p> 
	 * <p>
	 * This is part of the internal workings of {@link JenkinsPipelineSpecification} and probably should not be modified by test suites.
	 * </p>
	 */
	protected Map<Class<?>,Object> dummy_extension_instances = new HashMap<>()

	/**
	 * The defined paths will be checked to load the script given in
	 * {@link JenkinsPipelineSpecification#loadPipelineScriptForTest(java.lang.String)}. You can add or override this
	 * path in case you specified custom source sets in your project.
	 */
	protected String[] script_class_path = ["src/main/resources", // if it's a main resource
											"src/test/resources", // if it's a test resource
											"target/classes", // if it's on the main classpath
											"target/test-classes"] // if it's on the test classpath

	/**
	 * Add Spock Mock objects for each of the pipeline extensions to each of the _objects.
	 * <p>
	 * Afterwards, those _objects will be able to call <code>PIPELINE_EXTENSION_NAME()</code> and this call will not only succeed,
	 * but also register an interaction with the appropriate {@link #mocks}.
	 * <p>
	 *
	 * @param _objects The objects to enable to call pipeline extensions.
	 */
	protected void addPipelineMocksToObjects(Object... _objects) {
		
		_objects.each { object ->
			
			final MetaMethod originalMethodMissing = object.metaClass.getMetaMethod("methodMissing", "string", new Object[0] )
			
			object.metaClass.methodMissing = { String _name, _args ->  
				
				// a method was called that didn't exist and for which there was no mock.
				// The test code is calling a method that won't exist with the given dependency spec, and that hasn't been explicitly mocked.
				if( null != originalMethodMissing ) {

					LOG_CALL_INTERCEPT(
						"debug",
						"pipeline step - but receiver had its own methodMissing",
						object,
						"methodMissing",
						_name,
						_args,
						delegate,
						"methodMissing.${_name}",
						_args,
						false )

					try {
						return originalMethodMissing.invoke( delegate, _name, _args )
					} catch( MissingMethodException mme ) {
						// ok, they can't handle it.
						LOG.debug(
							"{}.methodMissing({}, {}) threw a MissingMethodException - checking if it's a mocked pipeline step.",
							object,
							_name,
							_args.toString() )
					}
				}
				
				if( PIPELINE_STEPS.contains( _name ) || pipeline_steps.contains( _name ) ) {
					
					LOG_CALL_INTERCEPT(
						"debug",
						"pipeline step",
						object,
						"methodMissing",
						_name,
						_args,
						mocks.get( _name ),
						"call",
						_args,
						true )
					
					/*
					 * `*_args`? 
					 * https://stackoverflow.com/a/14453937/2891339
					 * It turns an Iterable back into varargs for a method call
					 *  
					 * You must do this from within invokeMethod or methodMissing when forwarding arguments to 
					 * something that might be an actual method, so the Groovy runtime will be able to match
					 * the call to the appropriate receiver.
					 */
					Object result = mocks.get( _name )( *_args )
					
					if( _args != null && _args.length >= 1 && _args[_args.length-1] instanceof Closure ) {
						// there was at least one argument, and the last argument was a Closure
						// this almost certainly means that the user's trying to pass the closure to the function as a "body"
						// they probably want it to execute right now.
						_args[_args.length-1]()
					}
					
					return result
				} else if( PIPELINE_SYMBOLS.contains( _name ) || pipeline_symbols.contains( _name ) ) {
					// It's a symbol, being called.

					// *_args? See comment above.
					Object result = mocks.get( _name )( *_args )
					
					return result
				}
				
				MissingMethodException mme = new MissingMethodException( "(intercepted on instance [${object}] during test [${this}]) ${_name}", delegate.getClass(), _args )
				throw new IllegalStateException( "During a test, the pipeline step [${_name}] was called but there was no mock for it.\n\t1. Is the name correct?\n\t2. Does the pipeline step have a descriptor with that name?\n\t3. Does that step come from a plugin? If so, is that plugin listed as a dependency in your pom.xml?\n\t4. If not, you may need to call explicitlyMockPipelineStep('${_name}') in your test's setup: block.", mme )
			}
			
			def originalPropertyMissing = object.metaClass.getMetaMethod("propertyMissing", "string" )
			
			object.metaClass.propertyMissing = { String _name ->
				
				// we get here when accessing a variable that doesn't exist.
				if( null != originalPropertyMissing ) {

					LOG_CALL_INTERCEPT(
						"debug", 
						"pipeline variable - but receiver had its own propertyMissing",
						object,
						"propertyMissing",
						"getProperty",
						_name,
						delegate,
						"propertyMissing",
						_name,
						false )
					
					try {
						return originalPropertyMissing.invoke( delegate, _name )
					} catch( MissingPropertyException mpe ) {
						// ok, they can't handle it.
						LOG.debug(
							"{}.propertyMissing({}) threw a MissingPropertyException - checking if it's a mocked pipeline variable.",
							object,
							_name )
					}
				}
				
				if( PIPELINE_SYMBOLS.contains( _name ) || pipeline_symbols.contains( _name ) ) {
					
					PipelineVariableImpersonator impersonator = explicitlyMockPipelineVariable( _name )
					
					LOG.debug(
						"Intercepted pipeline variable access of [{}] from an INSTANCE, returning a PipelineVariableImpersonator ({})",
						_name,
						impersonator )
					
					return impersonator
				}
				
				MissingPropertyException mpe = new MissingPropertyException( "(intercepted on instance [${object}] during test [${this}]) ${_name}", object.getClass() )
				throw new IllegalStateException( "There is no pipeline variable mock for [${_name}].\n\t1. Is the name correct?\n\t2. Is it a GlobalVariable extension point? If so, does the getName() method return [${_name}]?\n\t3. Is that variable normally defined by Jenkins? If so, you may need to define it by hand in your Spec.\n\t4. Does that variable come from a plugin? If so, is that plugin listed as a dependency in your pom.xml?\n\t5. If not, you may need to call explicitlyMockPipelineVariable(\"${_name}\") during your test setup.", mpe )
			}
			
			instrumented_objects.add( object )
		}
	}
	
	/**
	 * Retrieve the Spock Mock object for the given _pipeline_extension for the current test suite.
	 * 
	 * @param _pipeline_extension The pipeline extension to retrieve the mock object for.
	 * 
	 * @return the Spock Mock object for the given _pipeline_extension for the current test suite.
	 */
	protected Object getPipelineMock(String _pipeline_extension) {
		
		if( ! mocks.containsKey( _pipeline_extension ) ) {
			
			/*
			 * It's possible that the access is to a method on a mock property, e.g. docker.inside(...)
			 * If getPipelineMock("docker.inside") is called in the "then" block of a fixture,
			 * it will be evaluated before test code runs and automatically creates the mock.
			 * 
			 * We could either 
			 *     tell users that all accesses to global variables must be manually set up with explicitlyMockPipelineStep("docker.inside") ahead-of-time,
			 * or
			 *    automatically create the mock that the user is going to expect.
			 * 
			 * One of those is clearly a better user experience.
			 * 
			 * It's also possible that there is in fact no mock... let's check.
			 */
			String[] parts = _pipeline_extension.split("\\.")
			if( parts.length == 2 ) {
				
				// Yup, it's a property method invocation!
				
				String property = parts[0]
				String method = parts[1]
				
				PipelineVariableImpersonator pam = mocks.get( property )
				
				if( null != pam ) {
					// and there _is_ a mock for that global variable, so, we should automatically mock this method invocation.
					Closure implicitMock = Mock( name: "(implicit-expected) getPipelineMock(\"${_pipeline_extension}\")".toString() )
					mocks.put( _pipeline_extension, implicitMock )
					return implicitMock
				}
			}
			
			throw new IllegalStateException( "There is no pipeline step mock for [${_pipeline_extension}].\n\t1. Is the name correct?\n\t2. Does the pipeline step have a descriptor with that name?\n\t3. Does that step come from a plugin? If so, is that plugin listed as a dependency in your pom.xml?\n\t4. If not, you may need to call explicitlyMockPipelineStep('${_pipeline_extension}') in your test's setup: block." )
		}
		
		return mocks.get( _pipeline_extension )
	}
	
	/**
	 * Explicitly mock a pipeline step that doesn't come from an annotated {@link Extension}.
	 * <p>
	 * If your code-under-test calls methods on {@link CpsScript} directly (e.g. {@link CpsScript#getBinding}), those methods won't be mocked by default since they aren't {@link Extension}s.
	 * Using this, you can mock those methods. This method is idempotent and will return the same mock every time it is called.
	 * </p>
	 * For example, to mock {@link CpsScript#getBinding()}().{@link groovy.lang.Binding#hasVariable(java.lang.String) hasVariable}() to always return false,
	 * <pre><code>
	 * Closure mock_get_binding = explicitlyMockPipelineStep( "getBinding" )
	 * Binding mock_binding = Mock()
	 * mock_get_binding() >> { mock_binding }
	 * mock_binding.hasVariable(_) >> { return false }
	 * </code></pre>
	 * <p>
	 * If you need this because your code-under-test relies on a regular pipeline step and that step wasn't automatically mocked, this method is <i>not</i> the solution.
	 * You should either
	 * </p>
	 * <ol>
	 * <li>Add a dependency to your pom.xml that brings in the plugin that provides the necessary pipeline step</li>
	 * <li>Refactor your code to not depend on the pipeline step "magically" existing.
	 * </ol>
	 * 
	 * @param _step_name The name of the pipeline step to mock.
	 * @param _mock_name The display name of the mock (used in test failure messages)
	 * 
	 * @return A mock for the _step_name
	 */
	protected Closure explicitlyMockPipelineStep(String _step_name, String _mock_name = null) {
		
		if( ! mocks.containsKey( _step_name ) ) {
			
			Map<String, String> name_args = new HashMap<>()
			
			if( _mock_name != null ) {
				name_args.put( "name", _mock_name.toString() )
			} else {
				name_args.put( "name", "(explicit) getPipelineMock(\"${_step_name}\")".toString() )
			}
			
			Closure explicitMock = Mock( name_args )
			mocks.put( _step_name, explicitMock )
			pipeline_steps.add( _step_name )
		}
		
		return mocks.get( _step_name )
	}

	/**
	 * Create and/or retrieve a {@link PipelineVariableImpersonator} to mock method calls on a pipeline variable with the given _property_name.
	 *
	 * @param _property_name A property name in a pipeline script
	 *
	 * @return a {@link PipelineVariableImpersonator} to handle method calls on a pipeline variable with the given _property_name.
	 */
	protected explicitlyMockPipelineVariable(String _variable_name) {

		if( ! mocks.containsKey( _variable_name ) ) {
			
			PipelineVariableImpersonator impersonator = new PipelineVariableImpersonator( _variable_name, this )
			
			mocks.put( _variable_name, impersonator )
			pipeline_symbols.add( _variable_name )
			
		} else if ( ! ( mocks.get( _variable_name ) instanceof PipelineVariableImpersonator)  ) {
			throw new IllegalStateException( "Attempted to automatically set up mock calls on Pipeline Variable [${_variable_name}], but there was already a mock for that name and it wasn't a variable: [${mocks.get( _variable_name )}]." )
		}
		
		return mocks.get( _variable_name )
	}
	
	/**
	 * Given a Pipeline Script classpath resource, load it as an executable {@link Script} and instrument it with the appropriate pipeline mocks.
	 * 
	 * @param _path The classpath to a pipeline script resource
	 * 
	 * @return A {@link Script} ready to run in a test suite
	 */
	protected Script loadPipelineScriptForTest(String _path) {
		
		String[] path_parts = _path.split( "/" )
		
		String filename = path_parts[path_parts.length-1]
		
		String resource_path = "/"
		if( path_parts.length >= 2 ) {
			resource_path = String.join( "/", path_parts[0..path_parts.length-2] )
			resource_path = "/${resource_path}/"
		}

		GroovyScriptEngine script_engine = new GroovyScriptEngine(generateScriptClasspath(resource_path))

		Class<Script> script_class = script_engine.loadScriptByName( "${filename}" )

		Script script = script_class.newInstance()

		addPipelineMocksToObjects( script )
		
		return script
	}

	protected String[] generateScriptClasspath(String resourcePath) {
		return script_class_path.collect { path -> path + resourcePath }
	}

	/**
	 * Log helpful information about a call intercepted as a result of this specification.
	 *
	 * @param _level The log level (e.g. <code>DEBUG</code> or <code>WARN</code>, etc)
	 * @param _note A description of this intercept
	 * @param _original_receiver The original object that the call was made on.
	 * @param _original_intercept_method The original mechanism used to intercept the call (e.g. <code>propertyMissing</code> or <code>invokeMethod</code>, etc)
	 * @param _original_method The original call that was attempted
	 * @param _original_args The original arguments to the _original_method
	 * @param _new_receiver The object that the call is being forwarded to
	 * @param _new_method The method on the _new_receiver that will be called
	 * @param _new_args The arguments to the _new_method on the _new_receiver
	 * @param _unwrap_varargs Whether _new_args is a varargs array that will be unwrapped with *_new_args before being passed to _new_method
	 */
	protected static void LOG_CALL_INTERCEPT(
		_level,
		_note,
		_original_receiver,
		_original_intercept_method,
		_original_method,
		_original_args,
		_new_receiver,
		_new_method,
		_new_args,
		_unwrap_varargs )
	{
		
		String receiver_classname = "(can't tell; invokeMethod is overridden)"
		
		if( _original_intercept_method != "invokeMethod" ) {
			// It's only safe to call methods on the original receiver to inspect it if its original invokeMethod behavior is untouched.
			receiver_classname = _original_receiver?.getClass().getSimpleName()
		}
		
		String new_arg_values = _new_args
		String new_arg_types = _new_args?.getClass()
		
		if( _unwrap_varargs && _new_args instanceof Object[] ) {
			new_arg_values = String.join( ", ", _new_args.collect { it.toString() } )
			new_arg_types = String.join( ", ", _new_args.collect{ it.getClass().getSimpleName() } )
		}
		
		LOG."${_level}"(
"""TEST FIXTURE intercepted & redirected a call:
	Test         : {}
	Note         : {} 
	Via          : {}.{}
	    (types)  : {}.{}
	Invocation   : {}.{}({})
	    (types)  : {}.{}({})
	Forwarded To : {}.{}({})
	    (types)  : {}.{}({})
""",
			this,

			_note,
			
			_original_receiver,
			_original_intercept_method,
			
			receiver_classname,
			_original_intercept_method,
			
			_original_receiver,
			_original_method,
			_original_args.toString(),
			
			receiver_classname,
			_original_method,
			_original_args?.getClass().getSimpleName(),
			
			_new_receiver,
			_new_method,
			new_arg_values,
			
			_new_receiver?.getClass().getSimpleName(),
			_new_method,
			new_arg_types
			
			)
	}
	
	/**
	 * Find the names of all .groovy resources on the classpath in <code>vars/*.groovy</code>.
	 * These should correspond to the global variables defined in a shared library.
	 * 
	 * @return The names of all .groovy resources on the classpath in <code>vars/*.groovy</code>.
	 */
	protected Set<String> getSharedLibraryVariables() {
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader()

		URL url = loader.getResource("vars")
		
		if( null == url ) {
			// there are no vars; this probably isn't a shared library.
			return new HashSet<>()
		}

		String path = url?.getPath()
		
		if( null == path ) {
			// it really shouldn't reach this point; the classloaders are probably messed up.
			return new HashSet<>()
		}
		
		List files = new File(path).listFiles() as List
		
		return files
			.collect { it.getName() }                                       // get the name from the File
			.findAll { it.substring( it.lastIndexOf('.') + 1) == "groovy" } // find all groovy files
			.collect{ it.replaceAll( "\\.groovy\$", "" ) }                  // return just the filename
	}
	
	/**
	 * Detect existing pipeline extensions and classes that should be able to call them.
	 * <ol>
	 * <li>Detect all classes in the current project (see {@link LocalProjectPipelineExtensionDetector}) that should delegate calls to pipeline extensions to Spock {@link #mocks}.</li>
	 * <li>Detect all classes on the classpath that provide pipeline extensions, and should be mocked.</li>
	 * <li>Detect all classpath resources in vars/ that correspond to Pipeline Shared Library variables, that should be mocked.</li>
	 * </ol>
	 * <b>Includes</b>
	 * <ol>
	 * <li>{@link CpsScript}</li>
	 * </ol>
	 * <b>Excludes</b>
	 * <ol>
	 * <li>{@link PipelineVariableImpersonator}</li>
	 * <li>All interfaces</li>
	 * </ol>
	 * 
	 * @see #PIPELINE_STEPS
	 * @see #PIPELINE_SYMBOLS
	 * @see #DEFAULT_TEST_CLASSES
	 */
	def setupSpec() {
		
		APipelineExtensionDetector classpath_scanner = new WholeClasspathPipelineExtensionDetector()
		
		PIPELINE_STEPS = classpath_scanner.getPipelineSteps(Optional.<String>empty())
		
		Set<String> symbols = classpath_scanner.getSymbols(Optional.<String>empty())
		Set<String> global_variables = classpath_scanner.getGlobalVariables(Optional.<String>empty())
		Set<String> shared_library_variables = getSharedLibraryVariables()
		
		PIPELINE_SYMBOLS.addAll( symbols )
		PIPELINE_SYMBOLS.addAll( global_variables )
		PIPELINE_SYMBOLS.addAll( shared_library_variables )
		
		Set<Class<?>> classes = new LocalProjectPipelineExtensionDetector()
		.getClassesOfTypeInPackage(
			Object.class,
			Optional.<String>empty())
		
		// This class will behave differently and _not_ forward pipeline step invocations directly to the appropriately-named mock. 
		classes.remove(PipelineVariableImpersonator.class)
		
		// instrument CpsScript, which we know we'll be using.
		classes.add( CpsScript.class )
		
		// exclude all interfaces - nobody will be able to call them, so they don't need to be instrumented.
		// any implementation will be a real, detected class.
		classes = classes.findAll { ! it.isInterface() }
		
		// and publish that list of classes
		DEFAULT_TEST_CLASSES.addAll( classes )
	}
	
	/**
	 * Create mocks for the current test run.
	 * <ol>
	 * <li>Instrument the project's source classes (see {@link #DEFAULT_TEST_CLASSES}) to delegate all calls to Spock {@link #mocks}</li>
	 * <li>Instrument the current test suite object to delegate all calls to pipeline extensions to Spock {@link #mocks}</li>
	 * <li>Mock the {@link Jenkins} singleton in <code>getPipelineMock("Jenkins")</code></li>
	 * <li>Mock the {@link CpsScript} pipeline execution in <code>getPipelineMock("CpsScript")</code></li>
	 * <li>Actually create mock objects for the pipeline extensions for the current test</li>
	 * </ol>
	 */
	def setup() {
		
		/* ==========
		 * Clean up lingering data from previous runs
		 * (shouldn't be necessary...)
		 * ==========
		 */
		// ensure that no generated pipeline steps from other test runs can affect this one
		pipeline_steps.clear()
		pipeline_symbols.clear()
		
		// ensure that no mocks from anywhere else can affect this test-run
		mocks.clear()
		
		/* ==========
		 * Add ability to call pipeline mocks from classes in this project
		 * ==========
		 */
		// first, remove any modifications that previous tests didn't clean up
		DEFAULT_TEST_CLASSES.each {
			GroovySystem.metaClassRegistry.removeMetaClass(it)
		}
		
		// munge input
		Class<?>[] clarray = DEFAULT_TEST_CLASSES.toArray( new Class[0] )
		
		// add mock
		addPipelineMocksToObjects( clarray )
		
		/* ==========
		 * Add ability to call pipeline mocks from
		 * this Specification
		 * ==========
		 */
		addPipelineMocksToObjects( this )
		
		/* ==========
		 * Add ability to call pipeline mocks from
		 * the class this Specification is "for"
		 * ==========
		 */
		// if it just so happens that this class is a SomeClassNameSpec,
		// and SomeClassName exists, try to add pipeline mock functions to SomeClassName
		String test_suite_name = getClass().getName()
		if( test_suite_name.endsWith( "Spec" ) ) {
			String likely_classname_under_test = test_suite_name[0..(test_suite_name.length()-5)]
			try {
				Class<?> likely_class_under_test = Class.forName( likely_classname_under_test )
				
				if( ! instrumented_objects.contains( likely_class_under_test ) ) {
					addPipelineMocksToObjects( likely_class_under_test )
				}
			} catch( ClassNotFoundException cnfe ) {
				// I guess the spec wasn't named like that
			}
		}
		
		/* ==========
		 * Mock the Jenkins singleton
		 * ==========
		 */
		// the singleton itself...
		final Jenkins jenkins = Mock()
		mocks.put( "Jenkins", jenkins )
		
		jenkins.getInstanceOrNull() >> jenkins
		jenkins.getInstance() >> jenkins
		
		Jenkins.HOLDER = new JenkinsHolder() {
			Jenkins getInstance() {
				return mocks.get( "Jenkins" )
			}
		}
		
		// mock .getExtensionList to return dummy extension instances
		jenkins.getExtensionList(_) >> { Class<?> extension_type ->
			
			ExtensionList extensions = Mock()
			
			Set<Class<?>> classes = new WholeClasspathPipelineExtensionDetector()
				.getClassesWithAnnotationOfTypeInPackage(
					Extension.class,
					extension_type,
					Optional.<String>empty() )
				
			LOG.debug(
				"Intercepted jenkins.getExtensionList({}); will return a mock Iterable with a single-use .iterator() stubbed over [{}]",
				extension_type,
				classes )
			
			List<Object> extension_objs = classes.collect {
				if( ! dummy_extension_instances.containsKey( it ) ) {
					dummy_extension_instances.put( it, it.newInstance() )
				}
				
				return dummy_extension_instances.get( it )
			}
			
			extensions.iterator() >> extension_objs.iterator()
			extensions.size() >> extension_objs.size()
				
			return extensions
		}
		
		/* ==========
		 * Mock the Pipeline execution
		 * ==========
		 */
		final CpsScript interceptor_script = Spy(name: "getPipelineMock(\"CpsScript\")")
		interceptor_script.metaClass.invokeMethod = { String _name, Object[] _args ->
			
			if( _name == "invokeMethod" ) {
				/*
				 * Something called invokeMethod explicitly, e.g.
				 * _script.invokeMethod( "foo", "bar" )
				 * This is the expected way that Groovy code will use CpsScript objects 
				 * to call pipeline steps.
				 * 
				 * Remove the explicit "invokeMethod" wrappring and then try to invoke the actual method
				 * on the CpsScript object.
				 */
				
				String unwrapped_name = _args[0]
				def unwrapped_args = _args[1]
				
				if( ! (unwrapped_args instanceof Object[]) ) {
					// this method took one argument. Later on, unwrapping it will be WRONG, so, just pre-wrap it.
					unwrapped_args = [_args[1]].toArray()
				}

				LOG_CALL_INTERCEPT(
					"debug",
					"(wrapped) Spy CpsScript",
					interceptor_script,
					"invokeMethod",
					_name,
					_args,
					owner,
					unwrapped_name,
					unwrapped_args,
					true )
				
				invokeMethod( unwrapped_name, unwrapped_args )
			} else {
				
				/*
				 * something actually called a method on a CpsScript.
				 * This could either be a legitimate use-case in Jenkins,
				 * or a result of the above conditional branch during a unit test.
				 * 
				 * Redirect the method call to the current test specification
				 * and let it hit a Mock object (or actually miss, if there is no mock).
				 */
				
				def prepared_args = _args
				
				if( ! ( prepared_args instanceof Object[] ) ) {
					// this method took one argument. Later on, unwrapping it will be WRONG, so, just pre-wrap it.
					prepared_args = [_args].toArray()
				}
				
				LOG_CALL_INTERCEPT(
					"debug",
					"Spy CpsScript",
					interceptor_script,
					"invokeMethod",
					_name,
					_args,
					owner,
					_name,
					prepared_args,
					true )
				
				owner."${_name}"( *prepared_args )
			}
		}
		
		mocks.put( "CpsScript", interceptor_script )
		
		/* ==========
		 * Actually create the Pipeline mock objects
		 * ==========
		 */
		// pipeline symbols, e.g. Symbol.someMethod( ... )
		PIPELINE_SYMBOLS.each {
			PipelineVariableImpersonator pam = new PipelineVariableImpersonator( it, this )
			mocks.put( it, pam )
			PIPELINE_STEPS.add( "${it}.getProperty".toString() )
		}
		
		// Pipeline steps, e.g. someStep( ... )
		PIPELINE_STEPS.each {
			Closure mockForStepFromClasspath = Mock( name: "getPipelineMock(\"${it}\")".toString() )
			mocks.put( it, mockForStepFromClasspath )
		}
		
		// mock the CpsScript's variable/environment binding
		Binding mock_pipeline_binding = Mock()
		
		// mock the CpsScript's getBinding
		Closure mock_pipeline_getBinding = Mock( name: "getPipelineMock(\"getBinding\")" )
		// make getBinding() return the mock Binding
		mock_pipeline_getBinding() >> mock_pipeline_binding
		
		mocks.put( "getBinding", mock_pipeline_getBinding )
		PIPELINE_STEPS.add( "getBinding" )
	}
}
