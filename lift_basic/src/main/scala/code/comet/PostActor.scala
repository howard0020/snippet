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

class PostActor extends CometActor with CometListener {
  implicit val formats = net.liftweb.json.DefaultFormats

  private var posts: List[CodeSnippet] = Nil
  posts = CodeSnippet.findAll()

  def registerWith = PostServer

  def render = "#postForm" #> ajaxForm & "#postTemplate" #> bindText

  def bindText =
    ".post_content" #> (
      (ns: NodeSeq) => (
        posts.flatMap(p => (".content" #> scala.xml.Unparsed(p.content.get) & ".tag" #> "")(ns)
        			)
        ))

  def ajaxForm = SHtml.ajaxForm(JsRaw("editor.save();").cmd, (SHtml.textarea("", sendMessage _, "id" -> "snippetTextArea") ++ SHtml.submitButton(() => {})))

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