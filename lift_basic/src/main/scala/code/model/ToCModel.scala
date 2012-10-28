package code.model

/*
import code.model.ToCModel.StrippedToCAST
import code.model.ToCModel.StrippedToCASTtoToCAST
import code.model.ToCModel.StrippedToCString
import code.model.ToCModel.StrippedToCStringtoToCAST
import code.model.ToCModel.ToCAST
import code.model.ToCModel.ToCASTtoToCString
import code.model.ToCModel.ToCString
import code.model.ToCModel.ToCStringToString
*/
import net.liftweb.json.JsonAST.JArray
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL.jobject2assoc
import net.liftweb.json.JsonDSL.pair2Assoc
import net.liftweb.json.JsonDSL.pair2jvalue
import net.liftweb.json.JsonDSL.seq2jvalue
import net.liftweb.json.Serialization.{ read, write }
import net.liftweb.json.{ compact, render }
import net.liftweb.json.parse
import net.liftweb.json.DefaultFormats
import net.liftweb.mapper.MappedField.mapToType
import net.liftweb.mapper.By
import net.liftweb.mapper.CreatedUpdated
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.ManyToMany
import net.liftweb.mapper.MappedLongIndex
import net.liftweb.mapper.MappedLong
import net.liftweb.mapper.MappedText
import code.model.ToCModel.ToCPost
import net.liftweb.json.parse

class ToCModel extends LongKeyedMapper[ToCModel] with ManyToMany with CreatedUpdated {
  implicit val formats = DefaultFormats

  def getSingleton = ToCModel
  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object content extends MappedText(this)
  object Author extends MappedLong(this)

  def getToCPosts: List[ToCPost] = {
    val content = """{"id":-1,"title": "temp node", "isFolder": true, "children": """ + this.content + "}"
    println("getToCPosts: " + content)
    val post = parse(content).extract[ToCPost]
    post.children match {
      case Some(ls: List[ToCPost]) => ls
      case _ => Nil
    }
  }

  def updateTitles {
    val userPosts = CodeSnippet.findAll(By(CodeSnippet.Author, this.Author))
    var idToTitle = Map[Long, String]()
    for (post <- userPosts)
      idToTitle += (post.id.toLong -> post.title)
      
    val posts = getToCPosts
    val updatedPosts = ToCModel.updateTitles(posts, idToTitle)
    val jsonString = write(updatedPosts)
    content.set(jsonString)
    save
  }
}
object ToCModel extends ToCModel with LongKeyedMetaMapper[ToCModel] {
  override def dbTableName = "TblOfContent"

  def createFor(user: User): ToCModel = {
    val toc = ToCModel.create

    val posts = CodeSnippet.findAll(By(CodeSnippet.Author, user)).map {
      snippet => CodeSnippetToToCPost(snippet)
    }

    val content: String = write(posts)
    toc.Author.set(user.id)
    toc.content.set(content)
    toc.save
    toc
  }

  private def createToC(posts: List[ToCPost]): ToCPost = {
    ToCPost(-1, "Table Of Content", Some(posts), true)
  }

  implicit def CodeSnippetToToCPost(snippet: CodeSnippet): ToCPost = {
    ToCPost(snippet.id, snippet.title, None, false)
  }
  // clone post and replace title with most updated titel from database
  private def updateTitles(posts: List[ToCPost], idToTitle: Map[Long, String]): List[ToCPost] = {
    for (post <- posts) yield {
      val id = post.id
      val isFolder = post.isFolder
      val title = isFolder match {
        case true => post.title
        case false => idToTitle(id)
      }
      val children = post.children match {
        case None => None
        case Some(ls) =>
          Some(updateTitles(ls, idToTitle))
      }
      ToCPost(id, title, children, isFolder)
    }
  }

  case class ToCPost(id: Long, title: String, children: Option[List[ToCPost]], isFolder: Boolean = false)

}