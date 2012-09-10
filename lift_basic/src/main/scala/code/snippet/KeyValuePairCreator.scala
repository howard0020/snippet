package code 
package snippet 
import net.liftweb.http.LiftScreen
import net.liftweb.http.S
import code.model._

object KeyValuePairCreator extends LiftScreen{
	object keyvalue extends ScreenVar(KeyValuePair.create)
  	override def screenTop = <b>Key Value Pair Creation</b>
  	  
    addFields(()=>keyvalue.is)
    
    
    
    /*override def validations = KeyValueValidation _ :: super.validations
	
	def KeyValueValidation(): Errors ={
	  var msg = ""
	  if(keyvalue.key.equals("")) msg+"Key can't be empty\n";
	    if(keyvalue.value.equals("")) msg + "value can't be empty\n"
	        
	  else Nil
	}*/
	
	def finish(){
	  //S.notice("Adding Value: " + keyvalue.value)
	  if(keyvalue.is.save)
	    S.notice(keyvalue.is.toString+"Saved!")
	}
}