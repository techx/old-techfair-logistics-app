package com.morlunk.mumbleclient.service;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

import android.util.Log;

import com.morlunk.mumbleclient.Globals;

public class PlumbleSSLSocketFactory extends SSLSocketFactory {
	
	private SSLContext sslContext = SSLContext.getInstance(TLS);
	
	public PlumbleSSLSocketFactory(KeyStore keystore,
			String keystorePassword, KeyStore truststore) throws NoSuchAlgorithmException,
			KeyManagementException, KeyStoreException,
			UnrecoverableKeyException {
		super(keystore, keystorePassword, truststore);
		
		TrustManager trustManager = new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
			}
			
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
			}
		};
		
		KeyManager[] keyManagers = null;
		
		if(keystore != null) {
			Log.i(Globals.LOG_TAG, "Loaded "+keystore.size()+" client certificate(s).");
			
			KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			factory.init(keystore, keystorePassword.toCharArray());
			keyManagers = factory.getKeyManagers();
		}
		
		sslContext.init(keyManagers, new TrustManager[] { trustManager }, new SecureRandom());
	}
	
	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	}
	
	@Override
	public Socket createSocket() throws IOException {
		return sslContext.getSocketFactory().createSocket();
	}

}
