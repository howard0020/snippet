package code.model
import net.liftweb.mapper._

class GroupPosts extends LongKeyedMapper[GroupPosts] with IdPK {
	def getSingleton = GroupPosts
	object groups extends MappedLongForeignKey(this, Group)
	object posts extends MappedLongForeignKey(this, Post)
}

object GroupPosts extends GroupPosts
	with LongKeyedMetaMapper[GroupPosts]{

}