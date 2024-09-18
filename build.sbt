name := "IncRepro"

//ThisBuild / logLevel := Level.Debug

scalaVersion := "3.5.0"

Compile / incOptions ~= ( _.withTransitiveStep(10) )


