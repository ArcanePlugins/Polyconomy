package io.github.arcaneplugins.polyconomy.plugin.core.util

import java.nio.ByteBuffer
import java.util.*

object ByteUtil {

    fun uuidToBytes(uuid: UUID): ByteArray {
        val byteBuffer = ByteArray(16)
        val msb = uuid.mostSignificantBits
        val lsb = uuid.leastSignificantBits
        for (i in 0..7) {
            byteBuffer[i] = (msb shr (8 * (7 - i)) and 0xFF).toByte()
            byteBuffer[i + 8] = (lsb shr (8 * (7 - i)) and 0xFF).toByte()
        }
        return byteBuffer
    }

    fun bytesToUuid(ba: ByteArray): UUID {
        val mostSigBits = ByteBuffer.wrap(ba, 0, 8).long
        val leastSigBits = ByteBuffer.wrap(ba, 8, 8).long
        return UUID(mostSigBits, leastSigBits)
    }
}