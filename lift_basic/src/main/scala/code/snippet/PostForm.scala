package code.snippet

import net.liftweb.http.LiftScreen
import code.model._
import code.model.post._
import code.comet.PostServer
import net.liftweb.http.SHtml
import net.liftweb.http.js.{JsCmd, JE, JsCmds}
import JsCmds.Noop
import scala.xml.{NodeSeq}
import net.liftweb.http.{SessionVar}
import net.liftweb.http.js.jquery.JqJsCmds.FadeIn
import scala.swing.TextArea
import scala.swing.TextArea
import net.liftweb.util._
import Helpers._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JE.JsVar
import net.liftweb.http.js.JsCmds.{Function, Script}
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonAST.JArray
import net.liftweb.json.JsonAST.JString
import net.liftweb.common.Loggable
import net.liftweb.common.{Full,Empty,Failure}
import net.liftweb.http.S
import net.liftweb.http.RedirectResponse


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
class PostForm extends Loggable{
	def ajaxForm = SHtml.ajaxForm(JsRaw("editor.save();").cmd, (SHtml.textarea("",sendMessage _,"id"->"snippetTextArea") ++ SHtml.submitButton(() => {})))
	
    def sendMessage(msg: String) = {
  	    val snippet = CodeSnippet.create
  	    snippet.content.set(msg)
  	    snippet.save
  	    PostServer ! snippet
  	} 
	
	//----------------NEW----------------------------------------------------//
	val ourFnName = Helpers.nextFuncName
  /**
   * JavaScript to collect our form data
   */
  val js1 =
    """
      |window.dyTable = new window.fmpwizard.views.DynamicFields();
      |window.dyTable.collectFormData(%s);
  	  |
    """.format(ourFnName).stripMargin
  /**
   * JavaScript to setup the adding rows to the page action
   */
  val js2 =
    """
      |            $(document).ready(function() {
      |              window.dyTable = new window.fmpwizard.views.DynamicFields();
      |              window.dyTable.addFields();
      |              window.dyTable.removeFields();
      |            });
    """.stripMargin
    
    def render = {
    "#next [onclick]" #> JE.JsRaw(js1)
  }
	def sendToServer = {
    "#sendToServer" #> Script(
      Function(ourFnName, List("paramName"),
        SHtml.jsonCall(JsVar("paramName"), test _)._2.cmd //use on lift >= 2.5
        //SHtml.jsonCall(JsVar("paramName"), (s: Any) => addRowsToDB(s) )._2.cmd //Use this on Lift < 2.5
      )
    ) &
    "#initDynamic" #> Script(JE.JsRaw(js2).cmd)
	}
	def test(x:Any) :JsCmd ={
	 
	  val boxList = Full(x).asA[List[List[String]]]
	  boxList match { 
	 		case Full(tempList) =>
 		     
 		      if(User.currentUser.isEmpty){
 		        S.redirectTo("/user_mgt/login")
 		        return
 		      }
 		      val user = User.currentUser openTheBox
	 		  val post = CodeSnippet.create.Author(user.id)
	 		  tempList.foreach(s => 
	 		  	s match {
	 		  	  case "titleField" :: content :: Nil =>
	 		  	    post.title.set(content)
	 		  	  case "htmlBlock" :: content :: Nil =>
	 		  	  	val block = Block.create.post(post.id).content(content)
	 		  	  	post.blocks += block
	 		  	  	block.save()
	 		  	  case "codeBlock" :: meta :: content :: Nil => 
	 		  	    val block = Block.create.post(post.id).content(content).meta(meta)
	 		  	    post.blocks += block
	 		  	  	block.save()
	 		  	  case nil => 
	 		  	}
	 		  )
	 		  post.save()
	 		  Noop
	 		case Empty => S.error("Empty Form.")
	 		case Failure(msg,_,_) => S.error(msg)
	  }
	}
}