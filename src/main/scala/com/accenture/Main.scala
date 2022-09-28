package com.accenture

import com.accenture.api.Api
import com.accenture.config.ApiConfig
import com.accenture.controller.LibraryController
import com.accenture.services.LibraryService
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.server.interceptor.log.DefaultServerLog
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import zhttp.http.HttpApp
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio.*
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  private val prometeusMetric = PrometheusMetrics.default[Task]()

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    application.provide(
      LibraryService.dummyLayer,
      LibraryController.layer,
      ZLayer.succeed(prometeusMetric),
      ApiConfig.fromEnv,
      Api.layer
    )

  private val application = for {
    api <- ZIO.service[Api]
    apiConfig <- ZIO.service[ApiConfig]
    result <- buildServer(apiConfig, prometeusMetric, api)
  } yield result

  private def buildServer(apiConfig: ApiConfig, prometheusMetrics: PrometheusMetrics[Task], api: Api): URIO[Any, ExitCode] = {
    val logError = (msg: String, maybeError: Option[Throwable]) =>
      maybeError
        .map(ex => ZIO.logErrorCause(msg, Cause.die(ex)))
        .getOrElse(ZIO.logDebug(msg))

    val defaultServerLog =
      DefaultServerLog[Task](
        doLogWhenReceived = ZIO.logDebug,
        doLogWhenHandled = logError,
        doLogAllDecodeFailures = logError,
        doLogExceptions = (msg: String, ex: Throwable) => logError(msg, Some(ex)),
        noLog = ZIO.unit
      )

    val serverOptions: ZioHttpServerOptions[Any] =
      ZioHttpServerOptions.customiseInterceptors
        .serverLog(defaultServerLog)
        .metricsInterceptor(prometheusMetrics.metricsInterceptor())
        .options

    val app: HttpApp[Any, Throwable] = ZioHttpInterpreter(serverOptions).toHttp(api.allEndpoints)
    Server
      .start(apiConfig.port, app)
      .provideSomeLayer(EventLoopGroup.auto() ++ ServerChannelFactory.auto ++ Scope.default)
      .exitCode
  }

}
