package com.example

import akka.http.scaladsl.server.Directives.provide
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.db.{Entries, UserLogs}
import com.example.directives.{Admin, Client, Editor, Unknown, checkRequester}
import com.example.jsonFormatters.JsonWriter
import com.example.utils.{tryOptionToOption, tryOptionWithAlternative}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDateTime
import scala.util.{Failure, Success}

class UnitTests extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {


  "utils" should {
    "tryOptionToOption works correctly" in {
      tryOptionToOption(Success(Some("1"))) shouldEqual Some("1")
      tryOptionToOption(Success(Some(1))) shouldEqual Some(1)
      tryOptionToOption(Success(None)) shouldEqual None
      tryOptionToOption(Failure(new Exception)) shouldEqual None
    }

    "tryOptionWithAlternative works correctly" in {
      tryOptionWithAlternative(Success(Some(1)))(2) shouldEqual Some(1)
      tryOptionWithAlternative(Success(None))(2) shouldEqual None
      tryOptionWithAlternative(Failure(new Exception))(2) shouldEqual Some(2)
    }

  }

  "JsonWriter" should {
    "return correct json" in {
      JsonWriter.format("hello") shouldEqual """{"message":"hello"}"""
      JsonWriter.format(5) shouldEqual """{
                                         |  "max-limit" : 5
                                         |}""".stripMargin
      JsonWriter.format(Entries("authEntry", "hostname", true, 10.0)) shouldEqual "{\n  \"auth_entry\" : \"authEntry\",\n  \"hostname\" : \"hostname\",\n  \"is_admin\" : true,\n  \"actual_quota\" : 10.0\n}"
      JsonWriter.formatEntrySeq(Seq()) shouldEqual "[ ]"
      JsonWriter.format(UserLogs(0, "hostname", LocalDateTime.MIN, 100)) shouldEqual """{
                                                                                       |  "id" : 0,
                                                                                       |  "hostname" : "hostname",
                                                                                       |  "added_time" : "-999999999-01-01T00:00",
                                                                                       |  "quota_reserved" : 100
                                                                                       |}""".stripMargin
    }
  }

}
