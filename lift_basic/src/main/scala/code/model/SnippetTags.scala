package code.model
import net.liftweb.mapper._

class SnippetTags extends LongKeyedMapper[SnippetTags] with IdPK {
  def getSingleton = SnippetTags
  object codeSnippet extends MappedLongForeignKey(this, CodeSnippet)
  object tag extends MappedLongForeignKey(this, Tag)
}


object SnippetTags extends SnippetTags with LongKeyedMetaMapper[SnippetTags]{
  
  
}