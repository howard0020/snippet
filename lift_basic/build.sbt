name := "snippet"
 
organization := "my.company"
 
version := "0.1-SNAPSHOT"
 
scalaVersion := "2.9.1"

retrieveManaged := true 
 
EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

libraryDependencies ++= {
       val liftVersion = "2.4"
       Seq(
	           "mysql" % "mysql-connector-java" % "5.1.21",
			   "net.liftweb" %% "lift-wizard" % liftVersion % "compile->default",
               "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
               "net.liftweb" %% "lift-mapper" % liftVersion % "compile",
               "org.mortbay.jetty" % "jetty" % "6.1.26" % "test",
               "junit" % "junit" % "4.7" % "test",
               "ch.qos.logback" % "logback-classic" % "0.9.26",
               "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
               "com.h2database" % "h2" % "1.2.147"
       )
}