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
import code.share.SiteConsts
import net.liftweb.http.js.JE.JsArray


class PostForm extends Loggable{
	def ajaxForm = SHtml.ajaxForm(JsRaw("editor.save();").cmd, (SHtml.textarea("",sendMessage _,"id"->"snippetTextArea") ++ SHtml.submitButton(() => {})))
	
    def sendMessage(msg: String) = {
  	    val snippet = PostModel.create
  	    snippet.content.set(msg)
  	    snippet.save
  	    PostServer ! snippet
  	} 
	
	val ourFnName = Helpers.nextFuncName
	//0 if this is a new post
	var currentEditPost :PostModel =null;
  /**
   * JavaScript to collect our form data
   */
	
  val js1 =
    """
      |window.dyTable = new window.fmpwizard.views.DynamicFields();
      |window.dyTable.collectFormData(%s);
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
      |				 window.initModeOpts("newModeSelect");
      |              window.initModeOpts("changeModeSelect");
      |				 initEditors();
      |            });
    """.format(ourFnName).stripMargin
    /**
     * JavaScript to setup initial editors
     */
    def js3 :String=
      """
      	|function initEditors(){
      	|	%s	
      	|}
      """.format(addEditorsJS).stripMargin
      
    var addEditorsJS = "";
	
    def render = "#next [onclick]" #> JE.JsRaw(js1) & renderPostContent
	
    def renderPostContent = {
   
  	      var postTitle = ""
	      S.param("id") match{
	        case Full(id) => PostModel.findByKey(id.toLong) match{
	          case Full(post) => {
	        	  currentEditPost = post
	        	  postTitle = post.title
		    	  post.blocks.map(b => if(b.meta.toString() == ""){
		    		addEditorsJS += "addHTMLBlock('%s')".format(b.content.is) + "\n"
		    	  }else{ 
		    	    addEditorsJS += """addCodeBlock(%s,'%s')""".format(b.contentToJSString,b.meta.toString()) + "\n"
		    	  })
		    	  "#post_title [value]" #> postTitle
	          }
	          case Empty => "#empty" #> ""
	        }
	        case Empty => 
	          addEditorsJS += "addHTMLBlock('<h3>Header</h3>');"
	          addEditorsJS += "\n"
	          addEditorsJS += """addCodeBlock('import snippet.fun._\nclass ReplaceMe extends SomeCode{\n	def click = {\n		this.text.remove\n	}\n}','text/x-scala');"""
	          "#empty" #> ""
	      }
    }
    
	def sendToServer = {
    "#sendToServer" #> Script(
      Function(ourFnName, List("paramName"),
        SHtml.jsonCall(JsVar("paramName"), processForm _)._2.cmd //use on lift >= 2.5
      )
    ) & 
     "#initDynamic" #> Script(JE.JsRaw(js2).cmd & JE.JsRaw(js3).cmd) 
	}
	
	def processForm(x:Any) :JsCmd ={
	  val boxList = Full(x).asA[List[List[String]]]
	  boxList match { 
	 		case Full(tempList) =>
 		      if(UserModel.currentUser.isEmpty){
 		        S.redirectTo(SiteConsts.LOGIN_URL)
 		        return
 		      }
 		      val user = UserModel.currentUser openTheBox
 		       val post = if(currentEditPost != null){
 		         //delete all the blocks and recreate them
 		    	   currentEditPost.blocks.foreach(b => b.delete_!)
 		    	   currentEditPost.blocks.clear
 		    	   currentEditPost.blocks.save
 		    	   currentEditPost
 		       }else{
 		    	   PostModel.create.Author(user.id)
 		       }
	 		  tempList.foreach(s => 
	 		  	s match {
	 		  	  case "titleField" :: content :: Nil =>
	 		  	    post.title.set(content)
	 		  	  case "htmlBlock" :: content :: Nil =>
	 		  	  	val block = BlockModel.create.post(post.id).content(content)
	 		  	  	post.blocks += block
	 		  	  	block.save()
	 		  	  case "codeBlock" :: meta :: content :: Nil => 
	 		  	    val block = BlockModel.create.post(post.id).content(content).meta(meta)
	 		  	    post.blocks += block
	 		  	  	block.save()
	 		  	  case nil => 
	 		  	}
	 		  )
	 		  post.save()
	 		  PostServer ! post
	 		  S.redirectTo(SiteConsts.INDEX_URL)
	 		  Noop
	 		case Empty => S.error("Empty Form.")
	 		case Failure(msg,_,_) => S.error(msg)
	  }
	}
}