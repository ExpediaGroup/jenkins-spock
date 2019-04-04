import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class PipeSpec extends JenkinsPipelineSpecification {

	def "Seeded Job Test" () {
		setup:
			def script = loadPipelineScriptForTest("pipes/pipe.groovy")

		when:
			script.run()

		then:
			1 * getPipelineMock("node")(_)
			1 * getPipelineMock("timestamps")(_)
			1 * getPipelineMock("prepare.call")()
	}
}

