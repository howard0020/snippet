package code.snippet

import scala.xml.NodeSeq
import code.search.SearchEngine
import net.liftweb.http.js._
import JE.JsRaw
import JsCmds.Function
import JsCmds.{ Noop, Script }
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers.{ bind, strToCssBindPromoter, strToSuperArrowAssoc }
import net.liftweb.util.IterableConst.itStringPromotable
import net.liftweb.json._
import Serialization.write
import JsonAST.{ JArray, JValue, JInt, JObject }
import JsonDSL.{ pair2Assoc, seq2jvalue, string2jvalue }
import JsonDSL._
import net.liftweb.mapper.By
import code.model.User
import code.model.CodeSnippet
import code.model.ToCModel
import net.liftweb.common.{ Full, Empty, Failure }
import scala.xml.Text
import net.liftweb.http.js.JsCmds.Run
import net.liftweb.http.js.JsCmds.JsCrVar
import net.liftweb.http.S
import code.model.ToCModel._
import scala.xml.Elem
import scala.collection.immutable.Queue
import code.model.SampleData

class TblOfContent {

  def render = {
    val children = profileUser.getToCPosts

    "#tblofcontent *" #> getView(children)
  }

  private def getView(posts: List[ToCPost]): NodeSeq = {
    var nodes = Queue[Elem]()
    for (post <- posts) {
      val children = post.children match {
        case None => Text("")
        case Some(ls) => getView(ls)
      }
      val className = post.isFolder match {
        case true => "folder"
        case false => ""
      }
      nodes += <li id={ encodeID(post.id) } class={ className }>
                 { Text(post.title) }
                 { children }
               </li>
    }
    <ul>{ nodes }</ul>
  }

  private def encodeID(id: Long): String = {
    "tree_" + id
  }
  private def decodeID(encodedID: String): Long = {
    encodedID.split("_").last.toLong
  }

  def posts = {
    print("==================================posts")
    "#post_title" #> {
      SearchEngine.searchPost("").map(p => {
        "span * " #> p.title.toString &
          "span [data]" #> p.id.toString
      })
    }
  }

  private def toJsonString(posts: List[Map[String, Any]]): String = {
    val tocPosts = convertListToToCPost(posts)
    write(tocPosts)
  }
  private def convertListToToCPost(posts: List[Map[String, Any]]): List[ToCPost] = {
    posts.map { post =>
      {
        val id = decodeID(post("key").toString)
        val title = post("title").toString
        val isFolder = post("isFolder").toString().toBoolean
        val children = post.contains("children") match {
          case false => None
          case true =>
            post("children") match {
              case Nil => None
              case ls: List[Map[String, Any]] =>
                Some(convertListToToCPost(ls))
              case _ => throw new RuntimeException("TblOfContent.convertListToToCPost: unexpected type error")
            }
        }
        ToCPost(id, title, children, isFolder)
      }
    }
  }
  private def updateToC(json: String) {
    val toc = profileUser.toc
    toc.content.set(json)
    toc.save
  }

  private val profileUser: User = {
    val userIDString = S.param("id")
    val id = userIDString.get.toLong
    User.findByKey(id) match {
      case Full(u) => u
      case Empty =>
        S.error("No such user exists")
        throw new RuntimeException("gotta find out how to handle this unexpected error")
      case Failure(msg, _, _) =>
        S.error("profile_user: " + msg)
        throw new RuntimeException("gotta find out how to handle this unexpected error")
    }
  }

  private def isCurrentUserProfile: Boolean = {
    User.loggedIn_? match {
      case false => false
      case true => // user has logged in
        profileUser.id == User.currentUserId.get.toLong
    }
  }
  private def isToCEditable: Boolean = {
    isCurrentUserProfile
  }

  def update(xhtml: NodeSeq): NodeSeq = {
    val tocEditable = isToCEditable
    bind("tblofcontent",
      xhtml,
      "update_toc" ->
        (tocEditable match {
          case false => Text("")
          case true =>
            Script(
              Function("updateToC", Nil,
                SHtml.jsonCall(
                  JsRaw("$('#tblofcontent').dynatree('getTree').toDict().children"),
                  (tocGeneric: Any) => { // List[Map[String,Any]
                    if (tocEditable) {
                    val toc = tocGeneric match {
                      case toc: List[Map[String, Any]] => toc
                      case _ => throw new RuntimeException("update in TblOfContent.scala: to do handle this error")
                    }

                    val tocJsonString = toJsonString(toc)
                    updateToC(tocJsonString)
                    }
                    Noop
                  })._2.cmd))
        }),
      "is_toc_editable" -> Script(
        Function("isToCEditable", Nil,
          {
            JsCrVar("tocEditable", tocEditable) &
              Run("return tocEditable;")
          })),
       "refresh" -> SHtml.ajaxButton("Refresh",
           () => {
             profileUser.updateToC
             SampleData.run
             Noop
           }
           )
      )
  }
}