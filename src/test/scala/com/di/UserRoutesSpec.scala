package com.di

//#user-routes-spec
//#test-top
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

//#set-up
class UserRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  //#test-top

  // the Akka HTTP route testkit does not yet support a typed actor system (https://github.com/akka/akka-http/issues/2036)
  // so we have to adapt for now
  lazy val testKit: ActorTestKit = ActorTestKit()
  implicit def typedSystem: ActorSystem[Nothing] = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  // Here we need to implement all the abstract members of UserRoutes.
  // We use the real UserRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe
  // created with testKit.createTestProbe()
  lazy val routes: Route = new Routes().routes

  // use the json formats to marshal and unmarshall objects in the test
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  //#set-up

  "Routes" should {
    "return ok" in {
      val testHeaders = Seq(
        RawHeader("Host", "127.0.0.2"),
        RawHeader("Authorization", "no-auth"),
        RawHeader("Client-Entity", "client")
      )

      val request = HttpRequest(uri = "/healthcheck/ping", headers = testHeaders)

      request ~> routes ~> check {
        status shouldBe StatusCodes.OK

        contentType shouldBe ContentTypes.`application/json`

        entityAs[String] shouldBe """{"message":"ok"}"""
      }
    }

    "return correct value" in {
      val testHeaders = Seq(
        RawHeader("Host", "127.0.0.2"),
        RawHeader("Authorization", "no-auth"),
        RawHeader("Client-Entity", "client")
      )

      val request = HttpRequest(uri = "/healthcheck/max-limit", headers = testHeaders)

      request ~> routes ~> check {
        status shouldBe StatusCodes.OK

        contentType shouldBe ContentTypes.`application/json`

        entityAs[String] shouldBe s"""{"message":${system.settings.config.getInt("main")}"""
      }
    }

    "return 404 for wrong url" in {
      val testHeaders = Seq(
        RawHeader("Host", "127.0.0.2"),
        RawHeader("Authorization", "no-auth"),
        RawHeader("Client-Entity", "client")
      )

      val request = HttpRequest(uri = "/healthcheck/aaa", headers = testHeaders)

      request ~> routes ~> check {
        status shouldBe StatusCodes.NotFound

        contentType shouldBe ContentTypes.`application/json`

        entityAs[String] shouldBe s"""{
                                     |  "message": "Url is malformed"
                                     |}"""
      }
    }
  }
}