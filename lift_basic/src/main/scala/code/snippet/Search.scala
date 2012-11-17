package code.snippet

import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.NodeSeq

import code.search.SearchEngine
import code.search.SearchQuery
import net.liftweb.common.Empty
import net.liftweb.common.Failure
import net.liftweb.common.Full
import net.liftweb.http.SHtml.BasicElemAttr
import net.liftweb.http.SHtml.ajaxText
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.S
import net.liftweb.util.Helpers.bind
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.Helpers.strToSuperArrowAssoc

class Search {

  def searchPosts(xhtml: NodeSeq): NodeSeq = {
    bind("search", xhtml,
      "textarea" ->
        ajaxText("", query => {
          for (session <- S.session) {
            session.sendCometActorMessage("PostActor", Full("PostActor"), SearchQuery(Full(query)))
          }
          Noop
        }))
  }

  def searchPostResult = {
    val queryString = S.param("queryString") openOr ""
    val posts = SearchEngine.searchPost(queryString)
    "#postTemplate *" #> {
      ".post_wrapper" #> (
        (ns: NodeSeq) => (posts.flatMap(p => (
          ".post_author_image [src]" #> (p.getAuthor match {
            case Full(author) =>
              if (author.iconURL.get.equals("")) "http://profile.ak.fbcdn.net/static-ak/rsrc.php/v2/yL/r/HsTZSDw4avx.gif"
              else author.iconURL.get
            case Empty => ""
            case Failure(msg, _, _) => "Error"
          }) &
          ".post_author_name *" #> (p.getAuthor match {
            case Full(author) => author.username.get
            case Empty => ""
            case Failure(msg, _, _) => "Error"
          }) &
          ".post_created_date *" #> (if (p.createdAt == null) "" else p.createdAt.toString()) &
          ".post_title *" #> (if (p.title == null) "" else p.title.toString()) &
          ".post_content *" #> scala.xml.Unparsed(p.content.get) &
          ".tag *" #> (if (p.getTags.equals("")) "" else ("tags: " + p.getTags)))(ns))))
    }
  }
}
