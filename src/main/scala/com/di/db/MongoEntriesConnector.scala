package com.di.db

import com.typesafe.config.Config
import org.mongodb.scala._
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.result.{DeleteResult, UpdateResult}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * Implements all mongo-based logic. All functions are Future[Option[_]],
 * so they can be safely extracted
 *
 * Supports:
 *
 * GET ~ by hostname | all | head | by hostname | by auth and hostname
 *
 * ADD ~ by
 */
class MongoEntriesConnector(url: String, dbName: String)(implicit ec: ExecutionContext) {

  private val mongoClient: MongoClient = MongoClient(url)

  private val database: MongoDatabase =
    mongoClient.getDatabase(dbName)

  private val entriesCollection: MongoCollection[Document] =
    database.getCollection("entries")

  private val userLogsCollection =
    database.getCollection("userLogs")

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
      UserLogs.idDb -> userLog.id,
      UserLogs.hostnameDb -> userLog.hostname,
      UserLogs.addedTimeDb -> userLog.addedTime.toString,
    )
    userLogsCollection.insertOne(docToInsert).toFutureOption()
  }

  def updateQuota(hostname: String, quotaToRecord: Int): Future[Option[UpdateResult]] =
    getEntryByHostname(hostname) flatMap {
      case Some(res) =>
        val actualQuota = res(Entries.actualQuotaDbFieldName).asDouble().getValue
        updateQuotaInner(actualQuota, quotaToRecord)(hostname)
      case None =>
        Future(None)
    }

  private def updateQuotaInner(actualQuota: Double, quotaToRecord: Int)
                              (hostname: String): Future[Option[UpdateResult]] =
    if (actualQuota >= quotaToRecord) {
      entriesCollection.updateOne(
        equal(Entries.hostnameDbFieldName, hostname),
        set(Entries.actualQuotaDbFieldName, actualQuota - quotaToRecord)
      ).toFutureOption()
    } else Future(None)

  def getHeadEntry: Future[Option[Document]] =
    entriesCollection.find().first().toFutureOption()

  def getAllEntries: Future[Seq[Document]] =
    entriesCollection.find().toFuture()

  def getHeadLog: Future[Option[Document]] =
    userLogsCollection.find().first().toFutureOption()

  def getEntryByAuth(toFind: String): Future[Option[Document]] =
    entriesCollection.find(equal(Entries.authEntryDbFieldName, toFind)).first().toFutureOption()

  def getEntryByHostname(toFind: String): Future[Option[Document]] =
    entriesCollection.find(equal(Entries.hostnameDbFieldName, toFind)).first().toFutureOption()

  def getEntryByAuthAndHostname(auth: String, hostname: String): Future[Option[Document]] =
    entriesCollection.find(and(
      equal(Entries.authEntryDbFieldName, auth),
      equal(Entries.hostnameDbFieldName, hostname))
    ).first().toFutureOption()

  def deleteByAuth(auth: String): Future[Option[DeleteResult]] =
    entriesCollection.deleteOne(equal(Entries.authEntryDbFieldName, auth)).toFutureOption()

  def deleteByHostname(hostnameToDelete: String): Future[Option[DeleteResult]] = {
    entriesCollection.deleteOne(equal(Entries.hostnameDbFieldName, hostnameToDelete)).toFutureOption()
  }
}

object MongoEntriesConnector {
  def initiateDb(config: Config)(implicit ec: ExecutionContext): MongoEntriesConnector = {
    val url = config.getString("main.db.url")
    val dbName = config.getString("main.db.name")
    new MongoEntriesConnector(url, dbName)
  }
}
