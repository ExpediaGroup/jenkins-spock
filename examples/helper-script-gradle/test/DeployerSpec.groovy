import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class DeployerSpec extends JenkinsPipelineSpecification {

	def Deployer = null

	def setup() {
		script_class_path = ["vars", "build/classes/groovy/main"]
		Deployer = loadPipelineScriptForTest("/Deployer.groovy")
	}

	def "deploy function deploys to TEST when asked" () {
		when:
			Deployer.deploy( "test" )
		then:
			1 * getPipelineMock("sshagent")(["test-ssh"], _ as Closure)
			1 * getPipelineMock("sh")({it =~ /ssh deployer@app-test .*/})
	}

	def "deploy function deploys to PRODUCTION when asked" () {
		when:
			Deployer.deploy( "production" )
		then:
			1 * getPipelineMock("sshagent")(["prod-ssh"], _ as Closure)
			1 * getPipelineMock("sh")({it =~ /ssh deployer@app-prod .*/})
	}
}