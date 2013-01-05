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

  // list of thumbnail images
  var images = 
    List[Image]()

  case class Image(data: Array[Byte], owner: String)

  startServer(interface = "0.0.0.0", port = 80){
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
    path("image" / PathElement){ username =>
      get {
        val image = images.find(username == _.owner)
        image match {
          case Some(image) =>
            println("found image for: " + image.owner)
            complete(HttpBody(`application/octet-stream`, image.data))
          case None =>
            println("no image found for: " + username)
            complete(NotFound)
        }
      } ~
      post {
        entity(as[MultipartFormData]){ formData =>
          println("recieved form")
          println(username + " put an image")
          images = Image(owner = username, data = formData.fields("image").entity.buffer) :: images.filterNot(username == _.owner)
          complete(OK)
        } ~ 
        complete {
          println("post sent, but no form with image")
          BadRequest
        }
      }
    } ~
    path("users"){
      complete(db)
    } ~
    complete(BadRequest)
  }
}