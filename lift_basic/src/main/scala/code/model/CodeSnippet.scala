package code.model

import code.model._
import net.liftweb.mapper._
import scala.xml.NodeSeq
import net.liftweb.sitemap.Loc.LocGroup

class CodeSnippet extends LongKeyedMapper[CodeSnippet]
{
	def getSingleton = CodeSnippet
	def primaryKeyField = id
	object id extends MappedLongIndex(this)
	object Author extends MappedLongForeignKey(this, User)
	object content extends MappedTextarea(this , 2048)
	
}
object CodeSnippet extends CodeSnippet with LongKeyedMetaMapper[CodeSnippet]
									   with CRUDify[Long, CodeSnippet]
{
	  override def dbTableName = "CodeSnippet"
	  //override def pageWrapper(body: NodeSeq) = <lift:surround with="admin" at="content">{body}</lift:surround>
	  //override def calcPrefix = List("admin",_dbTableNameLC)
	  override def displayName = "CodeSnipper"
	  //override def showAllMenuLocParams = LocGroup("admin") :: Nil
	 // override def createMenuLocParams = LocGroup("admin") :: Nil
}