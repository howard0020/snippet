package code.model
import net.liftweb.mapper._
import net.liftweb.common._
import net.liftweb.http.S

class Group extends LongKeyedMapper[Group] with IdPK with ManyToMany {
	def getSingleton = Group
	object Author extends MappedLongForeignKey(this, User)
	object title extends MappedText(this)
	object public extends MappedBoolean(this)
	object description extends MappedTextarea(this,2048)
	object posts extends MappedManyToMany(GroupPosts,GroupPosts.groups,GroupPosts.posts,Post)
	
	def setAuthor(author :Box[User]) = {
	  author match{
	    case Full(who) => Author.set(who.id)
	    case Empty => //nothing
	    case Failure(msg,_,_) => S.error(msg)
	  }
	}
	
}
object Group extends Group with LongKeyedMetaMapper[Group] {
  override def dbTableName = "Group_t"   
}