import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class JenkinsfileSpec extends JenkinsPipelineSpecification {
	
	def Jenkinsfile = null

	def setup() {
		Jenkinsfile = loadPipelineScriptForTest("/Jenkinsfile")
	}
	
	def "says 'hi' to the world"() {
		when:
			Jenkinsfile.run()
		then:
			1 * getPipelineMock("sh")( [returnStdout: true, script: "echo 'hello world'" ] ) >> "hello world"
			1 * getPipelineMock("echo")( "hello world" )
	}
}

