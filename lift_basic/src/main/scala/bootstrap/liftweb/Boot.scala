package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._
import common._
import http._
import sitemap._
import Loc._
import mapper._
import code.model._
import post._
import omniauth.lib._
import omniauth.Omniauth
import http.rest._
import code.gh.Auth
import code.auth.VThreeGithubProvider
import code.auth.SnippetFacebookProvider

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor =
        new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
          Props.get("db.url") openOr
            "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
          Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _, User, KeyValuePair, CodeSnippet, Tag, SnippetTags, Block, ToCModel)

    // where to search snippet
    LiftRules.addToPackages("code")
    val entries = List(
      Menu.i("Home") / "index" >> LocGroup("General"), // the simple way to declare a menu

      // more complex because this menu allows anything in the
      // /static path to be visible

      //Menu(Loc("AllSnippet", Link(List("AllSnippet"), true, "/AllSnippet/index"), "All Snippet", LocGroup("General"))),

      Menu.i("New post") / "compose/index"
        >> If(
          () => User.loggedIn_?,
          () => RedirectResponse(Auth.LOGIN_URL))
          >> LocGroup("General"),
      Menu(Loc("profile", "User" / "profile", "profile", Hidden, If(User.loggedIn_? _, () => RedirectResponse("/login")))),
      //,Menu(Loc("Static", Link(List("static"), true, "/static/index"), "Post Form"))

      //================== GITHUB RELATED LINKS ===========================//
      Menu.i("Github") / "github"
        >> If(
          () => User.loggedIn_? && Auth.is_logged_in,
          () => User.loggedIn_? match {
            case false => RedirectResponse(Auth.LOGIN_URL)
            case true => RedirectResponse(Auth.sign_url(Full("/github")))
          }),
      Menu(Loc("Github Repo", List("github_repo"), "Github Repo", Hidden,
        If(
          () => User.loggedIn_? && Auth.is_logged_in,
          () => User.loggedIn_? match {
            case false => RedirectResponse(Auth.LOGIN_URL)
            case true => RedirectResponse(
              Auth.sign_url(Full("/github/" + S.param("repoName").get +
                (S.param("path").get match {
                  case "" => ""
                  case path => "/" + path
                }))))
          }))),
      Menu(Loc("GhSignin", List("ghauth", "signin"), "GhSignin", Hidden)),
      Menu(Loc("GhCallback", List("ghauth", "callback"), "GhCallback", Hidden)),
      Menu(Loc("GhLoginUser", List("ghauth", "loginuser"), "GhLoginUser", Hidden)), //================== end GITHUB RELATED LINKS ===========================//
      Menu(Loc("Search", List("search"), "Search")),
      Menu(Loc("Edit Table of Content", List("tblofcontent"), "Edit Table of Content")),
      Menu(Loc("Page Not Found Error", List("pagenotfounderror"), "Page Not Found Error"))) :::
      Omniauth.sitemap

    //=============================== GITHUB RELATED CONFIG =====================//
    LiftRules.rewrite.append {
      case RewriteRequest(ParsePath(List("github", repoName, repo_path @ _*), suffix, _, _), _, _) =>
        val path = (repo_path mkString "/") + (suffix match {
          case "js" => ".js"
          case "css" => ".css"
          case _ => ""
        })
        RewriteResponse("github_repo" :: Nil, Map("repoName" -> repoName, "path" -> path))
      case RewriteRequest(ParsePath(List("search", queryString), suffix, _, _), _, _) =>
        val q = if (suffix.isEmpty) "" else suffix
        RewriteResponse("search" :: Nil, Map("queryString" -> queryString))
    }
    //=============================== end GITHUB RELATED CONFIG =========================//

    //=============================== Omniauth ==========================================//
    val SnippetProvider = new SnippetFacebookProvider(Props.get(FacebookProvider.providerPropertyKey).openOr(""), Props.get(FacebookProvider.providerPropertySecret).openOr(""))

    //init Omniauth with defaul providers
    Omniauth.initWithProviders(List(SnippetProvider))

    //Adding CRUDify CodeSnippet into menu
    val crudifyCodeSnippet: List[Menu] = CodeSnippet.menus
    val userMenu: List[Menu] = User.menus
    val tagMenu: List[Menu] = Tag.menus
    val MenuCombine = entries ++ crudifyCodeSnippet ++ userMenu ++ tagMenu
    // Build SiteMap
    def sitemap = SiteMap(MenuCombine: _*)

    //def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    //LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))
    LiftRules.setSiteMapFunc(() => sitemap)

    //============ ERROR PAGES =======================//
    // Catch 404s   
    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) => 
        NotFoundAsTemplate(ParsePath(List("404"),"html",false,false))
    })

    //============= STANDARD LIFT PROJECT CODE =================== //
    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)

    LiftRules.rewrite.append {
      case RewriteRequest(ParsePath("tag" :: tag :: Nil, _, _, _), _, _) =>
        RewriteResponse("index" :: Nil, Map("tag" -> tag))
    }
  }
}



