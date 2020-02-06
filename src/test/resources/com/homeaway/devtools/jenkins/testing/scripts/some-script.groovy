node {
	stage("greet") {
		echo "hello"
		helper_method()
	}
}

def helper_method() {
	echo "helped"
}
