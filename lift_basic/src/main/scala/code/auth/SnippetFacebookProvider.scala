package code.auth

import omniauth.lib.FacebookProvider
import omniauth.Omniauth
import dispatch._
import oauth.{Token, Consumer}
import json._
import JsHttp._
import oauth._
import oauth.OAuth._
import xml.{Text, NodeSeq}
import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST._
import net.liftweb.http._
import net.liftweb.util.Props
import net.liftweb.sitemap.{Menu, Loc, SiteMap}
import Loc._
import omniauth.AuthInfo
import code.model.User
import net.liftweb.common.Failure

class SnippetFacebookProvider(clientId:String, secret:String) extends FacebookProvider(clientId,secret){
  override def validateToken(accessToken:String): Boolean = {
    val tempRequest = :/("graph.facebook.com").secure / "me" <<? Map("access_token" -> accessToken)
    try{
      val json = Omniauth.http(tempRequest >- JsonParser.parse)

      val uid =  (json \ "id").extract[String]
      val name =  (json \ "name").extract[String]
     
      val ai = AuthInfo(providerName,uid,name,accessToken,Some(secret))
      Omniauth.setAuthInfo(ai)
      setupFacebookUser(json)
      logger.debug(ai)
      
      true
    } catch {
      case _ => false
    }
  }
  
  def setupFacebookUser(user : JValue) = {
    val email = (user \ "email").extract[String]
    User.findUser(email) match {
      case Full(oldUser) => User.logUserIn(oldUser)
      case Empty =>  User.logUserIn(setupNewUser(user)) 
      case Failure(msg,_,_) => S.error(msg)
    }
  }
  
  def setupNewUser(user : JValue) = {
    val newUser = User.create
    val email = (user \ "email").extract[String]
    newUser.email.set(email)
    newUser.username.set((user \ "name").extract[String])
    newUser.iconURL.set("https://graph.facebook.com/" + (user \ "id").extract[String] + "/picture")
    newUser.save
    newUser
  }
}