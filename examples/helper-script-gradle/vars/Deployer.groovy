def deploy( _env ) {
	
	def DEPLOY_COMMAND="""
	docker-compose pull && \
	docker-compose down && \
	docker-compose rm -f && \
	docker-compose up -d --force-recreate"""
	
	if( _env == "test" ) {
		sshagent(["test-ssh"]) {
			sh( "ssh deployer@app-test -c '${DEPLOY_COMMAND}'" )
		}
	} else if( _env == "production" ) {
		sshagent(["prod-ssh"]) {
			sh( "ssh deployer@app-prod -c '${DEPLOY_COMMAND}'" )
		}
	}
}

return this
