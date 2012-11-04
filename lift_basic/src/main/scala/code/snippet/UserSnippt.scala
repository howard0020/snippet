package code.snippet


import net.liftweb.util.BindPlus._
import net.liftweb.util._
import code.model.User
import Helpers._
import net.liftweb.common.{Full, Empty, Box}
import code.model.CodeSnippet
import net.liftweb.common.Failure

class UserSnippet {
	
	def UserName = User.currentUser match {
	  case Full(currUser) => currUser.username.get
	  case Empty =>  ""
	  case Failure(msg,_,_) => "msg"
	  case _=>	""
	}
	
  	def UserIcon = User.currentUser match {
	  case Full(currUser) => currUser.iconURL.get
	  case Empty =>  ""
	  case Failure(msg,_,_) => "msg"
	  case _=>	""
	}
  	
	def CurrentUserNameExpandable =  "#profile_user_name [class]" #> (User.loggedIn_? match{
	  case true => "expand"
	  case false => "collapse"
	})
	def CurrentUserName = ".userName *" #> UserName
	//bind to the src attribute 
	def CurrentUserIcon = ".profile_user_icon [src]" #> UserIcon
	
	def CurrentUserUrl = ".userUrl [href]" #> "/User/profile.html"
	
	def RemoveSlideOpenIfLoggedIn = "#slide_open [class]" #> (User.loggedIn_? match{
	  case true => "collapse"
	  case false => "expand"
	})
	def LogoutMenuItem = ".logout_url [href]"  #> "/user_mgt/logout" & ("#logout_menu_item [class]"#> (User.loggedIn_? match{
	  case true => "expand"
	  case false => "collapse"
	}))
	
	def loginForm = User.login
	
	def CurrentUserProfile = CurrentUserName & CurrentUserNameExpandable & CurrentUserIcon & CurrentUserUrl & RemoveSlideOpenIfLoggedIn & LogoutMenuItem
	
	def loginHtml = User.loginXhtml
	
	def signupForm = User.signup;
	
	def lostpasswordForm = User.lostPassword;
}