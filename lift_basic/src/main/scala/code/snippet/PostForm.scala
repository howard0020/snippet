package code.snippet

import net.liftweb.http.LiftScreen
import code.model._
import code.comet.PostServer
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE._


//object PostForm extends LiftScreen{
//  
//  object Snippet extends ScreenVar(CodeSnippet.create)
//  
//  addFields(()=>Snippet.is.content)
// 
//  
//def finish(){
//    Snippet.is.Author(User.currentUser.get)
//    Snippet.is.save
//    PostServer ! Snippet.is
//   // User.currentUser.get.AllPost
//  //  System.out.print("123123" + User.currentUser.get.AllPost)
//	  //S.notice("Adding Value: " + keyvalue.value)
//	}
//}
class PostForm extends {
	def ajaxForm = SHtml.ajaxForm(JsRaw("editor.save();").cmd, (SHtml.textarea("",sendMessage _,"id"->"snippetTextArea") ++ SHtml.submitButton(() => {})))
	
    def sendMessage(msg: String) = {
  	    val snippet = CodeSnippet.create
  	    snippet.content.set(msg)
  	    snippet.save
  	    PostServer ! snippet
  	} 
}