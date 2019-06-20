package com.example

class ExampleClass {
	def execute() {
		dir(){ // Jenkins step
			echo "${WORKSPACE}"
		}
	}
}
