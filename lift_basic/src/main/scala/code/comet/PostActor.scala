package code.comet

import scala.xml._
import code.model.PostModel
import net.liftweb.actor.LiftActor
import net.liftweb.http._
import net.liftweb.json._
import net.liftweb.util._
import net.liftweb.http.js._
import net.liftweb.http.js.JE._
import JsCmds._
import scala.xml.NodeSeq
import Helpers._
import code.model.TagModel
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.mapper.By
import net.liftweb.common.Failure
import net.liftweb.common.Box
import code.model.SnippetTags
import code.model.UserModel
import code.model.post.BlockModel
import scala.xml.Attribute
import scala.Null
import code.search.SearchQuery
import code.search.SearchEngine
import code.share.PostSnippet

class PostActor extends CometActor with CometListener {

  implicit val formats = net.liftweb.json.DefaultFormats

  private var currTagFilter: Box[TagModel] = Empty

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

  def getPosts(tag: Box[TagModel]): List[PostModel] = {
    tag match {
      case Full(theTag) => theTag.posts.all.reverse
      case Empty => PostModel.findAll().reverse
      case Failure(msg, _, _) =>
        S.error(msg)
        PostModel.findAll().reverse
    }
  }

  def searchPosts(queryBox: Box[String]): List[PostModel] = {
    queryBox match {
      case Full(queryString) => SearchEngine.searchPostByTitle(queryString)
      case Empty => PostModel.findAll()
      case Failure(msg, _, _) =>
        S.error(msg)
        PostModel.findAll()
    }
  }

  def registerWith = PostServer

  def render = PostSnippet.render(posts) &	"#initCodeBlock" #> Script(JE.JsRaw(initCodeBlockJS).cmd)

  def ajaxForm = SHtml.ajaxForm(JsRaw("editor.save();").cmd,
    (SHtml.textarea("", content = _, "id" -> "snippetTextArea")
      ++ SHtml.text("", title = _)
      ++ SHtml.text("Lift", tags = _)
      ++ SHtml.submitButton(() => {})
      ++ SHtml.hidden(() => postForm)))

  private def postForm = {
    val post = PostModel.create
    post.Author.set(UserModel.currentUser match {
      case Full(curUser) => curUser.id
      case Empty => -1
      case Failure(msg, _, _) => -1
    })
    post.content.set(content)
    post.title.set(title)
    post.tags ++= TagModel.getTagList(tags)
    post.save
    PostServer ! post
  }

  private def sendMessage(msg: String) = {
    val snippet = PostModel.create
    snippet.content.set(msg)
    snippet.save
    PostServer ! snippet
  }

  override def lowPriority = {
    case msg: List[PostModel] =>
      posts = msg
      reRender(false)
    case msg: PostModel =>
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
    case msg: Box[TagModel] => {
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
 // var posts: List[Post] = Post.findAll().reverse
 // def createUpdate = posts
  def createUpdate = Empty
  override def lowPriority = {
    case msg: PostModel => {
      updateListeners(msg)
    }
  }
}