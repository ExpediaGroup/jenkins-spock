def method1() {
	sh "id"
	method2()
}
def method2(){
	sh "ls"
}