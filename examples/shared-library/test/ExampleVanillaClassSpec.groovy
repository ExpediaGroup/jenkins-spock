import spock.lang.Specification

import com.example.ExampleVanillaClass

public class ExampleVanillaClassSpec extends Specification {

	ExampleVanillaClass example = null

	def setup() {
		example = new ExampleVanillaClass()
	}

	def "[Example] will return a string" () {
		when:
		   def string = example.execute()
		then:
		   assert string == "Has executed"
	}
}