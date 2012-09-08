package code 
package snippet 

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import net.liftweb.util.BindPlus._

class HelloWorld {
  lazy val date: Box[Date] = DependencyFactory.inject[Date] // inject the date

  // replace the contents of the element with id "time" with the date
  def howdy = "#time *" #> "asdasd"

  /*
   lazy val date: Date = DependencyFactory.time.vend // create the date via factory

   def howdy = "#time *" #> date.toString
   */
  def A (xhtml : NodeSeq) : NodeSeq = 
    bind("A", xhtml, "name" -> "Text from snippet!")
    
 // def B (xhtml : NodeSeq) : NodeSeq = 
  //  bind("B",  xhtml, "text" -> Text("B text replaced!"))
}
class B {
	def snippet (xhtml : NodeSeq)  ={
		xhtml.bind("AAAA","text" -> "hahaha")
 	}
 }
