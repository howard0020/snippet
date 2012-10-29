package code.share
import net.liftweb.util.Props
import net.liftweb.common.Full

object SiteConsts {
  // DB consants
  val DB_DRIVER = readProp("db.driver")
  val DB_URL = readProp("db.url")
  val DB_USER = readProp("db.user")
  val DB_PASSWORD = readProp("db.password")
  
  // general site wide constants
  val LOGIN_URL = readProp("login_url")
  val HOME_URL = readProp("home_url")
  
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
  val GH_BASE_URL = readProp("gh.base_url")
  val GH_CALLBACK_URL = readProp("gh.callback_url")
  val GH_SIGNIN_URL = readProp("gh.signin_url")
  val GH_SUCCESS_URL = readProp("gh.success_url")
  val GH_FAILURE_URL = readProp("gh.failure_url")
  
  private def readProp(key: String): String = {
    Props.get(key) match { 
      case Full(value) => value
      case _ => throw new RuntimeException("SiteConsts.init: " + key + " is missing in properties file")
    }
  }
  
  def init {

  }
}