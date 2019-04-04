def call() {
	stage("Prepare") {
		echo "Prepare workspace"
		cleanWs()
	}
}
