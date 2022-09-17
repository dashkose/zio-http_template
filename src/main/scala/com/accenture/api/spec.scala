package com.accenture.api

import com.accenture.api.protocol.errors.GeneralError
import com.accenture.api.protocol.library.Book
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*

object spec {
  val booksListing: PublicEndpoint[Unit, GeneralError, List[Book], Any] = endpoint.get
    .in("books")
    .errorOut(jsonBody[GeneralError])
    .out(jsonBody[List[Book]])

}
