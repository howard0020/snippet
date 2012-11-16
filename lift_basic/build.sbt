name := "snippet"
 
organization := "my.company"
 
version := "0.1-SNAPSHOT"
 
scalaVersion := "2.9.1"

retrieveManaged := true 
 
seq(webSettings :_*)

resolvers ++= Seq(
	"Omniauth repo" at "https://repository-liftmodules.forge.cloudbees.com/release",
	 "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
	 "Scala Tools Releases" at "http://scala-tools.org/repo-releases/"
)

libraryDependencies ++= {
	val liftVersion = "2.4" // Put the current/latest lift version here
	Seq(
		"net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
		"net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
		"net.liftweb" %% "lift-wizard" % liftVersion % "compile->default"
	)
}

libraryDependencies ++= {
       val liftVersion = "2.4"
       Seq(
			   "net.liftmodules" %% "omniauth" % "2.4-0.5",
	           "mysql" % "mysql-connector-java" % "5.1.21",
               "net.liftweb" %% "lift-textile" % liftVersion % "compile->default",
               "net.liftweb" %% "lift-widgets" % liftVersion % "compile",
               "org.mortbay.jetty" % "jetty" % "6.1.26" % "container",
               "junit" % "junit" % "4.7" % "test",
               "ch.qos.logback" % "logback-classic" % "0.9.26",
               "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
               "com.h2database" % "h2" % "1.2.147"
       )
}