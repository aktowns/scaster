package scaster.protocol

import com.google.protobuf.ByteString
import extensions.api.cast_channel.cast_channel.CastMessage
import extensions.api.cast_channel.cast_channel.CastMessage.PayloadType
import CastPayloads.CastPayload
import net.liftweb.json.{Serialization, NoTypeHints}
import net.liftweb.json.Serialization.write

object CastMessageBuilder {
  implicit val formats = Serialization.formats(NoTypeHints)

  def buildCastMessage(namespace: String, payload: CastPayload): CastMessage = {
    new CastMessage(CastMessage.ProtocolVersion.CASTV2_1_0, "sender-0", "receiver-0",
      namespace, PayloadType.STRING, Some(write(payload)), None)
  }

  def buildCastMessage(namespace: String, payload: ByteString): CastMessage = {
    new CastMessage(CastMessage.ProtocolVersion.CASTV2_1_0, "sender-0", "receiver-0",
      namespace, PayloadType.BINARY, None, Some(payload))
  }

  def buildCastMessage(namespace: String, payload: String): CastMessage = {
    new CastMessage(CastMessage.ProtocolVersion.CASTV2_1_0, "sender-0", "receiver-0",
      namespace, PayloadType.STRING, Some(payload), None)
  }
}
