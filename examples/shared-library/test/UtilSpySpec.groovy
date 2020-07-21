import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

class UtilSpySpec extends JenkinsPipelineSpecification {
	def groovyScript = null
	def spyScript = null

	def setup() {
		groovyScript = loadPipelineScriptForTest("vars/util.groovy")
		spyScript = Spy( groovyScript )
	}

	def "[util.groovy] method1"() {
		setup:
			groovyScript.method2() >> null
		when:
			groovyScript.method1()
		then:
			1 * getPipelineMock("sh")("id")
			1 * groovyScript.method2()
			0 * getPipelineMock("sh")('ls')
	}
}
