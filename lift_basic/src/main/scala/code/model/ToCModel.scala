package code.model

import net.liftweb.json.JsonAST.JArray
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL.jobject2assoc
import net.liftweb.json.JsonDSL.pair2Assoc
import net.liftweb.json.JsonDSL.pair2jvalue
import net.liftweb.json.JsonDSL.seq2jvalue
import net.liftweb.json.Serialization.{ read, write }
import net.liftweb.json.{ compact, render }
import net.liftweb.json.parse
import net.liftweb.json.DefaultFormats
import net.liftweb.mapper.MappedField.mapToType
import net.liftweb.mapper.By
import net.liftweb.mapper.CreatedUpdated
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.ManyToMany
import net.liftweb.mapper.MappedLongIndex
import net.liftweb.mapper.MappedLong
import net.liftweb.mapper.MappedText
import code.model.ToCModel.ToCPost
import net.liftweb.json.parse
import net.liftweb.common.Full
import ToCModel.Tree
import net.liftweb.http.S

class ToCModel extends LongKeyedMapper[ToCModel] with ManyToMany with CreatedUpdated {
  implicit val formats = DefaultFormats

  def getSingleton = ToCModel
  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object content extends MappedText(this)
  object Author extends MappedLong(this)

  lazy val tree = {
    new Tree(this)
  }

  def notifyPostDelete(post: Post) = {
    tree.moveUpChildren(post.id)
    tree.save
  }

  def notifyPostAdd(post: Post) = {
    tree.add(post)
    tree.save

    for (session <- S.session) {
      session.sendCometActorMessage("ProfilePostActor", Full("ProfilePostActor"), "rerender")
    }

  }
}

object ToCModel extends ToCModel with LongKeyedMetaMapper[ToCModel] {
  override def dbTableName = "TblOfContent"

  override def create = {
    val toc = super.create
    toc.content.set("[]")
    toc
  }

  def createFor(user: User): ToCModel = {
    val toc = ToCModel.create

    val posts = Post.findAll(By(Post.Author, user)).map {
      snippet => PostToToCPost(snippet)
    }

    val content: String = write(posts)
    toc.Author.set(user.id)
    toc.content.set(content)
    toc.save
    toc
  }

  // precondition: map idToPost has key tocPost.id
  private def TocPostToListPosts(tocPost: ToCPost, idToPost: Map[Long, Post]): List[Post] = {
    var posts = List(idToPost(tocPost.id))
    posts ++ tocPost.children.flatMap(childPost => TocPostToListPosts(childPost, idToPost))
  }

  private def userPostIdToPostMap(userId: Long) = {
    val userPosts = Post.findAll(By(Post.Author, userId))
    var idToPost = Map[Long, Post]()
    for (post <- userPosts)
      idToPost += (post.id.toLong -> post)
    idToPost
  }

  private def createToC(posts: List[ToCPost]): ToCPost = {
    ToCPost(-1, "Table Of Content", posts, true)
  }

  implicit def PostToToCPost(snippet: Post): ToCPost = {
    ToCPost(snippet.id, snippet.title, Nil, false)
  }
  // clone post and replace title with most updated titel from database
  private def updateTitles(posts: List[ToCPost], idToPost: Map[Long, Post]): List[ToCPost] = {
    for (post <- posts) yield {
      val id = post.id
      val isFolder = post.isFolder
      val title = isFolder match {
        case true => post.title
        case false => idToPost(id).title.toString
      }
      val children = updateTitles(post.children, idToPost)
      ToCPost(id, title, children, isFolder)
    }
  }

  case class ToCPost(id: Long, var title: String, var children: List[ToCPost], isFolder: Boolean = false)

  class Tree(val toc: ToCModel) {
    private val root: ToCPost = {
      val content = """{"id":-1,"title": "temp node", "isFolder": true, "children": """ + toc.content + "}"
      parse(content).extract[ToCPost]
    }

    lazy val posts = {
      println("Tree.posts")
      val id2postMap = ToCModel.userPostIdToPostMap(toc.Author)
      root.children.flatMap(rootPost => ToCModel.TocPostToListPosts(rootPost, id2postMap))
    }

    lazy val tocposts: List[ToCPost] = root.children

    /**
     * Move the given post to the appropriate location
     */
    def move(sourceID: Long, destID: Long, hitMode: HitMode): Boolean = {
      // delete the node first
      delete(sourceID, persist = false) match {
        case Some(sourcePost) =>
          // insert the node
          val f = (target: ToCPost, parent: ToCPost, left: List[ToCPost], right: List[ToCPost]) => {
            hitMode match {
              case h: Before =>
                val combine = List(sourcePost, target)
                parent.children = left ::: combine ::: right
              case h: After =>
                val combine = List(target, sourcePost)
                parent.children = left ::: combine ::: right
              case h: Over =>
                target.children = target.children ::: List(sourcePost)
            }
          }
          breadthTraversal(root, destID, f)
          save
          true
        case None =>
          // do nothing
          false
      }
    }

    /**
     * Replace the given post with all of its direct children
     */
    def moveUpChildren(id: Long) {
      require(id > 0)
      breadthTraversal(root, id,
        (target, parent, left, right) => {
          parent.children = left ::: target.children ::: right
        })
      save
    }

    /**
     * Helper method for methods that need to traverse the tree
     */
    private def breadthTraversal(post: ToCPost, targetID: Long, f: (ToCPost, ToCPost, List[ToCPost], List[ToCPost]) => Unit): Boolean = {
      val (ls1, ls2) = post.children.span((p) => p.id != targetID)
      ls2 match {
        case Nil => // didn't find the target post
          // search in direct children
          for (p <- ls1) {
            if (breadthTraversal(p, targetID, f)) {
              return true
            }
          }
          return false
        case target :: ls3 => // found the target post
          f(target, post, ls1, ls3)
          true
      }
    }
    /**
     * Delete the given post from the tree
     */
    private def delete(id: Long, persist: Boolean = true): Option[ToCPost] = {
      require(id > 0)
      var ret: Option[ToCPost] = None

      val deleteSuccess = breadthTraversal(root, id,
        (target, parent, left, right) => {
          parent.children = left ::: right
          ret = Some(target)
        })

      if (persist)
        save

      if (deleteSuccess)
        ret
      else
        None
    }

    /**
     * Add given post to tree
     */
    def add(post: ToCPost) = {
      root.children = post :: root.children
    }

    /**
     *  update titles of all posts
     */
    def refresh = {

    }

    def toJSONString(ignoreRoot: Boolean = true): String = {
      if (ignoreRoot)
        write(root.children)
      else
        write(root)
    }

    /**
     * convert this tree to json string and save it in the ToCModel database table
     */
    def save = {
      toc.content.set(write(root.children))
      toc.save
    }
  }

  case class HitMode
  case class Before extends HitMode
  case class After extends HitMode
  case class Over extends HitMode
}