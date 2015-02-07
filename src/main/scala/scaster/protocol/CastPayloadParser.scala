package scaster.protocol

import net.liftweb.json._
import scaster.protocol.CastPayloads.{LaunchPayload, StatusPayload, GenericPayload, CastPayload}

object CastPayloadParser {
  object CastPayloadType {
    val PING = "PING"
    val PONG = "PONG"
    val CONNECTED = "CONNECT"
    val LAUNCH = "LAUNCH"
    val STATUS = "RECEIVER_STATUS"
  }

  implicit val formats = DefaultFormats

  def parseUTF8(str: String): CastPayload = {
    val json = parse(str)
    val generic = json.extract[GenericPayload]
    generic.`type` match {
      case CastPayloadType.STATUS => json.extract[StatusPayload]
      case CastPayloadType.LAUNCH => json.extract[LaunchPayload]
      case _ => generic
    }

  }
}
