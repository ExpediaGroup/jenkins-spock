def call( Map _args ) {

	node {
		stage( "Checkout" ) {
			checkout scm
		}
	
		stage( "Build" ) {
			sh( "docker build --tag whole-pipeline ." )
		}
	
		stage( "Test" ) {
			try {
				sh( "docker run --entrypoint python whole-pipeline -m unittest discover" )
			} catch( Exception e ) {
				
				def message = evaluate( '"""' + libraryResource( "com/example/SlackMessageTemplate.txt" ) + '"""' )
				
				slackSend(
					color: 'error',
					message: message )
				throw e
			}
		}
	
		stage( "Push" ) {
			sh( "docker push whole-pipeline" )
		}
	
		stage( "Deploy to TEST" ) {
			Deployer( "test" )
		}
	
		if( BRANCH_NAME == "master" ) {
			stage( "Deploy to PRODUCTION" ) {
				Deployer( "production" )
			}
		}
	}
}
