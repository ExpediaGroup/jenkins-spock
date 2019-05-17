import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

import com.example.ExamplePipelineClass

public class ExamplePipelineClassSpec extends JenkinsPipelineSpecification {

	ExamplePipelineClass example = null

	def setup() {
		example = new ExamplePipelineClass()
	}

	def "[Example] will return a string" () {
		when:
		   def string = example.execute()
		then:
			1 * getPipelineMock( "sh" )( "some command" )
		   assert string == "Has executed"
	}
}