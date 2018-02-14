// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.18")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

// Build

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")

// CI

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.5")

// revision

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.1.0")

addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.8")


// style

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")


// test

addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.4")

addSbtPlugin("org.scalastyle" % "scalastyle-sbt-plugin" % "0.8.0")