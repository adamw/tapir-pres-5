package demo

import java.util.UUID
import javax.ws.rs.core.MediaType
import javax.ws.rs.{GET, Path, PathParam, Produces}
import javax.xml.bind.annotation.XmlRootElement

object S130_Current_state_java:
  @XmlRootElement
  class Book(title: String)

  @GET
  @Path("/books/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getBookById(@PathParam("id") id: UUID): Book = new Book("The Trial")
