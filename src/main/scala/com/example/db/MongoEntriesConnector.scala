package com.example.db

import org.mongodb.scala._
import org.mongodb.scala.model.Filters.equal

import scala.concurrent.{ExecutionContext, Future}

class MongoEntriesConnector(dbName: String)(implicit ec: ExecutionContext) {

  private val mongoClient: MongoClient = MongoClient("mongodb://localhost")

  private val database: MongoDatabase = mongoClient.getDatabase(dbName)

  private val entriesCollection: MongoCollection[Document] = database.getCollection("entries")

  private val userLogsCollection = database.getCollection("userLogs")

  def insertSingleEntry(entry: Entries): Future[Option[Completed]] = {
    val docToInsert = Document(
      "authEntry" -> entry.authEntry,
      "hostname" -> entry.hostname,
      "is_admin" -> entry.isAdmin)

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

  def getHeadEntry: Future[Option[Document]] = entriesCollection.find().first().toFutureOption()

  def getAllEntries: Future[Seq[Document]] = entriesCollection.find().toFuture()

  def getHeadLog: Future[Option[Document]] = userLogsCollection.find().first().toFutureOption()

  def getEntryByAuth(toFind: String): Future[Option[Document]] =
    entriesCollection.find(equal("auth_entry", toFind)).first().toFutureOption()

}
