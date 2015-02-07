import com.google.protobuf.ByteString
import com.typesafe.scalalogging.Logger
import extensions.api.cast_channel.cast_channel.{AuthChallenge, DeviceAuthMessage, CastMessage}
import extensions.api.cast_channel.cast_channel.CastMessage.PayloadType
import org.slf4j.LoggerFactory
import scaster._
import scaster.discovery.Discover
import scaster.protocol.CastPayloadParser.CastPayloadType
import scaster.protocol.CastPayloads.{GenericPayload, StatusPayload}
import scaster.protocol._

import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

object Main {
  val logger = Logger(LoggerFactory.getLogger("scaster"))

  def readPacketIn(prot: CastProtocol): Unit = {
    prot.readPacket() match {
      case Some(message) =>
        CastProtocol.tryReadPayload(message) match {
          case Some(payload) =>
            payload match {
              case GenericPayload(typ) if typ == CastPayloadType.PING =>
                val msg = new CastMessage(message.protocolVersion, message.destinationId, message.sourceId, message.namespace, PayloadType.STRING, Some( """{"type": "PONG"}"""), None)
                prot.sendPacket(msg)
                logger.info("Chromecast: PING/PONG")
              case StatusPayload(typ, id, status) =>
                logger.info(s"Chromecast($id): $status")
              case _ => logger.info("Ignoring payload..")
            }
          case _ => logger.info("Ignoring payload..")
        }
      case None =>
        logger.debug("Waiting..")
        Thread.sleep(5000)
    }
  }

  def main(args: Array[String]) {
    val device = Discover.service(None)
    device.future.onComplete {
      case Success(device: Device) =>
        logger.info(s"Found: ${device.name} at ${device.address}")
        val protocol = new CastProtocol(device)
        logger.info("Saying hello")
        val helo = new CastMessage(CastMessage.ProtocolVersion.CASTV2_1_0, "sender-0", "receiver-0", CastNamespaces.CONNECTION, PayloadType.STRING, Some("""{"type": "CONNECT"}"""), None)
        protocol.sendPacket(helo)

        logger.info("Authenticating..")
        val authPack = new DeviceAuthMessage(None, None, None)
        val msg = new CastMessage(CastMessage.ProtocolVersion.CASTV2_1_0, "sender-0", "receiver-0", CastNamespaces.DEVICEAUTH, PayloadType.BINARY, None, Some(ByteString.copyFrom(authPack.toByteArray)))
        protocol.sendPacket(msg)

        val query = new CastMessage(CastMessage.ProtocolVersion.CASTV2_1_0, "sender-0", "receiver-0", CastNamespaces.RECEIVER, PayloadType.STRING, Some("""{"type": "GET_STATUS", "requestId": 1337 }"""), None)
        protocol.sendPacket(query)

        while(protocol.isConnected) {
          readPacketIn(protocol)
        }
      case _ => logger.error("Failed to find any devices.")
    }
  }
}
