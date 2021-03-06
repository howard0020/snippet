package code.model

import net.liftweb.http.RedirectResponse
import net.liftweb.mapper._
import net.liftweb.sitemap.Loc.If
import net.liftweb.sitemap.Loc.LocGroup
import net.liftweb.sitemap.Loc
import net.liftweb.common.Full
import net.liftweb.common.Empty
import net.liftweb.common.Box
import code.model.post.BlockModel

class PostModel extends LongKeyedMapper[PostModel] with ManyToMany
  with CreatedUpdated
  with OneToMany[Long, PostModel] {
  def getSingleton = PostModel
  def primaryKeyField = id
  object title extends MappedString(this, 500)
  object id extends MappedLongIndex(this)
  
  object Author extends MappedLongForeignKey(this, UserModel)
  
  
  object content extends MappedTextarea(this, 2048)
  object blocks extends MappedOneToMany(BlockModel, BlockModel.post, OrderBy(BlockModel.id, Ascending))
  object tags extends MappedManyToMany(SnippetTags, SnippetTags.posts, SnippetTags.tag, TagModel)
  object groups extends MappedManyToMany(GroupPosts,GroupPosts.posts,GroupPosts.groups,GroupModel)
  

  def getTags = {
    var str = ""
    tags.foreach(t => str += t.name.get + ",")
    str.dropRight(1)
  }

  def getAuthor: Box[UserModel] = UserModel.findByKey(Author.get)

  /*  // before deleting a post, notify appropriate ToCModel that we are about to delete a post
  override def delete_! = {
    getAuthor match {
      case Full(user) =>
        user.toc.notifyPostDelete(this) match {
          case true => super.delete_!
          case false => false
        }
      case _ => false
    }
  }*/
  //val name = author.username
  //val imgDir = author.iconURL

}
object PostModel extends PostModel with LongKeyedMetaMapper[PostModel]
  with CRUDify[Long, PostModel] {
  //override def pageWrapper(body: NodeSeq) = <lift:surround with="admin" at="content">{body}</lift:surround>
  //override def calcPrefix = List("admin",_dbTableNameLC)
  override def displayName = "Snippet"

  // when a new post is created(inserted into database), this method will be called
  override def afterCreate = createPostCallback _ :: super.afterCreate

  private def createPostCallback(post: PostModel): Unit = {
    println("before create")
    val toc = post.getAuthor.get.toc
    println("save: " + toc.saved_?)
    toc.notifyPostAdd(post)
    println("after create")
  }
  override def afterDelete = deletePostCallback _ :: super.afterDelete

  private def deletePostCallback(post: PostModel): Unit = {
    println("before delete")
    val toc = post.getAuthor.get.toc
    println("save: " + toc.saved_?)
    toc.notifyPostDelete(post)
    println("after delete")
  }

  /*  
	  override def createMenuLocParams: List[Loc.AnyLocParam] =  {
			  List(If(User.loggedIn_? _, () => RedirectResponse("/login")), LocGroup("General"))
	  }
	  override def showAllMenuLocParams: List[Loc.AnyLocParam] =  {
			  List(If(User.loggedIn_? _, () => RedirectResponse("/login")), LocGroup("General"))
	  }
	  override def createMenuName = "New Snippet"
	  */

  //override def showAllMenuLocParams = LocGroup("admin") :: Nil
  /*override def createMenuLocParams = LocGroup("admin") :: Nil*/
}