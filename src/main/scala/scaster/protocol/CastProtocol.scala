package scaster.protocol

import java.io.{DataOutputStream, DataInputStream}
import com.trueaccord.scalapb.GeneratedMessage
import com.typesafe.scalalogging.Logger
import extensions.api.cast_channel.cast_channel.CastMessage
import extensions.api.cast_channel.cast_channel.CastMessage.PayloadType
import org.slf4j.LoggerFactory
import scaster.Device
import scaster.protocol.CastPayloads.CastPayload

class CastProtocol(device: Device)  {
  val logger = Logger(LoggerFactory.getLogger("scaster"))

  var sock = WeakSSLSocket.connect(device.address, device.port)
  private var ips = sock.getInputStream
  private var ops = sock.getOutputStream

  def sendPacket(message: GeneratedMessage): Unit = {
    logger.debug(s"<= $message")
    val writer = new DataOutputStream(ops)
    NativesHelper.writeUInt32(message.serializedSize.asInstanceOf[Long], writer)
    message.writeTo(ops)
    ops.flush()
  }

  def readPacket(): Option[CastMessage] = {
    try {
      logger.debug("Attempting to read a packet")
      val reader = new DataInputStream(ips)
      val headerLen = reader.readInt()
      logger.debug(s"Received header length: $headerLen")
      val packet = Array.fill(headerLen) {
        Byte.MinValue
      }
      reader.readFully(packet)
      val message = CastMessage.parseFrom(packet)
      logger.debug(s"=> $message")
      Some(message)
    } catch {
      case e:java.io.EOFException =>
        logger.warn("EOF on packet read")
        None
    }
  }

  def isConnected: Boolean = sock.isConnected
}

object CastProtocol {
  def tryReadPayload(pack: CastMessage): Option[CastPayload] = {
    if (pack.payloadType == PayloadType.STRING) {
      val str = pack.getPayloadUtf8
      Some(CastPayloadParser.parseUTF8(str))
    } else {
      None
    }
  }
}
