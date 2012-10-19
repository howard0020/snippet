package code.comet

import scala.xml._
import code.model.CodeSnippet
import net.liftweb.actor.LiftActor
import net.liftweb.http._
import net.liftweb.json._
import net.liftweb.util._
import net.liftweb.http.js._
import net.liftweb.http.js.JE._
import JsCmds._
import scala.xml.NodeSeq
import Helpers._
import code.model.Tag
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.mapper.By
import code.snippet.Post
import net.liftweb.common.Failure
import net.liftweb.common.Box
import code.model.SnippetTags
import code.model.User
import code.model.post.Block
import scala.xml.Attribute
import scala.Null

class PostActor extends CometActor with CometListener {

  
  implicit val formats = net.liftweb.json.DefaultFormats
  
  private var currTagFilter : Box[Tag] = Empty
  
  //for new post
  private var content = ""
  //for new post
  private var tags = ""

  private var title = ""

  private var posts = getPosts(Empty)
 

  def getPosts(tag:Box[Tag]):List[CodeSnippet] = {
	    tag match {
	    	case Full(theTag) => theTag.posts.all
	    	case Empty =>  CodeSnippet.findAll()
	    	case Failure(msg,_,_) => 
	    	  S.error(msg)
	    	  CodeSnippet.findAll()
	 	}
  }


  def registerWith = PostServer
		  
  def render = "#postForm *" #> ajaxForm & "#postTemplate *" #> bindText

  def bindText =
    ".post_wrapper" #> (
      (ns: NodeSeq) => (posts.flatMap( p => (
          ".post_author_image [src]" #> (p.getAuthor match{
            case Full(author)=> 
              if(author.iconURL.get.equals("")) "http://profile.ak.fbcdn.net/static-ak/rsrc.php/v2/yL/r/HsTZSDw4avx.gif"
              else author.iconURL.get
            case Empty => ""
            case Failure(msg,_,_) =>"Error" 
            })&
          ".post_author_name *" #> (p.getAuthor match{
            case Full(author)=> author.username.get
            case Empty => ""
            case Failure(msg,_,_) =>"Error"
          }) &
          ".post_created_date *" #> (if(p.createdAt == null) "" else p.createdAt.toString()) &
          ".post_title *" #> (if(p.title == null) "" else p.title.toString()) &
          ".post_content *" #> { ".post_block" #> 
          			(p.blocks.map(b => "*" #> getBlockContent(b)))} & 
          ".tag *" #> (if(p.getTags.equals("")) "" else ("tags: " + p.getTags))
          )(ns))))
          
  def getBlockContent(block: Block):NodeSeq = {
      if(block.meta.toString == ""){
	     xml.Unparsed(block.content.is)  
      }else
      {
         val text =Option(block.content.is)
        <textarea class="code-block-fields">{text.getOrElse("")}</textarea> % Attribute(None,"mode",Text(block.meta.is),Null)
      }
  }
    
  def ajaxForm = SHtml.ajaxForm(JsRaw("editor.save();").cmd, 
      (SHtml.textarea("", content = _, "id" -> "snippetTextArea") 
    		  ++ SHtml.text("", title = _)  
    		  ++ SHtml.text("Lift",tags = _)
    		  ++ SHtml.submitButton(() => {})
    		  ++ SHtml.hidden(() => postForm)
      ))
  
  private def postForm = {
    val snippet = CodeSnippet.create
    snippet.Author.set(User.currentUser match{
            case Full(curUser)=> curUser.id
            case Empty => -1
            case Failure(msg,_,_) => -1})
    snippet.content.set(content)
    snippet.title.set(title)
    snippet.tags ++= Tag.getTagList(tags)
    snippet.save
    PostServer ! snippet
  }

  private def sendMessage(msg: String) = {
    val snippet = CodeSnippet.create
    snippet.content.set(msg)
    snippet.save
    PostServer ! snippet
  }

  override def lowPriority = {
    case msg: List[CodeSnippet] =>
      posts = msg
      reRender(false)
    case msg: CodeSnippet =>
  
         Console.println("=========cometActor.Current Tag Filter>"+currTagFilter.openOr(""))
      posts = if(msg.tags.exists(tag => tag == currTagFilter.openOr(""))){
        Console.println("=========cometActor.CodeSnippet.contain>"+currTagFilter.openOr(""))
        msg :: posts
      }else
      {
        Console.println("=========cometActor.CodeSnippet.NOTcontain>"+currTagFilter.openOr(""))
        posts
      }
        reRender(false)
    case msg: Box[Tag] =>{
      Console.println("=========cometActor.Box[Tag]>"+msg.openOr(""))
      currTagFilter = msg
      Console.println("=========cometActor.Current Tag Filter>"+currTagFilter.openOr(""))
      posts = getPosts(msg)
      reRender(false)
      //TODO change reRender to be partialUpdate
      //partialUpdate(SetHtml("postTemplate",))
    }
  }
}

object PostServer extends LiftActor with ListenerManager {
  var posts: List[CodeSnippet] = CodeSnippet.findAll()
  def createUpdate = posts
  override def lowPriority = {
    case msg: CodeSnippet => {
    	updateListeners(msg)
    }
  }
}