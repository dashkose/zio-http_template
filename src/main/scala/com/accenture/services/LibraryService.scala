package com.accenture.services

import com.accenture.domain.library.*
import com.accenture.services.LibraryService.errors.*
import zio.*

trait LibraryService {
  def getBooks: ZIO[Any, UnderlyingError | BusinessError, List[Book]]
}

object LibraryService {
  class DummyLibraryService extends LibraryService {
    def getBooks: ZIO[Any, UnderlyingError | BusinessError, List[Book]] = ZIO.succeed(
      List(
        Book("The Sorrows of Young Werther", 1774, Author("Johann Wolfgang von Goethe", 1), 1),
        Book("On the Niemen", 1888, Author("Eliza Orzeszkowa", 2), 2),
        Book("The Art of Computer Programming", 1968, Author("Donald Knuth", 3), 3),
        Book("Pharaoh", 1897, Author("Boleslaw Prus", 4), 4)
      )
    )
  }

  val dummyLayer: ULayer[LibraryService] = ZLayer.succeed(new DummyLibraryService)

  object errors {
    case class BusinessError(message: String)
    case class UnderlyingError(cause: Throwable)
  }
}
