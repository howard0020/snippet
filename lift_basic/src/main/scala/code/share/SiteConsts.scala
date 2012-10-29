package code.share
import net.liftweb.util.Props
import net.liftweb.common.Full
import net.liftweb.common.Empty

object SiteConsts {
  // DB consants
  val DB_DRIVER = readProp("db.driver")
  val DB_URL = readProp("db.url")
  val DB_USER = readProp("db.user")
  val DB_PASSWORD = readProp("db.password")
  
  // general site wide constants
  val INDEX_URL = readProp("index_url")
  val LOGIN_URL = readProp("login_url")
  
  // general omniauth properties
  val OMNI_BASE_URL = readProp("omniauth.baseurl")
  val OMNI_SUCCESS_URL = readProp("omniauth.successurl")
  val OMNI_FAILURE_URL = readProp("omniauth.failureurl")
  
  // facebook properties
  val OMNI_FB_SECRET = readProp("omniauth.facebooksecret")
  val OMNI_FB_KEY = readProp("omniauth.facebookkey")
  val OMNI_FB_PERMISSION = readProp("omniauth.facebookpermissions")

  // github properties
  val GH_SECRET = readProp("gh.secret")
  val GH_KEY = readProp("gh.key")
  val GH_SUCCESS_URL = INDEX_URL + "/" + readProp("gh.success_url")
  val GH_SIGNIN_NAME = readProp("gh.signin_name", Some("signin"))
  val GH_CALLBACK_NAME = readProp("gh.callback_name", Some("callback"))
  val GH_LOGINUSER_NAME = readProp("gh.loginuser_name", Some("loginuser"))
  
  private def readProp(key: String, defaultValue:Option[String]=None): String = {
    val errorMsg = "SiteConsts.init: " + key + " is missing in properties file"
    Props.get(key) match { 
      case Full(value) => value
      case Empty => defaultValue match {
        case Some(value) => value
        case None => throw new RuntimeException(errorMsg)
      }
      case _ => throw new RuntimeException(errorMsg)
    }
  }
  
  def init {

  }
}