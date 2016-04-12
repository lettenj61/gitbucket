package gitbucket.core.util

import com.typesafe.config.ConfigFactory
import Directory.DatabaseHome
import liquibase.database.AbstractJdbcDatabase
import liquibase.database.core.{MySQLDatabase, H2Database}

object DatabaseConfig {

  private lazy val config = ConfigFactory.load("database")
  private lazy val dbUrl = config.getString("db.url")

  def url(directory: Option[String]): String =
    dbUrl.replace("${DatabaseHome}", directory.getOrElse(DatabaseHome))

  lazy val url: String = url(None)
  lazy val user: String = config.getString("db.user")
  lazy val password: String = config.getString("db.password")
  lazy val jdbcDriver: String = DatabaseType(url).jdbcDriver
  lazy val slickDriver: slick.driver.JdbcProfile = DatabaseType(url).slickDriver
  lazy val liquiDriver: AbstractJdbcDatabase = DatabaseType(url).liquiDriver

}

sealed trait DatabaseType {
  val jdbcDriver: String
  val slickDriver: slick.driver.JdbcProfile
  val liquiDriver: AbstractJdbcDatabase
}

object DatabaseType {

  def apply(url: String): DatabaseType = {
    if(url.indexOf("h2") >= 0){
      H2
    } else if(url.indexOf("mysql") >= 0){
      MySQL
    } else {
      throw new IllegalArgumentException(s"${url} is not supported.")
    }
  }

  object H2 extends DatabaseType {
    val jdbcDriver = "org.h2.Driver"
    val slickDriver = slick.driver.H2Driver
    val liquiDriver = new H2Database()
  }

  object MySQL extends DatabaseType {
    val jdbcDriver = "com.mysql.jdbc.Driver"
    val slickDriver = slick.driver.MySQLDriver
    val liquiDriver = new MySQLDatabase()
  }
}
