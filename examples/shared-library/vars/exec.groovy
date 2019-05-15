void call(String command) {
	if (isUnix()) {
		sh command
	} else{
		bat command
	}
}