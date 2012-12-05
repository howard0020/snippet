package code.snippet


import net.liftweb.util.BindPlus._
import net.liftweb.util._
import code.model.UserModel
import Helpers._
import net.liftweb.common.{Full, Empty, Box}
import code.model.PostModel
import net.liftweb.common.Failure

class UserSnippet {
	
	def UserName = UserModel.currentUser match {
	  case Full(currUser) => currUser.username.get
	  case Empty =>  ""
	  case Failure(msg,_,_) => "msg"
	  case _=>	""
	}
	
  	def UserIcon = UserModel.currentUser match {
	  case Full(currUser) => currUser.iconURL.get
	  case Empty =>  ""
	  case Failure(msg,_,_) => "msg"
	  case _=>	""
	}
  	
	def CurrentUserNameExpandable =  "#profile_user_name [class]" #> (UserModel.loggedIn_? match{
	  case true => "expand"
	  case false => "collapse"
	})
	def CurrentUserName = ".userName *" #> UserName
	//bind to the src attribute 
	def CurrentUserIcon = ".profile_user_icon [src]" #> UserIcon
	
	def CurrentUserUrl = ".userUrl [href]" #> "/User/profile.html"
	
	def LogoutMenuItem = ".logout_url [href]"  #> "/user_mgt/logout"
	
	def loginForm = UserModel.login
	
	def CurrentUserProfile = CurrentUserName  & CurrentUserIcon & CurrentUserUrl  & LogoutMenuItem
	
	def loginHtml = UserModel.loginXhtml
	
	def signupForm = UserModel.signup;
	
	def lostpasswordForm = UserModel.lostPassword;
}