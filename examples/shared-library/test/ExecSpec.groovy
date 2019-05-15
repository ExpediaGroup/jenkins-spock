import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

class ExecSpec extends JenkinsPipelineSpecification {

	def Exec

	def setup() {
		Exec = loadPipelineScriptForTest('vars/exec.groovy')
	}
	
	def "Sanity-Check isUnix"() {
		expect:
			isUnix() == null
	}
	
	def "Sanity-Check mocking isUnix"() {
		setup:
			getPipelineMock('isUnix')() >> { return true }
		expect:
			isUnix() != null
	}
	
	def "Sanity-Check expecting isUnix"() {
		when:
			Exec('ls')
		then:
			_ * getPipelineMock('isUnix')() >> { return true }
			1 * getPipelineMock('sh') ('ls')
	}

	def "(broken) Test on Windows"() {
		setup:
			getPipelineMock('isUnix')() >> { return false }

		when:
			Exec('ls')
		then:
			1 * getPipelineMock('isUnix') ()
			1 * getPipelineMock('bat') ('ls')
	}

	def "(broken) Test on Linux"() {
		setup:
			getPipelineMock('isUnix')() >> { return true }

		when:
			Exec('ls')
		then:
			1 * getPipelineMock('isUnix') ()
			0 * getPipelineMock('sh') ('ls')
	}
	
	def "Test on Windows"() {
		when:
			Exec('ls')
		then:
			1 * getPipelineMock('isUnix') () >> { return false }
			1 * getPipelineMock('bat') ('ls')
	}

	def "Test on Linux"() {
		when:
			Exec('ls')
		then:
			1 * getPipelineMock('isUnix') () >> { return true }
			1 * getPipelineMock('sh') ('ls')
	}
}