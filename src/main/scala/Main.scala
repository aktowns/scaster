import com.google.protobuf.ByteString
import com.typesafe.scalalogging.Logger
import extensions.api.cast_channel.cast_channel.{AuthChallenge, DeviceAuthMessage, CastMessage}
import extensions.api.cast_channel.cast_channel.CastMessage.PayloadType
import org.slf4j.LoggerFactory
import scaster._
import scaster.discovery.Discover
import scaster.protocol.CastPayloadParser.CastPayloadType
import scaster.protocol.CastPayloads.{CastPayload, GetStatusPayload, GenericPayload, StatusPayload}
import scaster.protocol._

import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

object Main {
  val logger = Logger(LoggerFactory.getLogger("scaster"))

  def handlePayload(prot: CastProtocol, payload: CastPayload): Unit = {
    payload match {
      case GenericPayload(typ) if typ == CastPayloadType.PING =>
        val msg = CastMessageBuilder.buildCastMessage(CastNamespaces.HEARTBEAT, GenericPayload("PONG"))
        prot.sendPacket(msg)
        logger.info("Chromecast: PING/PONG")
      case StatusPayload(typ, id, status) =>
        logger.info(s"Chromecast($id): $status")
      case _ => logger.info("Ignoring payload..")
    }
  }

  def readPacketIn(prot: CastProtocol): Unit = {
    prot.readPacket() match {
      case Some(message) =>
        CastProtocol.tryReadPayload(message) match {
          case Some(payload) => handlePayload(prot, payload)
          case _ => logger.info("Ignoring payload..")
        }
      case None => Thread.sleep(4000)
    }
  }

  def main(args: Array[String]) {
    val device = Discover.service(None)
    device.future.onComplete {
      case Success(device: Device) =>
        logger.info(s"Found: ${device.name} at ${device.address}")
        val protocol = new CastProtocol(device)

        logger.info("Saying hello")
        val helo = CastMessageBuilder.buildCastMessage(CastNamespaces.CONNECTION, GenericPayload("CONNECT"))
        protocol.sendPacket(helo)

        logger.info("Authenticating..")
        val authPack = new DeviceAuthMessage(None, None, None)
        val msg = CastMessageBuilder.buildCastMessage(CastNamespaces.DEVICEAUTH,
          ByteString.copyFrom(authPack.toByteArray))
        protocol.sendPacket(msg)

        val query = CastMessageBuilder.buildCastMessage(CastNamespaces.RECEIVER, GetStatusPayload("GET_STATUS", 1337))
        protocol.sendPacket(query)

        while(protocol.isConnected) {
          readPacketIn(protocol)
        }
      case _ => logger.error("Failed to find any devices.")
    }
  }
}
