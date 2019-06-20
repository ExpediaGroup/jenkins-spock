import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification
import com.example.ExampleClass

class ExampleClassSpec extends JenkinsPipelineSpecification {
	
	def exampleClass

	def setup() {
		exampleClass = new ExampleClass()
	}
	
	def "forcibly adding a new instance variable" () {
		setup:
			exampleClass.metaClass.WORKSPACE="dummy-workspace"
		when:
			exampleClass.execute()
		then:
			1 * getPipelineMock("echo")("dummy-workspace")
	}
}
