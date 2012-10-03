package code.model


import net.liftweb.http.RedirectResponse
import net.liftweb.mapper._
import net.liftweb.sitemap.Loc.If
import net.liftweb.sitemap.Loc.LocGroup
import net.liftweb.sitemap.Loc
import net.liftweb.common.Full
import net.liftweb.common.Empty
import net.liftweb.common.Box


class CodeSnippet extends LongKeyedMapper[CodeSnippet] with ManyToMany with CreatedUpdated
{
	def getSingleton = CodeSnippet
	def primaryKeyField = id
	object id extends MappedLongIndex(this)
	object Author extends MappedLongForeignKey(this, User)
	object content extends MappedTextarea(this , 2048)
	object tags extends MappedManyToMany(SnippetTags, SnippetTags.codeSnippet, SnippetTags.tag, Tag)
	
	def getTags = {
		var str = ""
	    tags.foreach(t => str += t.name.get + ",")
	    str.dropRight(1)
	}
	
	def getAuthor:Box[User] =User.findByKey(Author.get)

	  
	  //val name = author.username
	  //val imgDir = author.iconURL
	
}
object CodeSnippet extends CodeSnippet with LongKeyedMetaMapper[CodeSnippet]
									   with CRUDify[Long, CodeSnippet]
{
	  override def dbTableName = "Posts"
	  //override def pageWrapper(body: NodeSeq) = <lift:surround with="admin" at="content">{body}</lift:surround>
	  //override def calcPrefix = List("admin",_dbTableNameLC)
	  override def displayName = "Snippet"
	  override def createMenuLocParams: List[Loc.AnyLocParam] =  {
			  List(If(User.loggedIn_? _, () => RedirectResponse("/login")), LocGroup("General"))
	  }
	  override def showAllMenuLocParams: List[Loc.AnyLocParam] =  {
			  List(If(User.loggedIn_? _, () => RedirectResponse("/login")), LocGroup("General"))
	  }
	  override def createMenuName = "New Snippet"
	    
	 

	  //override def showAllMenuLocParams = LocGroup("admin") :: Nil
	 /*override def createMenuLocParams = LocGroup("admin") :: Nil*/
}