package scaster.protocol

object CastPayloads {
  sealed trait CastPayload

  case class GenericPayload(`type`: String) extends CastPayload

  case class GetStatusPayload(`type`: String, requestId: Int) extends CastPayload

  case class LaunchPayload(`type`: String, appId: String, requestId: String) extends CastPayload

  case class StatusNamespace(name: String)

  case class StatusVolume(level: Long, muted: Boolean)

  case class StatusApplication(appId: String, displayName: String, namespaces: Array[StatusNamespace],
                               sessionId: String, statusText: String, transportId: String)

  case class Status(applications: Array[StatusApplication], isActiveInput: Option[Boolean], volume: StatusVolume)

  case class StatusPayload(`type`: String, requestId: Int, status: Status) extends CastPayload

}
