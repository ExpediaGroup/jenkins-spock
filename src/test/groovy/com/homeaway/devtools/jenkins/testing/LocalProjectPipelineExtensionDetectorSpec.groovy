package com.homeaway.devtools.jenkins.testing

import com.homeaway.devtools.jenkins.testing.functions.ScriptToTest

class LocalProjectPipelineExtensionDetectorSpec extends JenkinsPipelineSpecification {

    def "LocalProjectPipelineExtensionDetectorSpec should detect classes that aren't direct subtypes of java-lang-Object" () {
        when:
        Set<Class<?>> classes = new LocalProjectPipelineExtensionDetector()
            .getClassesOfTypeInPackage(
                Object.class,
                Optional.<String>empty()
            )

        then:
        classes.contains(ScriptToTest)
    }
}
