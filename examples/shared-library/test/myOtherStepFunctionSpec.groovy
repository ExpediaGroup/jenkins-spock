import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class myOtherStepFunctionSpec extends JenkinsPipelineSpecification {

	def myOtherStepFunction = null

	def setup() {
		myOtherStepFunction = loadPipelineScriptForTest("vars/myOtherStepFunction.groovy")
	}
	
	def "demonstrates incorrect pipeline step call" () {
		when:
			myOtherStepFunction.call()
		then:
			1 * getPipelineMock( "echo" )( "in a node" )
	}
}