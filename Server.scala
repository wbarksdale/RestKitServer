package com.tuple23.restkitserver

import spray.routing.SimpleRoutingApp
import spray.routing._

import spray.http._
import StatusCodes._
import MediaTypes._

object Main extends App with SimpleRoutingApp {

  case class User(username: String, name: String, age: Int)

  // implicits for json marshalling
  import spray.json.DefaultJsonProtocol
  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val UserFormat = jsonFormat3(User)
  }
  import MyJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  // the "database", highly thread dangerous
  var db = 
    List(
      User("bobuser", "bob", 20),
      User("greguser",  "greg", 30),
      User("maryuser", "mary", 40))

  startServer(interface = "localhost", port = 8080){
    pathPrefix("user"){
      get {
        path(PathElement){ username =>
          val maybeUser = db.find(_.username == username)
          maybeUser match {
            case Some(user) => complete(user)
            case _ => complete(BadRequest, "user not found")
          }
        }
      } ~
      delete {
        path(PathElement){ username =>
          db = db.filterNot(_.username == username)
          respondWithMediaType(`application/json`){ complete("{}") }
        }
      } ~
      post {
        entity(as[User]){ user =>
          println("updating user: " + user)
          db = db.filterNot(user.username == _.username)
          db = user :: db
          println(db)
          respondWithMediaType(`application/json`){ complete("{}") }
        }
      } ~
      put {
        entity(as[User]){ user =>
          println("putting user: " + user)
          if(db.find(user.username == _.username).isDefined)
            complete(BadRequest, "user already exists")
          else {
            db = user :: db
            complete(OK)
          }
        }
      }
    } ~
    path("users"){
      complete(db)
    } ~
    complete("unknown route")
  }
}