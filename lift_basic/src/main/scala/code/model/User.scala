package code
package model

import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http.S
import net.liftweb.sitemap.Loc.LocGroup
import code.gh.Auth
import code.model.ToCModel.ToCPost

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
    (
      <form id="user_login_form" class="thirty_three_percent_inline_block" method="post" action={ S.uri }>
        <span class="user_form_title">Already a Member?</span>
        <div class="user_login_regularlogin">
          <div class="user_form_row">
            <span class="user_form_left">{ S.??("log.in") }</span>
            <div class="user_form_right user_form_input"><user:email/></div>
          </div>
          <div class="user_form_row">
            <span class="user_form_left">{ S.??("password") }</span>
            <div class="user_form_right user_form_input"><user:password/></div>
          </div>
          <!--<div class="user_login_lostpassword"><a href={ lostPasswordPath.mkString("/", "/", "") }>{ S.??("recover.password") }</a></div>-->
          <div class="user_form_button"><user:submit/></div>
        </div>
        <div class="user_login_otherlogin">
          <div class="user_login_facebook"><lift:SignUp.FacebookNormal></lift:SignUp.FacebookNormal></div>
          <div class="user_login_github"><a href={ Auth.login_url }>Github Login</a></div>
        </div>
      </form>)
  }
  override def lostPasswordXhtml = {
    (<form id="user_lostpassword_form" class="thirty_three_percent_inline_block" method="post" action={ S.uri }>
       <span class="user_form_title">Lost your password?</span>
       <span>{ S.??("enter.email") }</span>
       <div class="user_form_row">
         <span class="user_form_left">{ userNameFieldString }</span>
         <div class="user_form_right user_form_input"><user:email/></div>
       </div>
       <div class="user_form_button"><user:submit/></div>
     </form>)
  }
  override def signupXhtml(user: TheUserType) = {
    (<form id="user_signup_form" class="thirty_three_percent_inline_block" method="post" action={ S.uri }>
       <span class="user_form_title">Not a Member?</span>
       <div class="user_form_input">{ localForm(user, false, signupFields) }</div>
       <div class="user_form_button"><user:submit/></div>
     </form>)
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

    def toc: ToCModel = {
    ToCModel.find(By(ToCModel.Author, this.id)) match {
      case Full(t) => t
      case Empty => ToCModel.createFor(this)
      case Failure(msg, _, _) =>
        throw new RuntimeException("update_toc")
    }   
  }
  
  def getToCPosts: List[ToCPost] = {
    toc.getToCPosts
  }
  
  def updateToC {
    toc.updateTitles
  }
}

