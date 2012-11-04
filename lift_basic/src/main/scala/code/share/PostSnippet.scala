package code.share
import code.model.CodeSnippet
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq
import net.liftweb.common.Full
import net.liftweb.common.Empty
import net.liftweb.common.Failure
import code.model.post.Block
import scala.xml.Attribute
import scala.xml.Text
import scala.xml.Null

object PostSnippet {
  val profileLink = SiteConsts.INDEX_URL + "/" + "profile"

  def render(posts: List[CodeSnippet]) = {
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
        ".post_author_name [href]" #> (p.getAuthor match {
          case Full(author) => profileLink + "/" + author.id.get
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
        ".post_content *" #> {
          ".post_block" #>
            (p.blocks.map(b => "*" #> getBlockContent(b)))
        } &
        ".tag *" #> (if (p.getTags.equals("")) "" else ("tags: " + p.getTags)))(ns)))) }
  }
  
  // helper for rendering posts
  private  def getBlockContent(block: Block): NodeSeq = {
    if (block.meta.toString == "") {
      <div>{xml.Unparsed(block.content.is)}</div>
    } else {
      val text = Option(block.content.is)
      <textarea class="code-block-fields">{ text.getOrElse("") }</textarea> % Attribute(None, "mode", Text(block.meta.is), Null)
    }
  }
}