package code
package model

import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http.S
import net.liftweb.sitemap.Loc.LocGroup

/**
 * The singleton that has methods for accessing the database
 */
object User extends User with MetaMegaProtoUser[User] {
  override def dbTableName = "users" // define the DB table name
  override def screenWrap = Full(<lift:surround with="default" at="content">
			       <lift:bind /></lift:surround>)
  // define the order fields will appear in forms and output
  override def fieldOrder = List(id, firstName, lastName, username, email, iconURL,
  locale, timezone, password)
  
  override def signupFields = List(username, email, password)

  override def editFields = List(username, email,password,iconURL)

  // comment this line out to require email validations
  override def skipEmailValidation = true
  
  override def loginXhtml = {
    (<form method="post" action={S.uri}><table><tr><td
              colspan="2">{S.??("log.in")}</td></tr>
          <tr><td>{userNameFieldString}</td><td><user:email /></td></tr>
          <tr><td>{S.??("password")}</td><td><user:password /></td></tr>
          <tr><td><a href={lostPasswordPath.mkString("/", "/", "")}
                >{S.??("recover.password")}</a></td><td><user:submit /></td></tr>
                <tr><td></td><td><lift:SignUp.FacebookNormal></lift:SignUp.FacebookNormal></td></tr></table>
     </form>)
  }
  
  override def globalUserLocParams = LocGroup("UserMenu")::super.globalUserLocParams
}

/**
 * An O-R mapped "User" class that includes first name, last name, password and we add a "Personal Essay" to it
 */
class User extends MegaProtoUser[User] {
  def getSingleton = User // what's the "meta" server
  
  object username extends MappedString(this,32){
    override def displayName = "User Name" 
  }
  
  object iconURL extends MappedText(this){
	  override def displayName = "Icon URL"
  }
  
  def AllPost = CodeSnippet.findAll(By(CodeSnippet.Author,this.id))
  
  def findUser(email : String) = User.find(By(User.email,email))
}

