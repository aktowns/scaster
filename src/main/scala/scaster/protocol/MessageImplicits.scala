package scaster.protocol

import extensions.api.cast_channel.cast_channel.CastMessage
import extensions.api.cast_channel.cast_channel.CastMessage.PayloadType
import scaster.protocol.Payloads.CastPayload

object MessageImplicits {
  class CastMessageAdditions(val x: CastMessage) {
    def tryReadPayload(): Option[CastPayload] = {
      if (x.payloadType == PayloadType.STRING) {
        val str = x.getPayloadUtf8
        Some(PayloadParser.parseUTF8(str))
      } else {
        None
      }
    }
  }

  implicit def cast_message_has_try_read_payload(o: CastMessage) = new CastMessageAdditions(o)
}
