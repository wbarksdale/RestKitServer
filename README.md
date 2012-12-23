#RestKitServer

A simple REST server in scala. supports GET, POST, PUT, and Delete of Users. A User is represented by:

``` scala
case class User(username: String, name: String, age: Int)
```

to start the server:
``` bash
$ cd RestKitServer/
$ sbt
> re-start
```