package com.rycbar.read.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
@SerialName("IntRange")
private class IntRangeSurrogate(val start: Int, val endInclusive: Int)

object IntRangeSerializer : KSerializer<IntRange> {
    override val descriptor: SerialDescriptor = IntRangeSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): IntRange {
        val surrogate = decoder.decodeSerializableValue(IntRangeSurrogate.serializer())
        return IntRange(surrogate.start, surrogate.endInclusive)
    }

    override fun serialize(encoder: Encoder, value: IntRange) {
        val surrogate = IntRangeSurrogate(value.first, value.last)
        encoder.encodeSerializableValue(IntRangeSurrogate.serializer(), surrogate)
    }
}
