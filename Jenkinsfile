node( 'legacy' ) {
	sshagent( ['github.com_homeaway-jenkins_ssh'] ) {

		stage( 'Setup' ) {
			checkout scm
		}
		
		stage( 'Build' ) {
			sh( 'mvn clean install' )
		}
	}
}
