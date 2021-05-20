import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class DefaultPipelineSpec extends JenkinsPipelineSpecification {

	def DefaultPipeline = null

	public static class DummyException extends RuntimeException {
		public DummyException(String _message) { super( _message ); }
	}

	def setup() {
		script_class_path = ["vars"]
		DefaultPipeline = loadPipelineScriptForTest("/DefaultPipeline.groovy")
		DefaultPipeline.getBinding().setVariable( "scm", null )
		getPipelineMock("libraryResource")(_) >> {
			return "Dummy Message"
		}
	}

	def "Slack is notified when tests fail" () {
		setup:
			getPipelineMock("sh")("docker run --entrypoint python whole-pipeline -m unittest discover") >> {
				throw new DummyException("Dummy test failure")
			}
		when:
			try {
				DefaultPipeline()
			} catch( DummyException e ) {}
		then:
			1 * getPipelineMock("slackSend")( _ as Map )
	}

	def "Attempts to deploy MASTER branch to PRODUCTION" () {
		setup:
			DefaultPipeline.getBinding().setVariable( "BRANCH_NAME", "master" )
		when:
			DefaultPipeline()
		then:
			1 * getPipelineMock("Deployer.call")("production")
	}

	def "Does NOT attempt to deploy non-MASTER branch PRODUCTION" () {
		setup:
			DefaultPipeline.getBinding().setVariable( "BRANCH_NAME", "develop" )
		when:
			DefaultPipeline()
		then:
			0 * getPipelineMock("Deployer.call")("production")
	}
}

