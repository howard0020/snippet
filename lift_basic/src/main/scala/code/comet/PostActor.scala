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
import net.liftweb.util.Helpers._
import code.model.Tag
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.mapper.By
import code.snippet.Post
import net.liftweb.common.Failure
import net.liftweb.common.Box
import code.model.SnippetTags
import code.model.User

class PostActor extends CometActor with CometListener {
  implicit val formats = net.liftweb.json.DefaultFormats

  private var content = ""
  private var tags = ""
  private var title = ""
  var param = S.param("tag")
  private var posts: List[CodeSnippet] = param match {
    case Empty => CodeSnippet.findAll()
    case Full(text) =>{ 
      Console.println("=======> tag:" + text)
      Tag.find(By(Tag.name,text)).get.posts.toList}
    case Failure(msg,_,_) => {
      S.error(msg)
      CodeSnippet.findAll()
    }
  }

  def registerWith = PostServer
		  
  def render = "#postForm *" #> ajaxForm & "#postTemplate *" #> bindText

  def bindText =
    ".post" #> (
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
          ".post_created_date *" #> p.createdAt &
          ".post_title *" #> p.title &
          ".post_content *" #> scala.xml.Unparsed(p.content.get) & 
          ".tag *" #> (p.getTags))(ns))))

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
  }
}

object PostServer extends LiftActor with ListenerManager {
  var posts: List[CodeSnippet] = CodeSnippet.findAll()
  def createUpdate = posts
  override def lowPriority = {
    case msg: CodeSnippet => {
      posts = CodeSnippet.findAll()
      updateListeners()
    }
  }
}