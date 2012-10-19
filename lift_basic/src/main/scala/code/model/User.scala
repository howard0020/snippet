package code
package model

import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http.S
import net.liftweb.sitemap.Loc.LocGroup
import code.gh.Auth

/**
 * The singleton that has methods for accessing the database
 */
object User extends User with MetaMegaProtoUser[User] {
  override def dbTableName = "users" // define the DB table name
  override def screenWrap = Full(<lift:surround with="default" at="content">
                                   <lift:bind/>
                                 </lift:surround>)
  // define the order fields will appear in forms and output
  override def fieldOrder = List(id, firstName, lastName, username, email, iconURL,
    locale, timezone, password)

  override def signupFields = List(username, email, password)

  override def editFields = List(username, email, password, iconURL)

  // comment this line out to require email validations
  override def skipEmailValidation = true

  override def loginXhtml = {
    (<div id="user_login_wrapper">
       <form id="user_login_form" method="post" action={ S.uri }>
         <div class="user_login_otherlogin">
           <div class="user_login_facebook"><lift:SignUp.FacebookNormal></lift:SignUp.FacebookNormal></div>
           <div class="user_login_github"><a href={ Auth.login_url }>Github Login</a></div>
         </div>
         <div class="user_login_regularlogin">
           <div class="form_row">
             <span class="user_login_username_text form_left">{ S.??("log.in") }</span>
             <div class="user_login_username_textbox form_right"><user:email/></div>
           </div>
           <div class="form_row">
             <span class="user_login_password_text form_left">{ S.??("password") }</span>
             <div class="user_login_username_textbox form_right"><user:password/></div>
           </div>
           <div class="user_login_lostpassword"><a href={ lostPasswordPath.mkString("/", "/", "") }>{ S.??("recover.password") }</a></div>
           <div class="loginbutton"><user:submit/></div>
         </div>
       </form>
     </div>)
  }

  //<span>{ userNameFieldString }</span>

  override def globalUserLocParams = LocGroup("UserMenu") :: super.globalUserLocParams
}

/**
 * An O-R mapped "User" class that includes first name, last name, password and we add a "Personal Essay" to it
 */
class User extends MegaProtoUser[User] {
  def getSingleton = User // what's the "meta" server

  object username extends MappedString(this, 32) {
    override def displayName = "User Name"
  }

  object iconURL extends MappedText(this) {
    override def displayName = "Icon URL"
  }

  def AllPost = CodeSnippet.findAll(By(CodeSnippet.Author, this.id))

  def findUser(email: String) = User.find(By(User.email, email))

}

