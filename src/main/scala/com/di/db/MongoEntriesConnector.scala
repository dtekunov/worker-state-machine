package com.di.db

import com.typesafe.config.Config
import org.mongodb.scala._
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class MongoEntriesConnector(url: String, dbName: String)(implicit ec: ExecutionContext) {

  private val mongoClient: MongoClient = MongoClient(url)

  private val database: MongoDatabase = mongoClient.getDatabase(dbName)

  private val entriesCollection: MongoCollection[Document] = database.getCollection("entries")

  private val userLogsCollection = database.getCollection("userLogs")

  def insertSingleEntry(entry: Entries): Future[Option[Completed]] = {
    val docToInsert = Document(
      Entries.authEntryDbFieldName -> entry.authEntry,
      Entries.hostnameDbFieldName -> entry.hostname,
      Entries.isAdminDbFieldName -> entry.isAdmin,
      Entries.actualQuotaDbFieldName -> entry.actualQuota)

    entriesCollection.insertOne(docToInsert).toFutureOption()
  }

  def insertSingleUserLogs(userLog: UserLogs): Future[Option[Completed]] = {
    val docToInsert = Document(
      "id" -> userLog.id,
      "hostname" -> userLog.hostname,
      "added_time" -> userLog.addedTime.toString,
      "quota_reserved" -> userLog.quotaReserved
    )
    userLogsCollection.insertOne(docToInsert).toFutureOption()
  }

  def updateQuota(hostname: String, quotaToRecord: Int): Future[Option[UpdateResult]] =
    Await.result(getEntryByHostname(hostname), 5.seconds) match {
      case Some(res) =>
        val actualQuota = res(Entries.actualQuotaDbFieldName).asInt32().getValue
        updateQuotaInner(actualQuota, quotaToRecord)(hostname)
      case None => Future(None)
    }

  private def updateQuotaInner(actualQuota: Int, quotaToRecord: Int)(hostname: String): Future[Option[UpdateResult]] =
    if (actualQuota >= quotaToRecord) {
      entriesCollection.updateOne(
        equal(Entries.hostnameDbFieldName, hostname),
        set(Entries.actualQuotaDbFieldName, actualQuota - quotaToRecord)
      ).toFutureOption()
    } else Future(None)


  def getHeadEntry: Future[Option[Document]] = entriesCollection.find().first().toFutureOption()

  def getAllEntries: Future[Seq[Document]] = entriesCollection.find().toFuture()

  def getHeadLog: Future[Option[Document]] = userLogsCollection.find().first().toFutureOption()

  def getEntryByAuth(toFind: String): Future[Option[Document]] =
    entriesCollection.find(equal(Entries.authEntryDbFieldName, toFind)).first().toFutureOption()

  def getEntryByHostname(toFind: String): Future[Option[Document]] =
    entriesCollection.find(equal(Entries.hostnameDbFieldName, toFind)).first().toFutureOption()

  def getEntryByAuthAndHostname(auth: String, hostname: String): Future[Option[Document]] =
    entriesCollection.find(and(
      equal(Entries.authEntryDbFieldName, auth),
      equal(Entries.hostnameDbFieldName, hostname))
    ).first().toFutureOption()
}

object MongoEntriesConnector {
  def initiateDb(config: Config)(implicit ec: ExecutionContext): MongoEntriesConnector = {
    val url = config.getString("main.db.url")
    val dbName = config.getString("main.db.name")
    new MongoEntriesConnector(url, dbName)
  }
}
