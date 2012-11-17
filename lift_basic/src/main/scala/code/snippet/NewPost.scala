package code.snippet

import code.model.CodeSnippet
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.LiftScreen
import net.liftweb.http.S
import net.liftweb.mapper.By
import code.model.User

object NewPost extends LiftScreen {
  // here are the fields and default values
  val title = field(S ? "Title", "", trim)
  val content = textarea(S ? "Post Content", "", trim)

  override def validations = fieldsMustBeNonEmpty _ :: super.validations

  def fieldsMustBeNonEmpty(): Errors = {
    if (title.isEmpty() || content.isEmpty())
      "Email and password can't be empty."
    else
      Nil
  }

  def finish() {
    val snippet = CodeSnippet.create
    snippet.title.set(title)
    snippet.content.set(content)
    snippet.Author.set(User.currentUser.get.id)
    snippet.save
  }
}