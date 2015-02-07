import scaster.utils.Log
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global
import scaster._
import scaster.discovery.Discover
import scaster.protocol.CastPayloadParser.CastPayloadType
import scaster.protocol.CastPayloads.{CastPayload, GenericPayload, StatusPayload}
import scaster.protocol._

object Main {
  def handlePayload(prot: CastProtocol, payload: CastPayload): Unit = {
    payload match {
      case GenericPayload(typ) if typ == CastPayloadType.PING => prot.sendPong()
      case StatusPayload(typ, id, status) => Log.shared.info(s"Chromecast($id): $status")
      case _ => Log.shared.info("Ignoring payload..")
    }
  }

  def readPacketIn(prot: CastProtocol): Unit = {
    prot.tryReadPacket() match {
      case Some(message) =>
        CastProtocol.tryReadPayload(message) match {
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
        val protocol = new CastProtocol(device)

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
