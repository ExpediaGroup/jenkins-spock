import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

class UtilMetaprogrammingSpec extends JenkinsPipelineSpecification {
	def groovyScript = null

	def setup() {
		
		// load pipeline script
		groovyScript = loadPipelineScriptForTest("vars/util.groovy")
		
		// create a mock for uitl.method2. The name actually does not matter.
		explicitlyMockPipelineStep( "util.method2" )
		
		// create a new runtime-expandable metaClass for method dispatch for the groovyScript
		def newMetaClass = new ExpandoMetaClass(Script.class, true, true)
		newMetaClass.initialize()
		
		// copy all new methods from the groovyScript over to the new metaClass
		groovyScript.metaClass.getMethods().each { _method -> 
			if( 
				( _method.getDeclaringClass().getName() ==  groovyScript.getClass().getName() ) &&
				! ( newMetaClass.getMetaMethods().any{ it.getName() == _method.getName() } ) &&
				! ( newMetaClass.getMethods().any{ it.getName() == _method.getName() } )
			) {
				// the method doesn't exist
				newMetaClass."${_method.getName()}" = groovyScript.&"${_method.getName()}"
			}
		}
		
		// define "method2" on the new metaClass to call the pipeline mock for util.method2
		// the .method2 is the name that matters
		newMetaClass.method2 = { -> getPipelineMock("util.method2")() }
		
		// set the new metaClass & method definitions on the loaded pipeline script
		groovyScript.metaClass = newMetaClass
		
		// re-add the pipeline mocks to the Script; they had been on the old metaClass which we threw out.
		addPipelineMocksToObjects(groovyScript)
	}

	def "[util.groovy] method1"() {
		when:
			groovyScript.method1()
		then:
			1 * getPipelineMock("sh")("id")
			1 * getPipelineMock("util.method2")()
			0 * getPipelineMock("sh")("ls")
	}
}