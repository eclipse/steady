/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.conn.ssl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.SSLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.annotation.Immutable;
import org.apache.http.conn.util.InetAddressUtils;

/**
 * /** Abstract base class for all standard {@link org.apache.http.conn.ssl.X509HostnameVerifier}
 * implementations that provides methods to extract Common Name (CN) and alternative subjects
 * (subjectAlt) from {@link java.security.cert.X509Certificate} being validated as well as {@link
 * #verify(String, String[], String[], boolean)} method that implements common certificate subject
 * validation logic.
 *
 * @since 4.4
 */
@Immutable
public abstract class AbstractCommonHostnameVerifier extends AbstractBaseHostnameVerifier {

  /**
   * This contains a list of 2nd-level domains that aren't allowed to have wildcards when combined
   * with country-codes. For example: [*.co.uk].
   *
   * <p>The [*.co.uk] problem is an interesting one. Should we just hope that CA's would never
   * foolishly allow such a certificate to happen? Looks like we're the only implementation guarding
   * against this. Firefox, Curl, Sun Java 1.4, 5, 6 don't bother with this check.
   */
  private static final String[] BAD_COUNTRY_2LDS = {
    "ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info", "lg", "ne", "net", "or", "org"
  };

  static {
    // Just in case developer forgot to manually sort the array.  :-)
    Arrays.sort(BAD_COUNTRY_2LDS);
  }

  private final Log log = LogFactory.getLog(getClass());

  @Override
  public final void verify(final String host, final X509Certificate cert) throws SSLException {
    final String subjectPrincipal = cert.getSubjectX500Principal().toString();
    final String[] cns = extractCNs(subjectPrincipal);
    final String[] subjectAlts = getSubjectAlts(cert, host);
    verify(host, cns, subjectAlts);
  }

  public final void verify(
      final String host,
      final String[] cns,
      final String[] subjectAlts,
      final boolean strictWithSubDomains)
      throws SSLException {

    // Build the list of names we're going to check.  Our DEFAULT and
    // STRICT implementations of the HostnameVerifier only use the
    // first CN provided.  All other CNs are ignored.
    // (Firefox, wget, curl, Sun Java 1.4, 5, 6 all work this way).
    final LinkedList<String> names = new LinkedList<String>();
    if (cns != null && cns.length > 0 && cns[0] != null) {
      names.add(cns[0]);
    }
    if (subjectAlts != null) {
      for (final String subjectAlt : subjectAlts) {
        if (subjectAlt != null) {
          names.add(subjectAlt);
        }
      }
    }

    if (names.isEmpty()) {
      final String msg = "Certificate for <" + host + "> doesn't contain CN or DNS subjectAlt";
      throw new SSLException(msg);
    }

    // StringBuilder for building the error message.
    final StringBuilder buf = new StringBuilder();

    // We're can be case-insensitive when comparing the host we used to
    // establish the socket to the hostname in the certificate.
    final String hostName = normaliseIPv6Address(host.trim().toLowerCase(Locale.ROOT));
    boolean match = false;
    for (final Iterator<String> it = names.iterator(); it.hasNext(); ) {
      // Don't trim the CN, though!
      String cn = it.next();
      cn = cn.toLowerCase(Locale.ROOT);
      // Store CN in StringBuilder in case we need to report an error.
      buf.append(" <");
      buf.append(cn);
      buf.append('>');
      if (it.hasNext()) {
        buf.append(" OR");
      }

      // The CN better have at least two dots if it wants wildcard
      // action.  It also can't be [*.co.uk] or [*.co.jp] or
      // [*.org.uk], etc...
      final String parts[] = cn.split("\\.");
      final boolean doWildcard =
          parts.length >= 3
              && parts[0].endsWith("*")
              && validCountryWildcard(cn)
              && !isIPAddress(host);

      if (doWildcard) {
        final String firstpart = parts[0];
        if (firstpart.length() > 1) { // e.g. server*
          final String prefix = firstpart.substring(0, firstpart.length() - 1); // e.g. server
          final String suffix = cn.substring(firstpart.length()); // skip wildcard part from cn
          final String hostSuffix =
              hostName.substring(prefix.length()); // skip wildcard part from host
          match = hostName.startsWith(prefix) && hostSuffix.endsWith(suffix);
        } else {
          match = hostName.endsWith(cn.substring(1));
        }
        if (match && strictWithSubDomains) {
          // If we're in strict mode, then [*.foo.com] is not
          // allowed to match [a.b.foo.com]
          match = countDots(hostName) == countDots(cn);
        }
      } else {
        match = hostName.equals(normaliseIPv6Address(cn));
      }
      if (match) {
        break;
      }
    }
    if (!match) {
      throw new SSLException("hostname in certificate didn't match: <" + host + "> !=" + buf);
    }
  }

  /** @deprecated (4.3.1) should not be a part of public APIs. */
  @Deprecated
  public static boolean acceptableCountryWildcard(final String cn) {
    final String parts[] = cn.split("\\.");
    if (parts.length != 3 || parts[2].length() != 2) {
      return true; // it's not an attempt to wildcard a 2TLD within a country code
    }
    return Arrays.binarySearch(BAD_COUNTRY_2LDS, parts[1]) < 0;
  }

  boolean validCountryWildcard(final String cn) {
    final String parts[] = cn.split("\\.");
    if (parts.length != 3 || parts[2].length() != 2) {
      return true; // it's not an attempt to wildcard a 2TLD within a country code
    }
    return Arrays.binarySearch(BAD_COUNTRY_2LDS, parts[1]) < 0;
  }

  static String[] extractCNs(final String subjectPrincipal) throws SSLException {
    if (subjectPrincipal == null) {
      return null;
    }
    final List<String> cns = new ArrayList<String>();
    try {
      final LdapName subjectDN = new LdapName(subjectPrincipal);
      final List<Rdn> rdns = subjectDN.getRdns();
      for (int i = rdns.size() - 1; i >= 0; i--) {
        final Rdn rds = rdns.get(i);
        final Attributes attributes = rds.toAttributes();
        final Attribute cn = attributes.get("cn");
        if (cn != null) {
          try {
            final Object value = cn.get();
            if (value != null) {
              cns.add(value.toString());
            }
          } catch (NamingException ignore) {
          }
        }
      }
    } catch (InvalidNameException e) {
      throw new SSLException(subjectPrincipal + " is not a valid X500 distinguished name");
    }
    return cns.isEmpty() ? null : cns.toArray(new String[cns.size()]);
  }

  /**
   * Extracts the array of SubjectAlt DNS or IP names from an X509Certificate. Returns null if there
   * aren't any.
   *
   * @param cert X509Certificate
   * @param hostname
   * @return Array of SubjectALT DNS or IP names stored in the certificate.
   */
  private static String[] getSubjectAlts(final X509Certificate cert, final String hostname) {
    final int subjectType;
    if (isIPAddress(hostname)) {
      subjectType = 7;
    } else {
      subjectType = 2;
    }

    final LinkedList<String> subjectAltList = new LinkedList<String>();
    Collection<List<?>> c = null;
    try {
      c = cert.getSubjectAlternativeNames();
    } catch (final CertificateParsingException cpe) {
    }
    if (c != null) {
      for (final List<?> aC : c) {
        final List<?> list = aC;
        final int type = ((Integer) list.get(0)).intValue();
        if (type == subjectType) {
          final String s = (String) list.get(1);
          subjectAltList.add(s);
        }
      }
    }
    if (!subjectAltList.isEmpty()) {
      final String[] subjectAlts = new String[subjectAltList.size()];
      subjectAltList.toArray(subjectAlts);
      return subjectAlts;
    } else {
      return null;
    }
  }

  /**
   * Extracts the array of SubjectAlt DNS names from an X509Certificate. Returns null if there
   * aren't any.
   *
   * <p>Note: Java doesn't appear able to extract international characters from the SubjectAlts. It
   * can only extract international characters from the CN field.
   *
   * <p>(Or maybe the version of OpenSSL I'm using to test isn't storing the international
   * characters correctly in the SubjectAlts?).
   *
   * @param cert X509Certificate
   * @return Array of SubjectALT DNS names stored in the certificate.
   */
  public static String[] getDNSSubjectAlts(final X509Certificate cert) {
    return getSubjectAlts(cert, null);
  }

  /**
   * Counts the number of dots "." in a string.
   *
   * @param s string to count dots from
   * @return number of dots
   */
  public static int countDots(final String s) {
    int count = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == '.') {
        count++;
      }
    }
    return count;
  }

  private static boolean isIPAddress(final String hostname) {
    return hostname != null
        && (InetAddressUtils.isIPv4Address(hostname) || InetAddressUtils.isIPv6Address(hostname));
  }

  /*
   * Check if hostname is IPv6, and if so, convert to standard format.
   */
  private String normaliseIPv6Address(final String hostname) {
    if (hostname == null || !InetAddressUtils.isIPv6Address(hostname)) {
      return hostname;
    }
    try {
      final InetAddress inetAddress = InetAddress.getByName(hostname);
      return inetAddress.getHostAddress();
    } catch (
        final UnknownHostException
            uhe) { // Should not happen, because we check for IPv6 address above
      log.error("Unexpected error converting " + hostname, uhe);
      return hostname;
    }
  }
}
