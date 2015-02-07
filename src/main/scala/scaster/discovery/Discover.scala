package scaster.discovery

import java.net.InetAddress
import javax.jmdns._

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scaster.Device

import scala.concurrent.Promise

object Discover {
  private val jmdns = JmDNS.create(InetAddress.getLocalHost)
  val logger = Logger(LoggerFactory.getLogger("scaster"))

  def service(maybeName: Option[String]): Promise[Device] = {
    val device = Promise[Device]()

    val listener = ServiceTypeListenerFactory.makeServiceTypeListener(jmdns, (event: ServiceEvent) => {
      val name = maybeName.getOrElse(event.getName)
      if (name.equals(event.getName)) {
        device.success(new Device(event.getName, event.getInfo.getAddress, event.getInfo.getPort))
      } else {
        logger.debug(s"Ignoring ${event.getName}")
      }
    }, (event: ServiceEvent) => {
      logger.info(s"serviceRemoved $event")
    })

    logger.info("Beginning mDNS discovery..")

    jmdns.addServiceTypeListener(listener)
    jmdns.requestServiceInfo("_googlecast._tcp.local", "")
    device
  }
}
