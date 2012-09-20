package code.snippet

import net.liftweb.http.LiftScreen
import code.model._
import code.comet.PostServer

object PostForm extends LiftScreen{
  
  object Snippet extends ScreenVar(CodeSnippet.create)
  
  addFields(()=>Snippet.is.content)
 
  
def finish(){
    Snippet.is.Author(User.currentUser.get)
    Snippet.is.save()
    PostServer ! Snippet.is
   // User.currentUser.get.AllPost
  //  System.out.print("123123" + User.currentUser.get.AllPost)
	  //S.notice("Adding Value: " + keyvalue.value)
	}
}