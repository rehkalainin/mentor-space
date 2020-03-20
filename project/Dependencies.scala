import sbt._

object Dependencies {

  val akkaVersion = "2.5.29"
  val akkaHttpVersion = "10.1.11"
  val scalaTestVersion = "3.1.0"
  val levelDbVersion = "0.12"
  val leveldbjniVersion = "1.8"
  val postgresqlVersion = " 42.2.10"
  val akkaPersistenceJdbcVersion = "3.5.3"
  val akkaCassandraVersion = "0.102"
  val akkaCassandraLauncherVersion = "0.102"
  val protobufJavaVersion = "3.11.4"


  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % akkaVersion
  val akkaHttp = "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion
  val levelDB = "org.iq80.leveldb" % "leveldb" % levelDbVersion
  val leveldbjni = "org.fusesource.leveldbjni" % "leveldbjni-all" % leveldbjniVersion


  // JDBC with PostgreSQL
  val postgresql = "org.postgresql" % "postgresql" % postgresqlVersion
  val akkaPersistenceJdbc = "com.github.dnvriend" %% "akka-persistence-jdbc" % akkaPersistenceJdbcVersion

  // Cassandra
  val akkaCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % akkaCassandraVersion
  val akkaCassandraLauncher = "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % akkaCassandraLauncherVersion % Test


  // Google Protocol Buffers
  val protobufJava = "com.google.protobuf" % "protobuf-java" % protobufJavaVersion

  val allDependencies = Seq(
    akkaActor,
    akkaTestKit,
    akkaPersistence,
    akkaHttp,
    scalaTest,
    levelDB,
    leveldbjni,
    postgresql,
    akkaPersistenceJdbc,
    akkaCassandra,
    akkaCassandraLauncher,
    protobufJava
  )
}