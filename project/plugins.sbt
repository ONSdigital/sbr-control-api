// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.20")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

// Build
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.8")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

// CI
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.10")

// revision
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.7")

addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.15")

// style
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")

// test
addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.9")

addSbtPlugin("org.scalastyle" % "scalastyle-sbt-plugin" % "1.0.0")

// kamon (for tracing)
resolvers += Resolver.bintrayIvyRepo("kamon-io", "sbt-plugins")
addSbtPlugin("io.kamon" % "sbt-aspectj-runner-play-2.6" % "1.1.0")
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.4")