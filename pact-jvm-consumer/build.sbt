resolvers ++= Seq("typesafe-releases" at "http://repo.typesafe.com/typesafe/releases",
                  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository")

libraryDependencies ++= Seq(
  "junit"        %  "junit"          % "4.11",
  "net.databinder" %% "unfiltered-netty-server" % "0.7.1",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "org.json4s"   % "json4s-native_2.10"  % "3.2.6",
  "org.json4s"   % "json4s-jackson_2.10" % "3.2.6",
  "org.specs2"   %% "specs2"         % "2.3.11" % "test",
  "org.mockito"  %  "mockito-all"    % "1.9.5" % "test",
  "dk.brics.automaton" % "automaton" % "1.11-8",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "com.googlecode.java-diff-utils" % "diffutils" % "1.3.0"
)

initialCommands := """
  import au.com.dius.pact.model._;
  import au.com.dius.pact.consumer._;
"""
