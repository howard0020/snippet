package code.model

object SampleData {
	
  def run {
    if (!User.loggedIn_?)
      return
      
    addPosts
  }
  
  def addPosts {
    for(i <- 1 until 15) {
    	val post = CodeSnippet.create
    	post.title.set("Scala " + i)
    	post.content.set("Scala Content " + i)
    	post.Author.set(User.currentUser.get.id)
    	post.save
    }
  }
}