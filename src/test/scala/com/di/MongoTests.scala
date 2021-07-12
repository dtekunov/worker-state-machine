package com.di

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.di.db.{Entries, MongoEntriesConnector}
import com.di.utils.mongoDocToEntry
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

class MongoTests extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  lazy val testKit: ActorTestKit = ActorTestKit()
  implicit def typedSystem: ActorSystem[Nothing] = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  val dbInstanceToTest: MongoEntriesConnector = MongoEntriesConnector.initiateDb(typedSystem.settings.config)

//  def deleteValues(auths: Seq[String]) =
//    for (auth <- auths) {
//    dbInstanceToTest.de
//  }

  "Mongo" should {
    val entryTest1 = Entries("authEntry1", "hostname1", false, 100)
    val entryTest2 = Entries("authEntry2", "hostname2", false, 110)

    "be empty at the start" in {
      Await.result(dbInstanceToTest.getAllEntries, 1.second) shouldBe Seq()
    }

    "add and delete values correctly" in {
      Await.result(dbInstanceToTest.insertSingleEntry(entryTest1), 5.seconds)
      val entries = Await.result(dbInstanceToTest.getAllEntries, 5.seconds)
      entries.length shouldBe 1
      mongoDocToEntry(entries.head) shouldBe Entries("authEntry1", "hostname1", false, 100)

      Await.result(dbInstanceToTest.deleteByAuth(entryTest1.authEntry), 5.seconds)
      val maybeEmptyEntries = Await.result(dbInstanceToTest.getAllEntries, 5.seconds)
      maybeEmptyEntries.length shouldBe 0
    }

  }

}
