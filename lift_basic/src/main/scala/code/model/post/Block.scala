package code.model.post

import net.liftweb.mapper._
import code.model.CodeSnippet
class Block extends LongKeyedMapper[Block]{
	def getSingleton = Block
	def primaryKeyField =id
	object id extends MappedLongIndex(this)
	object content extends MappedTextarea(this,2048)
	object post extends MappedLongForeignKey(this,CodeSnippet)
	object meta extends MappedText(this)
}
object Block extends Block with LongKeyedMetaMapper[Block]{
  
}