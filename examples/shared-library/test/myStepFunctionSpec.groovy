import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class myStepFunctionSpec extends JenkinsPipelineSpecification {

	def myStepFunction = null

	def setup() {
		myStepFunction = loadPipelineScriptForTest("vars/myStepFunction.groovy")
	}

	def "correct method call" () {
		when:
			myStepFunction.call()
		then:
			1 * getPipelineMock( "echo" )( "called methodCall" )
	}
}