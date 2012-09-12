package code.snippet

import net.liftweb.http.LiftScreen
import code.model._

object PostForm extends LiftScreen{
  
  object Snippet extends ScreenVar(CodeSnippet.create)
  
  addFields(()=>Snippet.is.content)
 
  
def finish(){
    Snippet.is.Author(User.currentUser)
    Snippet.save()
	  //S.notice("Adding Value: " + keyvalue.value)
	}
}