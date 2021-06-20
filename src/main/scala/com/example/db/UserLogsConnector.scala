package com.example.db

import slick.jdbc.JdbcDataSource
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile._
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class UserLogsConnector(isLogsTableCreated: Boolean, dbName: String, maxConnections: Option[Int])
               (implicit ec: ExecutionContext) {

  //# Creates table if it is does not exists
  createUserLogsTable()
  //$------------------

  private final val tableName = "user_logs"
  private final val allParams = "id, hostname, quota_reserved"
  private def convertToCaseClass: ((Long, String, Int)) => UserLogs =
    elem => UserLogs.tupled(elem)

  private val recordsRep = TableQuery[UserLogsTable]
  private val dbRef = Database.forName(name = dbName, maxConnections = None)

  private final def createUserLogsTable(): Unit = if (!isLogsTableCreated) {
    Await.result(dbRef.run(recordsRep.schema.create), 5.seconds)
  } else {}

  def selectAllUserLogs: Future[Vector[UserLogs]] = {
    val sqlRequest = sql"SELECT $allParams FROM $tableName".as[(Long, String, Int)]

    dbRef.run(sqlRequest).map(_.map(convertToCaseClass))
  }

  def insertLog(logValue: UserLogs): Future[Int] = {
    val sqlRequest = recordsRep += logValue

    dbRef.run(sqlRequest)
  }

  def selectByHostName(hostname: String): Future[Vector[UserLogs]] = {
    val sqlRequest = sql"SELECT $allParams FROM $tableName WHERE hostname = $hostname".as[(Long, String, Int)]

    dbRef.run(sqlRequest).map(_.map(convertToCaseClass))
  }

  def getById(id: Long): Future[Option[UserLogs]] = {
    val sqlRequest = sql"SELECT $allParams FROM $tableName WHERE id = $id".as[(Long, String, Int)]

    dbRef.run(sqlRequest).map(_.map(convertToCaseClass).headOption)
  }

  def getTopNRows(N: Byte): Future[Vector[UserLogs]] = {
    val sqlRequest = sql"SELECT TOP $N $allParams FROM $tableName".as[(Long, String, Int)]

    dbRef.run(sqlRequest).map(_.map(convertToCaseClass))
  }
}
