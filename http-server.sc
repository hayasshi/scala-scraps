//> using scala 2.13
//> using jvm 17
//> using dep com.typesafe.akka::akka-http:10.2.10
//> using dep com.typesafe.akka::akka-stream::2.6.21
//> using dep de.heikoseeberger::akka-http-circe::1.39.2
//> using dep io.circe::circe-core::0.14.6
//> using dep io.circe::circe-generic::0.14.6
import akka.http.scaladsl.Http
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

implicit val system: ActorSystem = ActorSystem()

case class Message(value: String)

val route = path("hello") {
  post {
    entity(as[Message]) { message =>
      println(message)
      complete(s"Hello. Your message: ${message.value}")
    }
  }
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
Http()
  .newServerAt("0.0.0.0", 8080)
  .bind(route)
  .map(binding => {println("Start server");binding})
  .map(_.addToCoordinatedShutdown(10.seconds))
  .foreach(_.whenTerminated.foreach(_ => println("Server terminated")))
