package demo

import io.circe.generic.auto.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.interceptor.{Interceptor, RequestInterceptor}
import sttp.tapir.server.netty.loom.{Id, NettyIdServer, NettyIdServerOptions}
import sttp.tapir.swagger.SwaggerUI
import sttp.tapir.swagger.bundle.SwaggerInterpreter

// data structures

case class Book(title: String, year: Int)

var books = List(
  Book("The Sorrows of Young Werther", 1774),
  Book("Iliad", -8000),
  Book("Nad Niemnem", 1888),
  Book("The Colour of Magic", 1983),
  Book("The Art of Computer Programming", 1968),
  Book("Pharaoh", 1897),
  Book("Lords and Ladies", 1992)
)

// endpoints

val baseEndpoint = endpoint.in("books").errorOut(stringBody)
val limitParameter = query[Option[Int]]("limit").description("Limit parameter")

// POST /books/add
val addBook: PublicEndpoint[(Book, String), String, Unit, Any] = baseEndpoint.post
  .in("add")
  .in(jsonBody[Book].example(Book("Pride and prejudice", 1813)))
  .in(header[String]("X-Auth-Token").description("The token is 'secret'"))

// GET /books?year=...&limit=...
val listBooks: PublicEndpoint[(Option[Int], Option[Int]), String, List[Book], Any] =
  baseEndpoint.get
    .in(query[Option[Int]]("year"))
    .in(limitParameter)
    .out(jsonBody[List[Book]])

// server logic

val addBookServerEndpoint = addBook.serverLogic[Id] { case (book, token) =>
  if token != "secure" then Left("Unauthorized access!!!11")
  else
    books = book :: books
    Right(())
}

val listBooksServerEndpoint = listBooks.serverLogic[Id] { case (year, limit) =>
  val filteredBooks = year match {
    case Some(y) => books.filter(_.year == y)
    case None    => books
  }
  val limitedBooks = limit match {
    case Some(l) => filteredBooks.take(l)
    case None    => filteredBooks
  }
  Right(limitedBooks)
}

def startServer(): Unit =
  val mainEndpoints = List(addBookServerEndpoint, listBooksServerEndpoint)
  val docEndpoints = SwaggerInterpreter().fromServerEndpoints(mainEndpoints, "Books API", "1.0")

  NettyIdServer().addEndpoints(mainEndpoints ++ docEndpoints).start()

def startServerWithInterceptor(): Unit =
  val mainEndpoints = List(addBookServerEndpoint, listBooksServerEndpoint)
  val docEndpoints = SwaggerInterpreter().fromServerEndpoints(mainEndpoints, "Books API", "1.0")

  val customOptions = NettyIdServerOptions.customiseInterceptors
    .prependInterceptor(RequestInterceptor.transformServerRequest { request =>
      println("Handling request: " + request)
      request
    })
    .options

  NettyIdServer(customOptions).addEndpoints(mainEndpoints ++ docEndpoints).start()

@main def start(): Unit =
  startServerWithInterceptor()
  println("Try out the API by opening the Swagger UI: http://localhost:8080/docs")
