package code.model

import code.model._
import net.liftweb.mapper._

class CodeSnippet extends LongKeyedMapper[CodeSnippet]
{
	def getSingleton = CodeSnippet
	def primaryKeyField = id
	object id extends MappedLongIndex(this)
	object Author extends MappedLongForeignKey(this, User)
	object content extends MappedTextarea(this , 2048)
	
}
object CodeSnippet extends CodeSnippet with LongKeyedMetaMapper[CodeSnippet]
{
  override def dbTableName = "CodeSnippet"
}