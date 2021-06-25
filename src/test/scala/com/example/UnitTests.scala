package com.example

import akka.http.scaladsl.server.Directives.provide
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.directives.{Admin, Client, Editor, Unknown, checkRequester}
import com.example.utils.{tryOptionToOption, tryOptionWithAlternative}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

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

}
