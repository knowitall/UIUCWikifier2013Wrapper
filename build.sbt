name := "UIUCWikifier2013Wrapper"

version := "1.0"

javaOptions += "-Xmx12G"

fork in run := true

scalaVersion := "2.10.1"

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
