package scaster.protocol

import java.io.{DataOutputStream, DataInputStream}
import com.trueaccord.scalapb.GeneratedMessage
import extensions.api.cast_channel.cast_channel.CastMessage
import scaster.Device
import scaster.protocol.CastPayloads.{GetStatusPayload, GenericPayload, CastPayload}
import scaster.utils.{Log, NativesHelper}

class CastProtocol(device: Device)  {
  var sock = WeakSSLSocket.connect(device.address, device.port)
  private val ips = sock.getInputStream
  private val ops = sock.getOutputStream

  def sendPacket(message: GeneratedMessage): Unit = {
    Log.shared.debug(s"<= $message")
    val writer = new DataOutputStream(ops)
    NativesHelper.writeUInt32(message.serializedSize.asInstanceOf[Long], writer)
    message.writeTo(ops)
    ops.flush()
  }

  def sendPing(): Unit = {
    val msg = CastMessageBuilder.build(CastNamespaces.HEARTBEAT, GenericPayload("PING"))
    sendPacket(msg)
  }

  def sendPong(): Unit = {
    val msg = CastMessageBuilder.build(CastNamespaces.HEARTBEAT, GenericPayload("PONG"))
    sendPacket(msg)
  }

  def sendConnect(): Unit = {
    val msg = CastMessageBuilder.build(CastNamespaces.CONNECTION, GenericPayload("CONNECT"))
    sendPacket(msg)
  }

  def sendGetStatus(requestId: Int): Unit = {
    val msg = CastMessageBuilder.build(CastNamespaces.RECEIVER, GetStatusPayload("GET_STATUS", requestId))
    sendPacket(msg)
  }

  def tryReadPacket(): Option[CastMessage] = {
    try {
      val reader = new DataInputStream(ips)
      val headerLen = reader.readInt()
      Log.shared.debug(s"Received header length: $headerLen")
      val packet = Array.fill(headerLen) {
        Byte.MinValue
      }
      reader.readFully(packet)
      val message = CastMessage.parseFrom(packet)
      Log.shared.debug(s"=> $message")
      Some(message)
    } catch {
      case e:java.io.EOFException => None
    }
  }

  def isConnected: Boolean = sock.isConnected
}