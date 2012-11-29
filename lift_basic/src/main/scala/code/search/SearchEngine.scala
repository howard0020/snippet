package code.search

import code.model.Post
import net.liftweb.mapper.Like

object SearchEngine {

  def searchPostByTitle(queryString:String): List[Post] = {
    Post.findAll(Like(Post.title, "%" + queryString + "%"))
  }

  def searchPostByContent(queryString:String): List[Post] = {
    Post.findAll(Like(Post.content, "%" + queryString + "%"))
  }
  
  /**
   * search for title only
   * To Do: Change Query to allow search for both title and content
   * return all posts where the queryString appears either in the post's title or content
   */
  def searchPost(queryString:String): List[Post] = {
    searchPostByTitle(queryString)
  }
  
}