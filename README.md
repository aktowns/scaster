## SCaster

Basic wrapper around the chromecast protocol.

My attempt at learning a bit of scala.

```scala
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global
import scaster._
import scaster.discovery.Discover
import scaster.protocol.PayloadParser.CastPayloadType
import scaster.protocol.Payloads.{CastPayload, Payload, StatusPayload}
import scaster.protocol._
import scaster.protocol.MessageImplicits._

def handlePayload(prot: Protocol, payload: CastPayload): Unit = {
  payload match {
    case Payload(typ) if typ == CastPayloadType.PING => prot.sendPong()
    case StatusPayload(typ, id, status) => println($status)
    case _ => println("Ignoring payload..")
  }
}

def readPacketIn(prot: Protocol): Unit = {
  prot.tryReadPacket() match {
    case Some(message) =>
      message.tryReadPayload() match {
        case Some(payload) => handlePayload(prot, payload)
        case None => println("binary payload data returned.")
      }
    case None => Thread.sleep(4000)
  }
}

def main(args: Array[String]) {
  val device = Discover.service(None)
  device.future.onComplete {
    case Success(device: Device) =>
      val protocol = new Protocol(device)

      println("Saying hello")
      protocol.sendConnect()

      protocol.sendGetStatus(1337)

      while(protocol.isConnected) {
        readPacketIn(protocol)
      }
    case _ => println("Failed to find any devices.")
  }
}
```

![http://ashley.is/ZhGP/Screen%20Shot%202015-02-08%20at%202.00.58%20am.png](http://ashley.is/ZhGP/Screen%20Shot%202015-02-08%20at%202.00.58%20am.png)