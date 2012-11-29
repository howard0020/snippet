package code 
package snippet 

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import net.liftweb.util.BindPlus._
import net.liftweb.http.js._
import net.liftweb.http.js.JE._
import code.model.User
import code.model.Post
import omniauth.Omniauth
import dispatch._
import xml.{Text, NodeSeq}
import oauth._
import oauth.OAuth._
import xml.{Text, NodeSeq}
import omniauth.Omniauth
import dispatch._
import oauth.{Token, Consumer}
import oauth._
import oauth.OAuth._
import xml.{Text, NodeSeq}
import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.http._
import net.liftweb.util.Props
import net.liftweb.sitemap.{Menu, Loc, SiteMap}
import Loc._
import omniauth.AuthInfo
import net.liftweb.json._

class HelloWorld {
  lazy val date: Box[Date] = DependencyFactory.inject[Date] // inject the date
  implicit val formats = net.liftweb.json.DefaultFormats
  // replace the contents of the element with id "time" with the date
  
  
  def howdy = "#time *" #> "asdasd"

  
  /*
   lazy val date: Date = DependencyFactory.time.vend // create the date via factory

   def howdy = "#time *" #> date.toString
   */
  def A (xhtml : NodeSeq) : NodeSeq = 
    bind("A", xhtml, "name" -> "Text from snippet!")
    
 // def B (xhtml : NodeSeq) : NodeSeq = 
  //  bind("B",  xhtml, "text" -> Text("B text replaced!"))
    
  def ProfileImage : String = {
     Omniauth.currentAuth match {
       case Full(user) => 
     }
     val accessToken = Omniauth.currentAuth.get.token
      val tempRequest = :/("graph.facebook.com").secure / "me" <<? Map("access_token" -> accessToken)
      try{
          	val json = Omniauth.http(tempRequest >- JsonParser.parse)
          	val text = "https://graph.facebook.com/" + (json \ "id").extractOpt[String] + "/picture"
        	
        	  ""
      }catch{
        case _ =>""
      }
  }  
}
class B {
	def snippet (xhtml : NodeSeq)  ={
	//	xhtml.bind("AAAA","text" -> "hahaha")
		bind("AAAA",xhtml,"text" -> "ahhahaha")
 	}
	def JS = "#JSMain *" #> JsRaw("$(document).ready(function() {" +
			"$( \"#datepicker\" ).datepicker();" +
			" });")

 }
class C {
	def snippet ="#text *" #> "hhe4heheh" & "#dddd" #>  "hehehe2"
	
	def snippet2 = "#dddd" #> "hehehe2"
 }

//class Post
//{
//	def Content(xhtml : NodeSeq)  = {
//		val user = User.currentUser
//		val posts = user match {
//		  case Full(user) => user.AllPost
//		  case Empty =>  Post.findAll()
//		  case Failure(msg,_,_) => List()
//		  
//		}
//		System.out.println("======>"+posts)
//		def bindText(template : NodeSeq) : NodeSeq =
//		{
//		  posts.flatMap{ case (code) => bind("content",template,"text" 
//		      -> scala.xml.Unparsed(code.content.get))}
//		  
//		}
//		bind("content",xhtml,"code"->bindText _)
//	}
//}
