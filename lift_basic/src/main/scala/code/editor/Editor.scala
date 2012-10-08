package code.editor

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
      "cs" -> "csharp"
      )
   private val pat = """(.*)[.]([^.]*)""".r
   private def get_file_ext(fname: String):Option[String] = {
    fname match {
      case pat(f, ext) => Some(ext)
      case _ => None
    }
  }
     
   def display(content: String, filename: String) = {
     get_file_ext(filename) match {
       case Some(ext) => 
         val lang = ext2lang(ext)
         val params_map = SYNTAX_HIGHLIGHTER_PARAMS + ("brush" -> lang)
         val ls = for((k, v) <- params_map)
        	 		yield k + ": " + v
         <div class="editor">
        	 	<button>Post Selected Text </button>
        	 	<pre class={ls mkString ";" }>
         			{ content }
        	 	</pre>
         </div>
       case None => 
         <div class="editor"><pre>{ content }</pre></div>
     }
  }
}