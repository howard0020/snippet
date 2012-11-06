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
import code.search.SearchQuery
import code.search.SearchEngine
import code.share.PostSnippet

class PostActor extends CometActor with CometListener {

  implicit val formats = net.liftweb.json.DefaultFormats

  private var currTagFilter: Box[Tag] = Empty

  //for new post
  private var content = ""
  //for new post
  private var tags = ""

  private var title = ""
    
  private val initCodeBlockJS = 
    """
      |            $(document).ready(function() {
      |              initCodeBlock();
      |            });
    """.stripMargin
    
  private var posts = getPosts(Empty)

  def getPosts(tag: Box[Tag]): List[CodeSnippet] = {
    tag match {
      case Full(theTag) => theTag.posts.all.reverse
      case Empty => CodeSnippet.findAll().reverse
      case Failure(msg, _, _) =>
        S.error(msg)
        CodeSnippet.findAll().reverse
    }
  }

  def searchPosts(queryBox: Box[String]): List[CodeSnippet] = {
    queryBox match {
      case Full(queryString) => SearchEngine.searchPostByTitle(queryString)
      case Empty => CodeSnippet.findAll()
      case Failure(msg, _, _) =>
        S.error(msg)
        CodeSnippet.findAll()
    }
  }

  def registerWith = PostServer

  def render = PostSnippet.render(posts) &
  				"#initCodeBlock" #> Script(JE.JsRaw(initCodeBlockJS).cmd)

  def ajaxForm = SHtml.ajaxForm(JsRaw("editor.save();").cmd,
    (SHtml.textarea("", content = _, "id" -> "snippetTextArea")
      ++ SHtml.text("", title = _)
      ++ SHtml.text("Lift", tags = _)
      ++ SHtml.submitButton(() => {})
      ++ SHtml.hidden(() => postForm)))

  private def postForm = {
    val snippet = CodeSnippet.create
    snippet.Author.set(User.currentUser match {
      case Full(curUser) => curUser.id
      case Empty => -1
      case Failure(msg, _, _) => -1
    })
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
      currTagFilter match {
        case Full(currTag) => 
        //if there is a tag filter check if the new message have this tag
        if (msg.tags.exists(tag => tag == currTag)){  
          posts = msg :: posts
        }
        //if no filter are apply add the new message
        case Empty =>
          posts = msg :: posts
        case Failure(msg,_,_) => S.error(msg)
      }
      reRender(false)
    case msg: Box[Tag] => {
      currTagFilter = msg
      posts = getPosts(msg)
      reRender(false)
      //TODO change reRender to be partialUpdate
      //partialUpdate(SetHtml("postTemplate",))
    }
    case SearchQuery(query) => 
      posts = searchPosts(query)
      reRender(false)
  }
}

object PostServer extends LiftActor with ListenerManager {
 // var posts: List[CodeSnippet] = CodeSnippet.findAll().reverse
 // def createUpdate = posts
  def createUpdate = Empty
  override def lowPriority = {
    case msg: CodeSnippet => {
      updateListeners(msg)
    }
  }
}