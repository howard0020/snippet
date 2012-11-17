package code.search
import code.snippet.Post
import code.model.CodeSnippet
import net.liftweb.mapper.Like

object SearchEngine {

  def searchPostByTitle(queryString:String): List[CodeSnippet] = {
    CodeSnippet.findAll(Like(CodeSnippet.title, "%" + queryString + "%"))
  }

  def searchPostByContent(queryString:String): List[CodeSnippet] = {
    CodeSnippet.findAll(Like(CodeSnippet.content, "%" + queryString + "%"))
  }
  
  /**
   * search for title only
   * To Do: Change Query to allow search for both title and content
   * return all posts where the queryString appears either in the post's title or content
   */
  def searchPost(queryString:String): List[CodeSnippet] = {
    searchPostByTitle(queryString)
  }
  
}