package code.model

import net.liftweb.mapper._
import code.model._

class Tag extends LongKeyedMapper[Tag] with IdPK
{
	def getSingleton = Tag
	object name extends MappedString(this,100)
	
}
object Tag extends Tag with LongKeyedMetaMapper[Tag] {
	
}