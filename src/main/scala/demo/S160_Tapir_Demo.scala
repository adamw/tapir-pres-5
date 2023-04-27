package demo

object S160_Tapir_Demo extends App {
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

  object Endpoints {
    import io.circe.generic.auto._
    import sttp.tapir._
    import sttp.tapir.generic.auto._
    import sttp.tapir.json.circe._

    val baseEndpoint = endpoint.in("books").errorOut(stringBody)
    val limitParameter = query[Option[Int]]("limit").description("Limit parameter")

    // POST /books/add
    val addBook: Endpoint[(Book, String), String, Unit, Any] = baseEndpoint.post
      .in("add")
      .in(jsonBody[Book].example(Book("Pride and prejudice", 1813)))
      .in(header[String]("X-Auth-Token").description("The token is 'secret'"))

    // GET /books?year=...&limit=...
    val listBooks: Endpoint[(Option[Int], Option[Int]), String, List[Book], Any] =
      baseEndpoint.get
        .in(query[Option[Int]]("year"))
        .in(limitParameter)
        .out(jsonBody[List[Book]])
  }

  //

  import Endpoints._
  import akka.http.scaladsl.server.Route

  def booksRoutes: Route = {
    import sttp.tapir.server.akkahttp._

    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Future

    val addBookServerEndpoint = addBook.serverLogic { case (book, token) =>
      Future {
        if (token != "secure") {
          Left("Unauthorized access!!!11")
        } else {
          books = book :: books
          Right(())
        }
      }
    }

    val listBooksServerEndpoint = listBooks.serverLogic[Future] { case (year, limit) =>
      Future {
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
    }

    AkkaHttpServerInterpreter.toRoute(List(addBookServerEndpoint, listBooksServerEndpoint))
  }

  def openapiYamlDocumentation: String = {
    import sttp.tapir.docs.openapi._
    import sttp.tapir.openapi.circe.yaml._

    val docs: OpenAPI = OpenAPIDocsInterpreter.toOpenAPI(List(addBook, listBooks), "Books I've read", "1.0")
    docs.toYaml
  }

  def startServer(): Unit = {
    import akka.actor.ActorSystem
    import akka.http.scaladsl.Http
    import akka.http.scaladsl.server.Directives._
    import sttp.tapir.swagger.akkahttp.SwaggerAkka

    import scala.concurrent.Await
    import scala.concurrent.duration._

    val routes = booksRoutes ~ new SwaggerAkka(openapiYamlDocumentation).routes
    implicit val actorSystem: ActorSystem = ActorSystem()
    Await.result(Http().newServerAt("localhost", 8080).bind(routes), 1.minute)
  }

  def makeClientRequest(): Unit = {
    import sttp.client3._
    import sttp.tapir.client.sttp._

    val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

    val booksListingRequest: Request[Either[String, List[Book]], Any] = SttpClientInterpreter
      .toRequestThrowDecodeFailures(listBooks, Some(uri"https://souoxj2dr5.execute-api.eu-central-1.amazonaws.com"))
      .apply((None, Some(2)))

    val result: Either[String, List[Book]] = booksListingRequest.send(backend).body
    println("Client call result: " + result)
  }

//  startServer()
  makeClientRequest()
//  println("Try out the API by opening the Swagger UI: http://localhost:8080/docs")
}
