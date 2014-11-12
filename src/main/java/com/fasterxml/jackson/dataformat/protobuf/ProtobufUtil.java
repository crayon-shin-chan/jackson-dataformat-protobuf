package com.fasterxml.jackson.dataformat.protobuf;

public class ProtobufUtil
{
    public final static int SECONDARY_BUFFER_LENGTH = 64000;

    /**
     * While we could get all fancy on allocating secondary buffer (after
     * initial one), let's start with very simple strategy of medium-length
     * buffer.
     */
    public static byte[] allocSecondary(byte[] curr) {
        return new byte[SECONDARY_BUFFER_LENGTH];
    }
    
    // NOTE: no negative values accepted
    public static int lengthLength(int len) {
        if (len <= 0x7F) { // 7 bytes
            // if negatives were allowed, would need another check here
            return 1;
        }
        if (len <= 0x3FFF) { // 14 bytes
            return 2;
        }
        if (len <= 0x1FFFFF) { // 21 bytes
            return 3;
        }
        if (len <= 0x1FFFFF) { // 21 bytes
            return 3;
        }
        if (len <= 0x0FFFFFFF) { // 28 bytes
            return 4;
        }
        return 5;
    }

    /**
     * NOTE: caller MUST ensure buffer has room for at least 5 bytes
     */
    public static int appendLengthLength(int len, byte[] buffer, int ptr)
    {
        // first a quick check for common case
        if (len <= 0x7F) {
            // if negatives were allowed, would need another check here
            buffer[ptr++] = (byte) len;
            return ptr;
        }
        // but loop for longer content
        do {
            buffer[ptr++] = (byte) (0x80 + (len & 0x7F));
            len = len >> 7;
        } while (len > 0x7F);
        buffer[ptr++] = (byte) len;
        return ptr;
    }
    
    // NOTE: no negative values accepted
    public static byte[] lengthAsBytes(int len) {
        int bytes = lengthLength(len);
        byte[] result = new byte[bytes];
        int last = bytes-1;

        for (int i = 0; i < last; ++i) {
            result[i] = (byte) (0x80 + (len & 0x7F));
            len >>= 7;
        }
        result[last] = (byte) len;
        return result;
    }
    
    public static int zigzagEncode(int input) {
        // Canonical version:
        //return (input << 1) ^  (input >> 31);
        // but this is even better
        if (input < 0) {
            return (input << 1) ^ -1;
        }
        return (input << 1);
    }

    public static int zigzagDecode(int encoded) {
        // canonical:
        //return (encoded >>> 1) ^ (-(encoded & 1));
        if ((encoded & 1) == 0) { // positive
            return (encoded >>> 1);
        }
        // negative
        return (encoded >>> 1) ^ -1;
    }
    
    public static long zigzagEncode(long input) {
        // Canonical version
        //return (input << 1) ^  (input >> 63);
        if (input < 0L) {
            return (input << 1) ^ -1L;
        }
        return (input << 1);
    }

    public static long zigzagDecode(long encoded) {
        // canonical:
        //return (encoded >>> 1) ^ (-(encoded & 1));
        if ((encoded & 1) == 0) { // positive
            return (encoded >>> 1);
        }
        // negative
        return (encoded >>> 1) ^ -1L;
    }
}
