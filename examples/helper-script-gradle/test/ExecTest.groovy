import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

// Ideally should be names ExecSpec.groovy
// if I name it ExecSpec, I get the following stack trace
//
//java.lang.NoClassDefFoundError: Exec (wrong name: exec)
//
//at java.lang.ClassLoader.defineClass(ClassLoader.java:756)
//at java.security.SecureClassLoader.defineClass(SecureClassLoader.java:142)
//at java.net.URLClassLoader.defineClass(URLClassLoader.java:468)
//at java.net.URLClassLoader.access$100(URLClassLoader.java:74)
//at java.net.URLClassLoader$1.run(URLClassLoader.java:369)
//at java.net.URLClassLoader$1.run(URLClassLoader.java:363)
//at java.net.URLClassLoader.findClass(URLClassLoader.java:362)
//at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
//at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:355)
//at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
//at java.lang.Class.forName(Class.java:264)
//at com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification.setup(JenkinsPipelineSpecification.groovy:1091)
class ExecTest extends JenkinsPipelineSpecification {

    def Exec

    def setup() {
        script_class_path = ["vars", "build/classes/groovy/main"]
        Exec = loadPipelineScriptForTest('/exec.groovy')
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