package com.gigaspaces.httpsession.qa.utils;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
public class MockSSLSocketFactory extends SSLSocketFactory {

    public MockSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(trustStrategy, hostnameVerifier);
    }

    private static final X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {
        @Override
        public void verify(String host, SSLSocket ssl) throws IOException {
            // Do nothing
        }

        @Override
        public void verify(String host, X509Certificate cert) throws SSLException {
            //Do nothing
        }

        @Override
        public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
            //Do nothing
        }

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    };

    private static final TrustStrategy trustStrategy = new TrustStrategy() {
        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType) {
            return true;
        }
    };
}