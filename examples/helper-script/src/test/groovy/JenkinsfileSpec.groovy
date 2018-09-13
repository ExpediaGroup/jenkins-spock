import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class JenkinsfileSpec extends JenkinsPipelineSpecification {

	def Jenkinsfile = null

	public static class DummyException extends RuntimeException {
		public DummyException(String _message) { super( _message ); }
	}

	def setup() {
		Jenkinsfile = loadPipelineScriptForTest("/Jenkinsfile")
		Jenkinsfile.getBinding().setVariable( "scm", null )
		explicitlyMockPipelineVariable("Deployer")
		getPipelineMock("load")("Deployer.groovy") >> {
			getPipelineMock("Deployer")
		}
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
			1 * getPipelineMock("Deployer.deploy")({it =~ /production/})
	}

	def "Does NOT attempt to deploy non-MASTER branch PRODUCTION" () {
		setup:
			Jenkinsfile.getBinding().setVariable( "BRANCH_NAME", "develop" )
		when:
			Jenkinsfile.run()
		then:
			0 * getPipelineMock("Deployer.deploy")({it =~ /production/})
	}
}

