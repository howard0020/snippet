package code.model
import net.liftweb.mapper._
import net.liftweb.common._
import net.liftweb.http.S

class GroupModel extends LongKeyedMapper[GroupModel] with IdPK with ManyToMany {
	def getSingleton = GroupModel
	object Author extends MappedLongForeignKey(this, UserModel)
	object title extends MappedText(this)
	object public extends MappedBoolean(this)
	object description extends MappedTextarea(this,2048)
	object posts extends MappedManyToMany(GroupPosts,GroupPosts.groups,GroupPosts.posts,PostModel)
	
	def setAuthor(author :Box[UserModel]) = {
	  author match{
	    case Full(who) => Author.set(who.id)
	    case Empty => //nothing
	    case Failure(msg,_,_) => S.error(msg)
	  }
	}
	
}
object GroupModel extends GroupModel with LongKeyedMetaMapper[GroupModel] {
}