package org.apache.commons.compress.archivers.tar;

import java.math.*;
import java.io.*;
import java.nio.*;
import org.apache.commons.compress.archivers.zip.*;

public class TarUtils
{
    private static final int BYTE_MASK = 255;
    static final ZipEncoding DEFAULT_ENCODING;
    static final ZipEncoding FALLBACK_ENCODING;
    
    private TarUtils() {
        super();
    }
    
    public static long parseOctal(final byte[] buffer, final int offset, final int length) {
        long result = 0L;
        int end = offset + length;
        int start = offset;
        if (length < 2) {
            throw new IllegalArgumentException("Length " + length + " must be at least 2");
        }
        if (buffer[start] == 0) {
            return 0L;
        }
        while (start < end && buffer[start] == 32) {
            ++start;
        }
        byte trailer = buffer[end - 1];
        if (trailer == 0 || trailer == 32) {
            --end;
            trailer = buffer[end - 1];
            if (trailer == 0 || trailer == 32) {
                --end;
            }
            while (start < end) {
                final byte currentByte = buffer[start];
                if (currentByte < 48 || currentByte > 55) {
                    throw new IllegalArgumentException(exceptionMessage(buffer, offset, length, start, currentByte));
                }
                result = (result << 3) + (currentByte - 48);
                ++start;
            }
            return result;
        }
        throw new IllegalArgumentException(exceptionMessage(buffer, offset, length, end - 1, trailer));
    }
    
    public static long parseOctalOrBinary(final byte[] buffer, final int offset, final int length) {
        if ((buffer[offset] & 0x80) == 0x0) {
            return parseOctal(buffer, offset, length);
        }
        final boolean negative = buffer[offset] == -1;
        if (length < 9) {
            return parseBinaryLong(buffer, offset, length, negative);
        }
        return parseBinaryBigInteger(buffer, offset, length, negative);
    }
    
    private static long parseBinaryLong(final byte[] buffer, final int offset, final int length, final boolean negative) {
        if (length >= 9) {
            throw new IllegalArgumentException("At offset " + offset + ", " + length + " byte binary number" + " exceeds maximum signed long" + " value");
        }
        long val = 0L;
        for (int i = 1; i < length; ++i) {
            val = (val << 8) + (buffer[offset + i] & 0xFF);
        }
        if (negative) {
            --val;
            val ^= (long)Math.pow(2.0, (length - 1) * 8) - 1L;
        }
        return negative ? (-val) : val;
    }
    
    private static long parseBinaryBigInteger(final byte[] buffer, final int offset, final int length, final boolean negative) {
        final byte[] remainder = new byte[length - 1];
        System.arraycopy(buffer, offset + 1, remainder, 0, length - 1);
        BigInteger val = new BigInteger(remainder);
        if (negative) {
            val = val.add(BigInteger.valueOf(-1L)).not();
        }
        if (val.bitLength() > 63) {
            throw new IllegalArgumentException("At offset " + offset + ", " + length + " byte binary number" + " exceeds maximum signed long" + " value");
        }
        return negative ? (-val.longValue()) : val.longValue();
    }
    
    public static boolean parseBoolean(final byte[] buffer, final int offset) {
        return buffer[offset] == 1;
    }
    
    private static String exceptionMessage(final byte[] buffer, final int offset, final int length, final int current, final byte currentByte) {
        String string = new String(buffer, offset, length);
        string = string.replaceAll("\u0000", "{NUL}");
        final String s = "Invalid byte " + currentByte + " at offset " + (current - offset) + " in '" + string + "' len=" + length;
        return s;
    }
    
    public static String parseName(final byte[] buffer, final int offset, final int length) {
        try {
            return parseName(buffer, offset, length, TarUtils.DEFAULT_ENCODING);
        }
        catch (IOException ex3) {
            try {
                return parseName(buffer, offset, length, TarUtils.FALLBACK_ENCODING);
            }
            catch (IOException ex2) {
                throw new RuntimeException(ex2);
            }
        }
    }
    
    public static String parseName(final byte[] buffer, final int offset, final int length, final ZipEncoding encoding) throws IOException {
        int len;
        for (len = length; len > 0 && buffer[offset + len - 1] == 0; --len) {}
        if (len > 0) {
            final byte[] b = new byte[len];
            System.arraycopy(buffer, offset, b, 0, len);
            return encoding.decode(b);
        }
        return "";
    }
    
    public static int formatNameBytes(final String name, final byte[] buf, final int offset, final int length) {
        try {
            return formatNameBytes(name, buf, offset, length, TarUtils.DEFAULT_ENCODING);
        }
        catch (IOException ex3) {
            try {
                return formatNameBytes(name, buf, offset, length, TarUtils.FALLBACK_ENCODING);
            }
            catch (IOException ex2) {
                throw new RuntimeException(ex2);
            }
        }
    }
    
    public static int formatNameBytes(final String name, final byte[] buf, final int offset, final int length, final ZipEncoding encoding) throws IOException {
        int len;
        ByteBuffer b;
        for (len = name.length(), b = encoding.encode(name); b.limit() > length && len > 0; b = encoding.encode(name.substring(0, --len))) {}
        final int limit = b.limit();
        System.arraycopy(b.array(), b.arrayOffset(), buf, offset, limit);
        for (int i = limit; i < length; ++i) {
            buf[offset + i] = 0;
        }
        return offset + length;
    }
    
    public static void formatUnsignedOctalString(final long value, final byte[] buffer, final int offset, final int length) {
        int remaining = length;
        --remaining;
        if (value == 0L) {
            buffer[offset + remaining--] = 48;
        }
        else {
            long val;
            for (val = value; remaining >= 0 && val != 0L; val >>>= 3, --remaining) {
                buffer[offset + remaining] = (byte)(48 + (byte)(val & 0x7L));
            }
            if (val != 0L) {
                throw new IllegalArgumentException(value + "=" + Long.toOctalString(value) + " will not fit in octal number buffer of length " + length);
            }
        }
        while (remaining >= 0) {
            buffer[offset + remaining] = 48;
            --remaining;
        }
    }
    
    public static int formatOctalBytes(final long value, final byte[] buf, final int offset, final int length) {
        int idx = length - 2;
        formatUnsignedOctalString(value, buf, offset, idx);
        buf[offset + idx++] = 32;
        buf[offset + idx] = 0;
        return offset + length;
    }
    
    public static int formatLongOctalBytes(final long value, final byte[] buf, final int offset, final int length) {
        final int idx = length - 1;
        formatUnsignedOctalString(value, buf, offset, idx);
        buf[offset + idx] = 32;
        return offset + length;
    }
    
    public static int formatLongOctalOrBinaryBytes(final long value, final byte[] buf, final int offset, final int length) {
        final long maxAsOctalChar = (length == 8) ? 2097151L : 8589934591L;
        final boolean negative = value < 0L;
        if (!negative && value <= maxAsOctalChar) {
            return formatLongOctalBytes(value, buf, offset, length);
        }
        if (length < 9) {
            formatLongBinary(value, buf, offset, length, negative);
        }
        formatBigIntegerBinary(value, buf, offset, length, negative);
        buf[offset] = (byte)(negative ? 255 : 128);
        return offset + length;
    }
    
    private static void formatLongBinary(final long value, final byte[] buf, final int offset, final int length, final boolean negative) {
        final int bits = (length - 1) * 8;
        final long max = 1L << bits;
        long val = Math.abs(value);
        if (val >= max) {
            throw new IllegalArgumentException("Value " + value + " is too large for " + length + " byte field.");
        }
        if (negative) {
            val ^= max - 1L;
            val |= 255 << bits;
            ++val;
        }
        for (int i = offset + length - 1; i >= offset; --i) {
            buf[i] = (byte)val;
            val >>= 8;
        }
    }
    
    private static void formatBigIntegerBinary(final long value, final byte[] buf, final int offset, final int length, final boolean negative) {
        final BigInteger val = BigInteger.valueOf(value);
        final byte[] b = val.toByteArray();
        final int len = b.length;
        final int off = offset + length - len;
        System.arraycopy(b, 0, buf, off, len);
        final byte fill = (byte)(negative ? 255 : 0);
        for (int i = offset + 1; i < off; ++i) {
            buf[i] = fill;
        }
    }
    
    public static int formatCheckSumOctalBytes(final long value, final byte[] buf, final int offset, final int length) {
        int idx = length - 2;
        formatUnsignedOctalString(value, buf, offset, idx);
        buf[offset + idx++] = 0;
        buf[offset + idx] = 32;
        return offset + length;
    }
    
    public static long computeCheckSum(final byte[] buf) {
        long sum = 0L;
        for (int i = 0; i < buf.length; ++i) {
            sum += (0xFF & buf[i]);
        }
        return sum;
    }
    
    static {
        DEFAULT_ENCODING = ZipEncodingHelper.getZipEncoding(null);
        FALLBACK_ENCODING = new ZipEncoding() {
            TarUtils$1() {
                super();
            }
            
            public boolean canEncode(final String name) {
                return true;
            }
            
            public ByteBuffer encode(final String name) {
                final int length = name.length();
                final byte[] buf = new byte[length];
                for (int i = 0; i < length; ++i) {
                    buf[i] = (byte)name.charAt(i);
                }
                return ByteBuffer.wrap(buf);
            }
            
            public String decode(final byte[] buffer) {
                final int length = buffer.length;
                final StringBuilder result = new StringBuilder(length);
                for (final byte b : buffer) {
                    if (b == 0) {
                        break;
                    }
                    result.append((char)(b & 0xFF));
                }
                return result.toString();
            }
        };
    }
}
