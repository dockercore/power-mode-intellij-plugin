import org.jetbrains.sbtidea.Keys._

lazy val powerMode =
  project
    .in(file("."))
    .enablePlugins(SbtIdeaPlugin)
    .settings(
      version := "102.201",
      scalaVersion := "2.13.2",
      ThisBuild / intellijPluginName := "Power Mode II",
      ThisBuild / intellijBuild := "213.7172.25",
      ThisBuild / intellijPlatform := IntelliJPlatform.IdeaUltimate,
      Global / intellijAttachSources := true,
      Compile / javacOptions ++= "--release" :: "11" :: Nil,
      intellijPlugins += "com.intellij.properties".toPlugin,
      libraryDependencies ++= Seq(
        "javazoom" % "jlayer" % "1.0.1",
        "org.typelevel" %% "squants" % "1.6.0",
        "org.apache.commons" % "commons-math3" % "3.3",
        "com.eclipsesource.minimal-json" % "minimal-json" % "0.9.5" withSources ()
      ),
      scalacOptions in ThisBuild += "-deprecation",
      unmanagedJars in Compile += baseDirectory.value / "lib",
      unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
      resourceDirectories in Runtime += baseDirectory.value / "resources",
      unmanagedResourceDirectories in Test += baseDirectory.value / "testResources",
      packageMethod := PackagingMethod.Standalone(),
      bundleScalaLibrary in ThisBuild := true
    )
