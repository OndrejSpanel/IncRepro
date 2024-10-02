name := "IncRepro"

//ThisBuild / logLevel := Level.Debug

scalaVersion := "3.5.0"

enablePlugins(ZincAnalysis)

Compile / incOptions ~= ( _.withTransitiveStep(10) )


