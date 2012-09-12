package code.model

import code.model._
import net.liftweb.mapper._

class KeyValuePair extends LongKeyedMapper[KeyValuePair]{
	def getSingleton = KeyValuePair
	def primaryKeyField = id
	object id extends MappedLongIndex(this)
	object key extends MappedString(this, 100)
	//This field has to be UTF-8 in order to store chinese
	object value extends MappedTextarea(this,2048)
	object published extends MappedBoolean(this)
	
}

object KeyValuePair extends KeyValuePair with LongKeyedMetaMapper[KeyValuePair]{
  override def dbTableName = "KeyValueMap"
}