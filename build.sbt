name := "Voltric"

version := "0.1"

scalaVersion := "2.11.6"

resolvers ++= Seq(
  // other resolvers here
  // if you want to use snapshot builds (currently 0.12-SNAPSHOT), use this.
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies ++= Seq (
  "colt" % "colt" % "1.2.0",
  "org.apache.commons" % "commons-math3" % "3.6.1"
)
    