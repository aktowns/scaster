import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global
import scaster._
import scaster.discovery.Discover
import scaster.protocol.PayloadParser.CastPayloadType
import scaster.protocol.Payloads.{CastPayload, Payload, StatusPayload}
import scaster.protocol._
import scaster.protocol.MessageImplicits._
import scaster.utils.Log

object Main {
  def handlePayload(prot: Protocol, payload: CastPayload): Unit = {
    payload match {
      case Payload(typ) if typ == CastPayloadType.PING => prot.sendPong()
      case StatusPayload(typ, id, status) => Log.shared.info(s"Chromecast($id): $status")
      case _ => Log.shared.info("Ignoring payload..")
    }
  }

  def readPacketIn(prot: Protocol): Unit = {
    prot.tryReadPacket() match {
      case Some(message) =>
        message.tryReadPayload() match {
          case Some(payload) => handlePayload(prot, payload)
          case None => Log.shared.info("binary payload data returned.")
        }
      case None => Thread.sleep(4000)
    }
  }

  def main(args: Array[String]) {
    val device = Discover.service(None)
    device.future.onComplete {
      case Success(device: Device) =>
        val protocol = new Protocol(device)

        Log.shared.info("Saying hello")
        protocol.sendConnect()
        protocol.sendGetStatus(1337)

        while(protocol.isConnected) {
          readPacketIn(protocol)
        }
      case _ => Log.shared.error("Failed to find any devices.")
    }
  }
}
