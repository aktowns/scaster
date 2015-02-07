package scaster.discovery

import java.net.InetAddress
import javax.jmdns._
import scaster.Device
import scaster.utils.Log

import scala.concurrent.Promise

object Discover {
  private val jmdns = JmDNS.create(InetAddress.getLocalHost)

  def service(maybeName: Option[String]): Promise[Device] = {
    val device = Promise[Device]()

    val listener = ServiceTypeListenerFactory.makeServiceTypeListener(jmdns, (event: ServiceEvent) => {
      val name = maybeName.getOrElse(event.getName)
      if (name.equals(event.getName)) {
        val foundDevice = new Device(event.getName, event.getInfo.getInetAddresses.head, event.getInfo.getPort)
        Log.shared.info(s"Found: ${foundDevice.name} at ${foundDevice.address}")
        device.success(foundDevice)
      } else {
        Log.shared.debug(s"Ignoring ${event.getName}")
      }
    }, (event: ServiceEvent) => {
      Log.shared.info(s"serviceRemoved $event")
    })

    Log.shared.info("Beginning mDNS discovery..")

    jmdns.addServiceTypeListener(listener)
    jmdns.requestServiceInfo("_googlecast._tcp.local", "")
    device
  }
}
