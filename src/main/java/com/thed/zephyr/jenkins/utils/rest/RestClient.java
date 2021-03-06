package com.thed.zephyr.jenkins.utils.rest;

import com.thed.zephyr.jenkins.model.ZephyrInstance;
import com.thed.zephyr.jenkins.utils.URLValidator;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class RestClient {

	private CloseableHttpClient httpclient;
	private HttpClientContext context;
	private String url;
	private String userName;
	private String password;
	
	public RestClient(String url, String userName, String password) {
		super();

		this.url = URLValidator.validateURL(url);
		this.userName = userName;
		this.password = password;

		createClientContext(this.url, this.userName, this.password);
		createHttpClient();
	}

	public RestClient(@Nonnull ZephyrInstance zephyrServer) {
		this(zephyrServer.getServerAddress(), zephyrServer.getUsername(), zephyrServer.getPassword());
	}

	public void destroy(){
		if(httpclient != null){
			try {
				httpclient.close();
			} catch (IOException e) {
				//TODO - Log it properly
				e.printStackTrace();
			}
		}
	}

	private HttpClientContext createClientContext(
			String hostAddressWithProtocol, String userName, String password) {
		URL url;
		try {
			url = new URL(hostAddressWithProtocol);
			HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(),
					url.getProtocol());
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(userName, password));

			AuthCache authCache = new BasicAuthCache();
			authCache.put(targetHost, new BasicScheme());

			context = HttpClientContext.create();
			context.setCredentialsProvider(credsProvider);
			context.setAuthCache(authCache);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return context;
	}

	private void createHttpClient() {
		try {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					builder.build(),
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			httpclient = HttpClients.custom().setSSLSocketFactory(sslsf)
					.build();
		} catch (KeyManagementException e1) {
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (KeyStoreException e1) {
			e1.printStackTrace();
		}
	}

	public CloseableHttpClient getHttpclient() {
		return httpclient;
	}

	public HttpClientContext getContext() {
		return context;
	}

	public String getUrl() {
		return url;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

}
