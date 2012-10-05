package code.model
import net.liftweb.mapper._

class SnippetTags extends LongKeyedMapper[SnippetTags] with IdPK {
  def getSingleton = SnippetTags
  object codeSnippet extends MappedLongForeignKey(this, CodeSnippet)
  object tag extends MappedLongForeignKey(this, Tag)
  
  
}


object SnippetTags extends SnippetTags 
	with LongKeyedMetaMapper[SnippetTags]{
  
   def getTopTag(count:Int):List[Tag] = Tag.findAllByInsecureSql(
       "SELECT * FROM Tag t1 JOIN ( select tag from snippettags group by tag order by count(tag) desc LIMIT %d) as t2 on t1.id = t2.tag".format(count),
       IHaveValidatedThisSQL("Howard","10/4/2012"))
}