class MyHelper {
   def thing() {
     /// does things independently of the pipeline
     def otherClassObj = new otherClass()
     otherClassObj.methodCall( "incorrectParameter" )
  } 
}