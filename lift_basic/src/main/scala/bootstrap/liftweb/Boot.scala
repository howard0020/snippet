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
import code.auth.SnippetFacebookProvider
import code.gh.GitHub
import code.share.SiteConsts
import code.auth.GhProvider

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {

  def boot {
    // create constants in the properties file
    // should be called first
    // ================================================================================
    // = TO DO: need to make sure necessary constants are read successfully
    // ================================================================================
    SiteConsts.init

    // setup database configuration
    initDB

    // where to search snippet
    LiftRules.addToPackages("code")

    // setup sitemap
    initSiteMap

    GitHub.init

    // Omniauth
    val SnippetProvider = new SnippetFacebookProvider(Props.get(FacebookProvider.providerPropertyKey).openOr(""), Props.get(FacebookProvider.providerPropertySecret).openOr(""))
    val ghProvider = new GhProvider(SiteConsts.GH_KEY, SiteConsts.GH_SECRET)
    Omniauth.initWithProviders(List(SnippetProvider, ghProvider))

    //=============================== REWRITES =====================//
    // * search rewrites
    // * tag rewrites
    LiftRules.rewrite.append {
      case RewriteRequest(ParsePath(List("search", queryString), suffix, _, _), _, _) =>
        val q = if (suffix.isEmpty) "" else suffix
        RewriteResponse("search" :: Nil, Map("queryString" -> queryString))
      case RewriteRequest(ParsePath("tag" :: tag :: Nil, _, _, _), _, _) =>
        RewriteResponse("index" :: Nil, Map("tag" -> tag))
      case RewriteRequest(ParsePath("profile" :: idString :: Nil, _, _, _), _, _) =>
        try {
          val id = idString.toLong
          User.findByKey(id) match {
            case Full(u) => RewriteResponse("profile" :: Nil, Map("id" -> id.toString))
            case _ => RewriteResponse("404" :: Nil)
          }
        } catch {
          case _ => RewriteResponse("404" :: Nil)
        }
      case RewriteRequest(ParsePath("edit-post" :: idString :: Nil, _, _, _), _, _) =>
        try {
          Console.println("===>id"+idString)
          val id = idString.toLong
          CodeSnippet.findByKey(id) match {
            case Full(post) => 
              Console.println("===find>"+post)
              RewriteResponse("compose" :: "index" :: Nil, Map("id" -> id.toString))
            case _ => RewriteResponse("404" :: Nil)
          }
        } catch {
          case _ => RewriteResponse("404" :: Nil)
        }
    }

    // Catch 404s   
    LiftRules.uriNotFound.prepend(NamedPF("404handler") {
      case (req, failure) =>
        NotFoundAsTemplate(ParsePath(List("404"), "html", false, false))
    })

    // Catch exceptions and return pages with friendly error messages
    LiftRules.exceptionHandler.prepend {
      case (runMode, request, exception: RuntimeException) =>
        InternalServerErrorResponse()
    }

    initStdConfig
  }

  def initSiteMap {
    var menuItems = List(
      Menu.i("Home") / "index" >> LocGroup("General"), // the simple way to declare a menu
      Menu.i("Edit post") / "compose" / "edit"
        >> If(
          () => User.loggedIn_?,
          () => RedirectResponse(SiteConsts.LOGIN_URL))
          >> Hidden,
      Menu.i("New post") / "compose" / "new"
        >> If(
          () => User.loggedIn_?,
          () => RedirectResponse(SiteConsts.LOGIN_URL))
          >> LocGroup("General"),
      Menu(Loc("profile", "User" / "profile", "profile", Hidden, If(User.loggedIn_? _, () => RedirectResponse("/login")))),
      Menu(Loc("Profile Page", List("profile"), "Profile Page",
        If(
          () =>
            {
              S.param("id") match {
                case Full(v) => true
                case Empty => User.loggedIn_?
                case Failure(_, _, _) => false
              }
            },
          () => {
            S.param("id") match {
              case Full(v) => InternalServerErrorResponse()
              case Empty => User.loggedIn_? match {
                case true => InternalServerErrorResponse()
                case false => RedirectResponse(SiteConsts.LOGIN_URL)
              }
              case Failure(_, _, _) => InternalServerErrorResponse()
            }
          }))),
      Menu(Loc("Search", List("search"), "Search")),
      Menu(Loc("Edit Table of Content", List("tblofcontent"), "Edit Table of Content")),
      Menu(Loc("Page Not Found", List("404"), "Page Not Found", Hidden)),
      Menu(Loc("Data Population Script", List("db"), "Database Script", Hidden))) :::
      Omniauth.sitemap

    val ghMenus = GitHub.sitemap
    val crudifyCodeSnippet = CodeSnippet.menus
    val userMenu = User.menus
    val tagMenu = Tag.menus

    menuItems = menuItems ++ ghMenus ++ crudifyCodeSnippet ++ userMenu ++ tagMenu

    def sitemap = SiteMap(menuItems: _*)

    //def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    //LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))
    LiftRules.setSiteMapFunc(() => sitemap)
  }

  def initDB {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = new StandardDBVendor(
        SiteConsts.DB_DRIVER, SiteConsts.DB_URL,
        Full(SiteConsts.DB_USER), Full(SiteConsts.DB_PASSWORD))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _, User, KeyValuePair, CodeSnippet, Tag, SnippetTags, Block, ToCModel)
  }

  def initStdConfig {
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
    //def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    //LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)
  }
}



