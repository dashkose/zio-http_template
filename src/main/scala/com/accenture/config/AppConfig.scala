package com.accenture.config

import zio.*
import zio.config.*
import ConfigDescriptor._

case class ApiConfig(port: Int)
object ApiConfig {
  private val configDesc: ConfigDescriptor[ApiConfig] =
    int("HTTP_PORT").to[ApiConfig]
  def fromEnv: ZLayer[Any, ReadError[String], ApiConfig] = ZConfig.fromSystemEnv(configDesc)
}

case class AppConfig(apiConfig: ApiConfig)
object AppConfig {
  private val appLayer = ZLayer(for {
    apiConfig <- ZIO.service[ApiConfig]
  } yield AppConfig(apiConfig))
  def fromEnv: ZLayer[Any, ReadError[String], AppConfig] = ApiConfig.fromEnv >>> appLayer
}
