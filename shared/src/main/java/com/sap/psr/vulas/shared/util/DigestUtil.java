package com.sap.psr.vulas.shared.util;

import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Offers methods for computing various digests. */
public class DigestUtil {

  private static final Log log = LogFactory.getLog(DigestUtil.class);

  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  /**
   * Returns a digest for the given {@link String}, using the given {@link Charset} (typically
   * {@link StandardCharsets#UTF_8}) and {@link DigestAlgorithm}.
   *
   * @param _source a {@link java.lang.String} object.
   * @param _charset a {@link java.nio.charset.Charset} object.
   * @param _alg a {@link com.sap.psr.vulas.shared.enums.DigestAlgorithm} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String getDigestAsString(
      String _source, Charset _charset, DigestAlgorithm _alg) {
    return DigestUtil.bytesToHex(DigestUtil.getDigestAsBytes(_source, _charset, _alg));
  }

  private static final byte[] getDigestAsBytes(
      String _source, Charset _charset, DigestAlgorithm _alg) {
    return DigestUtil.getDigestAsBytes(_source.getBytes(_charset), _alg);
  }

  private static final byte[] getDigestAsBytes(byte[] _bytes, DigestAlgorithm _alg) {
    byte[] digest = null;
    try {
      final MessageDigest md = MessageDigest.getInstance(_alg.toString());
      digest = md.digest(_bytes);
    } catch (NoSuchAlgorithmException e) {
      DigestUtil.log.error("Error while instantiating [" + _alg + "] digest: " + e.getMessage());
    } catch (ArrayIndexOutOfBoundsException be) {
      DigestUtil.log.error("Error while computing [" + _alg + "] digest: " + be.getMessage());
    }
    return digest;
  }

  /**
   * bytesToHex.
   *
   * @param bytes an array of {@link byte} objects.
   * @return a {@link java.lang.String} object.
   */
  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = DigestUtil.HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = DigestUtil.HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }
}
