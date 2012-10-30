package code.editor
import code.gh.GhFile

object Editor {
  private val SYNTAX_HIGHLIGHTER_PARAMS = Map(
    "toolbar" -> "false",
    "auto-links" -> "false")

  val ext2lang = Map(
    "rb" -> "ruby",
    "scala" -> "scala",
    "java" -> "java",
    "html" -> "xml",
    "js" -> "js",
    "cs" -> "csharp")

  def isImg(ext: String) = {
    List("png", "jpg", "gif").contains(ext.toLowerCase())
  }

  private val pat = """(.*)[.]([^.]*)""".r
  private def get_file_ext(fname: String): Option[String] = {
    fname match {
      case pat(f, ext) => Some(ext)
      case _ => None
    }
  }

  def display(ghFile: GhFile, content: String, filename: String) = {
    get_file_ext(filename) match {
      case Some(ext) =>
        isImg(ext) match {
          case true =>
            <div>
              <img src={ ghFile.htmlUrl + "?raw=true" } alt={ filename }></img>
            </div>
          case false =>
            var params_map = SYNTAX_HIGHLIGHTER_PARAMS
            if (ext2lang.contains(ext))
              params_map += ("brush" -> ext2lang(ext))
            val ls = for ((k, v) <- params_map)
              yield k + ": " + v
            <div class="editor">
              <pre class={ ls mkString ";" }>{ content }</pre>
            </div>
        }
      case None =>
        <div class="editor"><pre>{ content }</pre></div>
    }
  }
}