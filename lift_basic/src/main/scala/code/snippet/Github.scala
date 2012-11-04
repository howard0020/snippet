package code
package snippet

import java.util.Date
import code.gh.GhRepository
import code.gh.GhResource
import code.gh.parse
import code.lib.DependencyFactory
import dispatch.json.JsArray
import dispatch.json.JsObject
import dispatch.Http
import net.liftweb.common.Box.box2Option
import net.liftweb.common.Box
import net.liftweb.http.S
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.IterableConst
import code.gh.GhFile
import code.gh.Auth
import net.liftweb.common.Full
import code.editor.Editor
import code.gh.GhUser
import code.gh.GitHub.GhAuthInfo
import net.liftweb.common.Empty
import code.gh.GitHub
import net.liftweb.common.Failure

class Github {

  private val ghUser = {
    GhAuthInfo.is match {
      case Full(ghUser) => ghUser
      case Empty => S.redirectTo(GitHub.GH_SIGNIN_URL)
      case Failure(msg, _, _) =>
        S.error(msg)
        S.redirectTo(GitHub.GH_SIGNIN_URL)
    }
  }
  
  def repos = {    
    val repos = GhRepository.get_user_repos(ghUser.login, ghUser.access_token)
    ".repo_item" #> repos.map((r) =>
      ".repo_link *" #> r.name &
        ".repo_link [href]" #> (GitHub.GH_INDEX_NAME + "/" + r.name))
  }
}

