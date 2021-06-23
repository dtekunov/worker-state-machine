package com.example.db

import com.typesafe.config.Config
import org.mongodb.scala._
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class MongoEntriesConnector(dbName: String)(implicit ec: ExecutionContext) {

  private val mongoClient: MongoClient = MongoClient("mongodb://localhost")

  private val database: MongoDatabase = mongoClient.getDatabase(dbName)

  private val entriesCollection: MongoCollection[Document] = database.getCollection("entries")

  private val userLogsCollection = database.getCollection("userLogs")

  def insertSingleEntry(entry: Entries): Future[Option[Completed]] = {
    val docToInsert = Document(
      "authEntry" -> entry.authEntry,
      "hostname" -> entry.hostname,
      "is_admin" -> entry.isAdmin,
      "actual_quota" -> entry.actualQuota)

    entriesCollection.insertOne(docToInsert).toFutureOption()
  }

  def insertSingleUserLogs(userLog: UserLogs): Future[Option[Completed]] = {
    val docToInsert = Document(
      "id" -> userLog.id,
      "hostName" -> userLog.hostname,
      "addedTime" -> userLog.addedTime.toString,
      "quota_reserved" -> userLog.quota_reserved
    )
    userLogsCollection.insertOne(docToInsert).toFutureOption()
  }

  def updateQuota(auth: String, hostname: String, quotaToRecord: Int): Future[Option[UpdateResult]] = {
    Await.result(getEntryByAuthAndHostname(auth, hostname), 5.seconds) match {
      case Some(res) =>
        val actualQuota = res("actual_quota").asInt32().getValue
        if (actualQuota >= quotaToRecord) {
          entriesCollection.updateOne(
            equal("actual_quota", auth),
            set("actual_quota", actualQuota - quotaToRecord)
          ).toFutureOption()
        } else Future(None)
      case None => Future(None)
    }
  }

  def getHeadEntry: Future[Option[Document]] = entriesCollection.find().first().toFutureOption()

  def getAllEntries: Future[Seq[Document]] = entriesCollection.find().toFuture()

  def getHeadLog: Future[Option[Document]] = userLogsCollection.find().first().toFutureOption()

  def getEntryByAuth(toFind: String): Future[Option[Document]] =
    entriesCollection.find(equal("auth_entry", toFind)).first().toFutureOption()

  def getEntryByAuthAndHostname(auth: String, hostname: String): Future[Option[Document]] =
    entriesCollection.find(and(
      equal("auth_entry", auth),
      equal("hostname", hostname))
    ).first().toFutureOption()
}

object MongoEntriesConnector {
  def initiateDb(config: Config)(implicit ec: ExecutionContext): MongoEntriesConnector = {
    val dbName = config.getString("main.db.name")
    new MongoEntriesConnector(dbName)
  }
}
