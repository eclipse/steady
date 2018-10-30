package com.acme;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;


import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.client.DefaultHttpClient;

public class Simple {

    public void callHttpClient(String _url) throws Exception {

        //build a trust manager, which checks if given certificates are valid. Here accept all certificates, never throw an exception
        X509TrustManager tm = new X509TrustManager() {

            /** Same method name and signature as below, only difference is that argument type belongs to package javax.security.cert and the other one to java.security.cert. */
            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            /** Same method name and signature as below, only difference is that argument type belongs to package javax.security.cert and the other one to java.security.cert. */
            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub

            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub

            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                // TODO Auto-generated method stub
                return null;
            }
        };


        SSLContext sslcontext = SSLContext.getInstance("SSL");
        sslcontext.init(null, new TrustManager[]{tm}, null);

        SSLSocketFactory sslsf = new SSLSocketFactory(sslcontext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", new PlainSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", sslsf, 443));

        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(schemeRegistry);
        HttpClient client = new DefaultHttpClient(manager);
        HttpGet httpGet = new HttpGet(_url);
        try {
            client.execute(httpGet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}