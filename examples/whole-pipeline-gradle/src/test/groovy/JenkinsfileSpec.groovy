import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class JenkinsfileSpec extends JenkinsPipelineSpecification {
	
	def Jenkinsfile = null
	
	public static class DummyException extends RuntimeException {
		public DummyException(String _message) { super( _message ); }
	}

	def setup() {
		script_class_path = ["."]
		Jenkinsfile = loadPipelineScriptForTest("/Jenkinsfile")
		Jenkinsfile.getBinding().setVariable( "scm", null )
	}
	
	def "Slack is notified when tests fail" () {
		setup:
			getPipelineMock("sh")("docker run --entrypoint python whole-pipeline -m unittest discover") >> {
				throw new DummyException("Dummy test failure")
			}
		when:
			try {
				Jenkinsfile.run()
			} catch( DummyException e ) {}
		then:
			1 * getPipelineMock("slackSend")( _ as Map )
	}
	
	def "Attempts to deploy MASTER branch to PRODUCTION" () {
		setup:
			Jenkinsfile.getBinding().setVariable( "BRANCH_NAME", "master" )
		when:
			Jenkinsfile.run()
		then:
			1 * getPipelineMock("sh")({it =~ /ssh deployer@app-prod .*/})
	}
	
	def "Does NOT attempt to deploy non-MASTER branch PRODUCTION" () {
		setup:
			Jenkinsfile.getBinding().setVariable( "BRANCH_NAME", "develop" )
		when:
			Jenkinsfile.run()
		then:
			0 * getPipelineMock("sh")({it =~ /ssh deployer@app-prod .*/})
	}
	
	def "deploy function deploys to TEST when asked" () {
		when:
			Jenkinsfile.deploy( "test" )
		then:
			1 * getPipelineMock("sshagent")(["test-ssh"], _ as Closure)
			1 * getPipelineMock("sh")({it =~ /ssh deployer@app-test .*/})
	}
	
	def "deploy function deploys to PRODUCTION when asked" () {
		when:
			Jenkinsfile.deploy( "production" )
		then:
			1 * getPipelineMock("sshagent")(["prod-ssh"], _ as Closure)
			1 * getPipelineMock("sh")({it =~ /ssh deployer@app-prod .*/})
	}
}

