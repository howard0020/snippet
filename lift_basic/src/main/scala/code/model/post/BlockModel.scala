package code.model.post

import net.liftweb.mapper._
import code.model.PostModel
class BlockModel extends LongKeyedMapper[BlockModel]{
	def getSingleton = BlockModel
	def primaryKeyField =id
	object id extends MappedLongIndex(this)
	object content extends MappedTextarea(this,2048)
	object post extends MappedLongForeignKey(this,PostModel)
	object meta extends MappedText(this)
	def contentToJSString = { 
	  var result = "["
	  content.split("[\\r\\n]").foreach(s => result += "'"+ s + "',")
	  result = result.dropRight(1)
	  result += "].join('\\n')"
	  result
	}
}
object BlockModel extends BlockModel with LongKeyedMetaMapper[BlockModel]{

}