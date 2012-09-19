package code.model
import net.liftweb.mapper._

class SnippetTags extends LongKeyedMapper[SnippetTags] with IdPK{
	def getSingleton = SnippetTags
	
}
object SnippetTags extends SnippetTags with LongKeyedMetaMapper[SnippetTags]{
	
}