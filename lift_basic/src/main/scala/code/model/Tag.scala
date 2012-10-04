package code.model

import net.liftweb.mapper._
import code.model._
import net.liftweb.sitemap.Loc
import net.liftweb.sitemap.Loc.If
import net.liftweb.http.RedirectResponse
import net.liftweb.sitemap.Loc.LocGroup
import net.liftweb.util.StringHelpers
import scala.collection.mutable.ListBuffer
import net.liftweb.common.{Empty,Full,Failure,Box}


class Tag extends LongKeyedMapper[Tag] with IdPK 
									with ManyToMany
									with CreatedUpdated
{
	def getSingleton = Tag
	object name extends MappedString(this,100)
	object posts extends MappedManyToMany(SnippetTags, SnippetTags.tag, SnippetTags.codeSnippet, CodeSnippet)
	
	def getPosts = {
	  posts.all
	}
	

}
object Tag extends Tag with LongKeyedMetaMapper[Tag] with CRUDify[Long, Tag]{
	  override def dbTableName = "Tag"
	  override def displayName = "Tag"
	  override def createMenuLocParams: List[Loc.AnyLocParam] =  {
			  List(If(User.loggedIn_? _, () => RedirectResponse("/login")), LocGroup("General"))
	  }
	  override def showAllMenuLocParams: List[Loc.AnyLocParam] =  {
			  List(If(User.loggedIn_? _, () => RedirectResponse("/login")), LocGroup("General"))
	  }
	  override def createMenuName = "New Tag"
	  //get a list of tags from a string
	  def getTagList(s :String) : List[Tag] = {
	  val tagList = new ListBuffer[Tag]
	  val strs = StringHelpers.roboSplit(s,",")
	  strs.foreach(s => {
	    val oldTag = Tag.find(By(Tag.name,s))
	    oldTag match {
	      case Full(tag) => tagList +=tag
	      case Empty =>{
	         val tag = Tag.create
	         tag.name.set(s)
		     tag.save
		     tagList += tag
	      }
	    }
	  })
	  tagList.toList
	}
}



