package code.snippet
import code.model.Group
import code.share.SiteConsts
import net.liftweb.common.Full
import net.liftweb.http.LiftScreen
import net.liftweb.http.S
import code.model.User

class NewGroupForm extends LiftScreen{
    object group extends ScreenVar (Group.create)

    addFields(() => group.title)
    addFields(() => group.public)
    addFields(() => group.description)
    
	def finish = {
      Console.println("================Here")
    	S.notice("Thank you for adding "+group.is)
    	group.is.setAuthor(User.currentUser)
    	group.is.save
    	S.notice(group.is.toString+" Saved in the database")
	}
}