package code.search

import code.model.PostModel
import net.liftweb.mapper.Like

object SearchEngine {

  def searchPostByTitle(queryString:String): List[PostModel] = {
    PostModel.findAll(Like(PostModel.title, "%" + queryString + "%"))
  }

  def searchPostByContent(queryString:String): List[PostModel] = {
    PostModel.findAll(Like(PostModel.content, "%" + queryString + "%"))
  }
  
  /**
   * search for title only
   * To Do: Change Query to allow search for both title and content
   * return all posts where the queryString appears either in the post's title or content
   */
  def searchPost(queryString:String): List[PostModel] = {
    searchPostByTitle(queryString)
  }
  
}