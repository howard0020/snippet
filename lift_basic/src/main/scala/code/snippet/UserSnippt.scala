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
  	
	def CurrentUserNameExpandable = "#profile_user_name *" #> UserName & ("#profile_user_name [class]" #> (User.loggedIn_? match{
	  case true => "expand"
	  case false => "collapse"
	}))
	
	//bind to the src attribute 
	def CurrentUserIcon = ".userIcon [src]" #> UserIcon
	
	def CurrentUserUrl = ".userUrl [href]" #> "/User/profile.html"
	
	def RemoveSlideOpenIfLoggedIn = "#slide_open [class]" #> (User.loggedIn_? match{
	  case true => "collapse"
	  case false => "expand"
	})
	
	def loginForm = User.login
	
	def CurrentUserProfile = CurrentUserNameExpandable & CurrentUserIcon & CurrentUserUrl & RemoveSlideOpenIfLoggedIn
	
	def loginHtml = User.loginXhtml
	
	def signupForm = User.signup;
	
	def lostpasswordForm = User.lostPassword;
}