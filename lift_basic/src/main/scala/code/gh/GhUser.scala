package code.gh

import dispatch._

import json._
import JsHttp._

case class GhContributor(id: Int, login: String, avatar_url: String)

case class GhUser(id: Int, login: String, avatar_url: String, account_type: String, access_token: String)

object GhUser {
	
	def get_authenticated_user(access_token: String) = {
		val svc = GitHub.api_host / "user"
		val handler = svc.secure <<? Map("access_token" -> access_token) ># { json =>
			val jsonObj = parse.jsonObj(json)
			
			val id = jsonObj("id").asInt
			val name = jsonObj("login").asString
			val avatar_url = jsonObj("avatar_url").asString
			val account_type = jsonObj("type").asString
			
			
			GhUser(id, name, avatar_url, account_type, access_token)
		}
		Http(handler)
	}
	
	def get_emails(access_token: String) = {
	  val svc = GitHub.api_host
	  println("\n\nGET EMAILS================================" + access_token + "\n\n")
	  svc.secure.POST / "user" / "emails" <<? Map("access_token" -> access_token) ># { json =>
	    json
	  }
	}
}