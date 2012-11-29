package code.snippet
import code.share.PostSnippet
import code.model.User
import code.model.Post
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
import code.model.ToCModel
import net.liftweb.common.{ Full, Empty, Failure }
import scala.xml.Text
import net.liftweb.http.js.JsCmds.Run
import net.liftweb.http.js.JsCmds.JsCrVar
import net.liftweb.http.S
import code.model.ToCModel._
import scala.xml.Elem
import scala.collection.immutable.Queue
import code.share.SiteConsts

class Profile {

  private val profileUser: User = {
    val userIDString = S.param("id") match {
      case Full(id) => id
      case Empty => User.loggedIn_? match {
        case true => User.currentUserId match {
          case Full(id) => id
          case _ => throw new RuntimeException("Error: fail to retrieve user id.")
        }
        case false =>
          println("Access control should've prevented this from happening")
          S.redirectTo(SiteConsts.LOGIN_URL)
      }
      case Failure(msg, _, _) =>
        S.error(msg)
        S.redirectTo(SiteConsts.LOGIN_URL)
    }
    val id = userIDString.toLong
    User.findByKey(id) match {
      case Full(u) => u
      case Empty =>
        throw new RuntimeException("Access control should've prevented this from happening")
      case Failure(msg, _, _) =>
        throw new RuntimeException("Failed in retrieving user")
    }
  }

  private val isCurrentUserProfile: Boolean = {
    User.loggedIn_? match {
      case false => false
      case true => // user has logged in
        profileUser.id == User.currentUserId.get.toLong
    }
  }

  private lazy val tocTree = profileUser.toc.tree

  private val isToCEditable: Boolean = {
    isCurrentUserProfile
  }

  // snipppets
  def picktemplate = {
    val tplName = if (isCurrentUserProfile) "myprofile" else "otherprofile"
    "%s ^*".format(tplName) #> "right hand side value is ignored"
  }

  def posts = {
    val now = System.nanoTime

    val posts = tocTree.posts
    val micros = (System.nanoTime - now) / 1000

    println("Posts: %d microseconds".format(micros))

    PostSnippet.render(posts)
  }

  def tblofcontent = {
    val now = System.nanoTime
    val children = tocTree.tocposts

    val res = getView(children)
    val micros = (System.nanoTime - now) / 1000

    println("tblofcontent: %d microseconds".format(micros))

    "#tblofcontent *" #> res

  }

  def tocUpdate(xhtml: NodeSeq): NodeSeq = {
    val tocEditable = isToCEditable
    bind("tblofcontent",
      xhtml,
      "toc_update_listener" -> clientToCUpdateListener,
      "is_toc_editable" -> clientToCEditable)
  }

  private def clientToCUpdateListener = {
    Script(
      Function("clientToCUpdateListener", List("sourceID", "destID", "hitModeNum"),
        isToCEditable match {
          case false => Noop
          case true =>
            SHtml.jsonCall(
              JsRaw("{source: sourceID, dest: destID, hitMode: hitModeNum}"),
              (tocGeneric: Any) => { // List[Map[String,Any]
                if (isToCEditable) {
                  tocGeneric match {
                    case moveObj: Map[String, String] =>
                      if (!moveObj.contains("source") || !moveObj.contains("dest") || !moveObj.contains("hitMode"))
                        Noop
                      else {
                        val source = decodeID(moveObj("source"))
                        val dest = decodeID(moveObj("dest"))
                        val hitMode = moveObj("hitMode") match {
                          case "over" => Over()
                          case "after" => After()
                          case "before" => Before()
                          case _ => throw new RuntimeException("Profile.scala: unknown hitmode")
                        }
                        if (tocTree.move(source, dest, hitMode)) {
                          // send message to comet actor to rerender
                          for (session <- S.session) {
                            session.sendCometActorMessage("ProfilePostActor", Full("ProfilePostActor"), "rerender")
                          }
                        }
                      }
                    case _ => throw new RuntimeException("update in TblOfContent.scala: to do handle this error")
                  }
                }
                Noop
              })._2.cmd
        }))
  }

  private def clientToCEditable = {
    Script(
      Function("isToCEditable", Nil,
        {
          JsCrVar("tocEditable", isToCEditable) &
            Run("return tocEditable;")
        }))
  }

  private def getView(posts: List[ToCPost]): NodeSeq = {
    var nodes = Queue[Elem]()
    for (post <- posts) {

      val className = post.isFolder match {
        case true => "folder"
        case false => ""
      }

      nodes += <li id={ encodeID(post.id) } class={ className }>
                 <div><span class="disclose"><span></span></span>{ Text(post.title) }</div>
                 {
                   if (post.children != Nil)
                     getView(post.children)
                 }
               </li>
    }
    <ol class="sortable">{ nodes }</ol>
  }

  private def encodeID(id: Long): String = {
    "tree_" + id
  }

  private def decodeID(encodedID: String): Long = {
    encodedID.split("_").last.toLong
  }
}