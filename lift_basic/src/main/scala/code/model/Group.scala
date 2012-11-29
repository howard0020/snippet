package code.model
import net.liftweb.mapper._

class Group extends LongKeyedMapper[Group] with IdPK{
	def getSingleton = Group
	object Author extends MappedLongForeignKey(this, User)
	object title extends MappedText(this)
	object types extends MappedBoolean(this)
}
object Group extends Group with LongKeyedMetaMapper[Group] {
  override def dbTableName = "Group"   
}