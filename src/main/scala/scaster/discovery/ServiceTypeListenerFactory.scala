package scaster.discovery

import javax.jmdns.{JmDNS, ServiceEvent, ServiceListener, ServiceTypeListener}

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

object ServiceTypeListenerFactory {
  val logger = Logger(LoggerFactory.getLogger("scaster"))

  def makeServiceTypeListener(jmdns: JmDNS,
                              discF: (ServiceEvent) => Unit,
                              remoF: (ServiceEvent) => Unit): ServiceTypeListener = {

    new ServiceTypeListener {
      override def serviceTypeAdded(event: ServiceEvent): Unit = {
        Option(event.getType) match {
          case Some(typ) =>
            if (typ.equals("_googlecast._tcp.local.")) {
              logger.info("Found chromecast servicetype")
              jmdns.addServiceListener(typ, new ServiceListener {
                override def serviceAdded(event: ServiceEvent): Unit = {
                  logger.info(s"requesting resolution for ${event.getInfo.getQualifiedName}")
                  jmdns.requestServiceInfo(typ, event.getName)
                }

                override def serviceResolved(event: ServiceEvent): Unit = discF(event)

                override def serviceRemoved(event: ServiceEvent): Unit = remoF(event)
              })
            }
          case _ =>
        }
      }
      override def subTypeForServiceTypeAdded(event: ServiceEvent): Unit = Unit
    }
  }
}
