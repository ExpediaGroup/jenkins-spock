import com.example.SharedLibraryConstants

def call( _env ) {
	
	if( _env == "test" ) {
		sshagent(["test-ssh"]) {
			sh( "ssh deployer@app-test -c '${SharedLibraryConstants.DEPLOY_COMMAND}'" )
		}
	} else if( _env == "production" ) {
		sshagent(["prod-ssh"]) {
			sh( "ssh deployer@app-prod -c '${SharedLibraryConstants.DEPLOY_COMMAND}'" )
		}
	}
}
