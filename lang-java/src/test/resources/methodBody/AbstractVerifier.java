package org.apache.http.conn.ssl;

import org.apache.http.annotation.*;
import org.apache.commons.logging.*;
import java.io.*;
import javax.net.ssl.*;
import java.security.cert.*;
import java.util.*;
import org.apache.http.conn.util.*;
import java.net.*;

@Immutable
public abstract class AbstractVerifier implements X509HostnameVerifier
{
    private static final String[] BAD_COUNTRY_2LDS;
    private final Log log;
    
    public AbstractVerifier() {
        super();
        log = LogFactory.getLog(this.getClass());
    }
    
    public final void verify(final String host, final SSLSocket ssl) throws IOException {
        if (host == null) {
            throw new NullPointerException("host to verify is null");
        }
        SSLSession session = ssl.getSession();
        if (session == null) {
            final InputStream in = ssl.getInputStream();
            in.available();
            session = ssl.getSession();
            if (session == null) {
                ssl.startHandshake();
                session = ssl.getSession();
            }
        }
        final Certificate[] certs = session.getPeerCertificates();
        final X509Certificate x509 = (X509Certificate)certs[0];
        this.verify(host, x509);
    }
    
    public final boolean verify(final String host, final SSLSession session) {
        try {
            final Certificate[] certs = session.getPeerCertificates();
            final X509Certificate x509 = (X509Certificate)certs[0];
            this.verify(host, x509);
            return true;
        }
        catch (SSLException e) {
            return false;
        }
    }
    
    public final void verify(final String host, final X509Certificate cert) throws SSLException {
        final String[] cns = getCNs(cert);
        final String[] subjectAlts = getSubjectAlts(cert, host);
        this.verify(host, cns, subjectAlts);
    }
    
    public final void verify(final String host, final String[] cns, final String[] subjectAlts, final boolean strictWithSubDomains) throws SSLException {
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
        final StringBuilder buf = new StringBuilder();
        final String hostName = this.normaliseIPv6Address(host.trim().toLowerCase(Locale.ENGLISH));
        boolean match = false;
        final Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            String cn = it.next();
            cn = cn.toLowerCase(Locale.ENGLISH);
            buf.append(" <");
            buf.append(cn);
            buf.append('>');
            if (it.hasNext()) {
                buf.append(" OR");
            }
            final String[] parts = cn.split("\\.");
            final boolean doWildcard = parts.length >= 3 && parts[0].endsWith("*") && this.validCountryWildcard(cn) && !isIPAddress(host);
            if (doWildcard) {
                final String firstpart = parts[0];
                if (firstpart.length() > 1) {
                    final String prefix = firstpart.substring(0, firstpart.length() - 1);
                    final String suffix = cn.substring(firstpart.length());
                    final String hostSuffix = hostName.substring(prefix.length());
                    match = (hostName.startsWith(prefix) && hostSuffix.endsWith(suffix));
                }
                else {
                    match = hostName.endsWith(cn.substring(1));
                }
                if (match && strictWithSubDomains) {
                    match = (countDots(hostName) == countDots(cn));
                }
            }
            else {
                match = hostName.equals(this.normaliseIPv6Address(cn));
            }
            if (match) {
                break;
            }
        }
        if (!match) {
            throw new SSLException("hostname in certificate didn't match: <" + host + "> !=" + (Object)buf);
        }
    }
    
    @Deprecated
    public static boolean acceptableCountryWildcard(final String cn) {
        final String[] parts = cn.split("\\.");
        return parts.length != 3 || parts[2].length() != 2 || Arrays.binarySearch(BAD_COUNTRY_2LDS, parts[1]) < 0;
    }
    
    boolean validCountryWildcard(final String cn) {
        final String[] parts = cn.split("\\.");
        return parts.length != 3 || parts[2].length() != 2 || Arrays.binarySearch(BAD_COUNTRY_2LDS, parts[1]) < 0;
    }
    
    public static String[] getCNs(final X509Certificate cert) {
        final LinkedList<String> cnList = new LinkedList<String>();
        final String subjectPrincipal = cert.getSubjectX500Principal().toString();
        final StringTokenizer st = new StringTokenizer(subjectPrincipal, ",+");
        while (st.hasMoreTokens()) {
            final String tok = st.nextToken().trim();
            if (tok.length() > 3 && tok.substring(0, 3).equalsIgnoreCase("CN=")) {
                cnList.add(tok.substring(3));
            }
        }
        if (!cnList.isEmpty()) {
            final String[] cns = new String[cnList.size()];
            cnList.toArray(cns);
            return cns;
        }
        return null;
    }
    
    private static String[] getSubjectAlts(final X509Certificate cert, final String hostname) {
        int subjectType;
        if (isIPAddress(hostname)) {
            subjectType = 7;
        }
        else {
            subjectType = 2;
        }
        final LinkedList<String> subjectAltList = new LinkedList<String>();
        Collection<List<?>> c = null;
        try {
            c = cert.getSubjectAlternativeNames();
        }
        catch (CertificateParsingException ex) {}
        if (c != null) {
            for (final List<?> list : c) {
                final List<?> aC = list;
                final int type = (int)list.get(0);
                if (type == subjectType) {
                    final String s = (String)list.get(1);
                    subjectAltList.add(s);
                }
            }
        }
        if (!subjectAltList.isEmpty()) {
            final String[] subjectAlts = new String[subjectAltList.size()];
            subjectAltList.toArray(subjectAlts);
            return subjectAlts;
        }
        return null;
    }
    
    public static String[] getDNSSubjectAlts(final X509Certificate cert) {
        return getSubjectAlts(cert, null);
    }
    
    public static int countDots(final String s) {
        int count = 0;
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) == '.') {
                ++count;
            }
        }
        return count;
    }
    
    private static boolean isIPAddress(final String hostname) {
        return hostname != null && (InetAddressUtils.isIPv4Address(hostname) || InetAddressUtils.isIPv6Address(hostname));
    }
    
    private String normaliseIPv6Address(final String hostname) {
        if (hostname == null || !InetAddressUtils.isIPv6Address(hostname)) {
            return hostname;
        }
        try {
            final InetAddress inetAddress = InetAddress.getByName(hostname);
            return inetAddress.getHostAddress();
        }
        catch (UnknownHostException uhe) {
            log.error("Unexpected error converting " + hostname, uhe);
            return hostname;
        }
    }
    
    static {
        Arrays.sort(BAD_COUNTRY_2LDS = new String[] { "ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info", "lg", "ne", "net", "or", "org" });
    }
}
