import org.jenkinsci.plugins.workflow.libs.Library

@Library("pipeline") _

node {
	timestamps {
		prepare()
	}
}
