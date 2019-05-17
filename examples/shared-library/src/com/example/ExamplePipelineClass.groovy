package com.example

class ExamplePipelineClass implements Serializable {
	Utils utils = new Utils()

	def execute() {
		sh( "some command" )
		return "Has executed"
	}
}