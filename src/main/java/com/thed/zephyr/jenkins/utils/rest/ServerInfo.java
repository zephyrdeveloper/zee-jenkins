package com.thed.zephyr.jenkins.utils.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ServerInfo {

	private static String URL_GET_USERS = "{SERVER}/flex/services/rest/{REST_VERSION}/user/current";
	private static String URL_GET_ZEPHYR_VERSION = "{SERVER}/flex/version?format=xml";
	private static String INVALID_USER_CREDENTIALS = "Invalid user credentials";
	private static String INVALID_USER_ROLE = "User role should be manager or lead";
	private static String VALID_USER_ROLE = "User authentication is successful";

	public static boolean findServerAddressIsValidZephyrURL(RestClient restClient) {
		
		String zephyrVersion = findZephyrVersion(restClient);
		
		if (zephyrVersion == null) {
			return false;
		}
		
		if (zephyrVersion.equals("4.8")) {
			URL_GET_USERS = URL_GET_USERS.replace("{REST_VERSION}", "v1");
		} else {
			URL_GET_USERS = URL_GET_USERS.replace("{REST_VERSION}", "latest");
		}
		return true;
	}

	public static Map<Boolean, String> validateCredentials(RestClient restClient, String restVersion) {

		Map<Boolean, String> statusMap = new HashMap<Boolean, String>();
		statusMap.put(false, INVALID_USER_CREDENTIALS);


		HttpResponse response = null;
		try {
			String constructedURL = URL_GET_USERS
					.replace("{SERVER}", restClient.getUrl()).replace("{REST_VERSION}", restVersion);
			response = restClient.getHttpclient().execute(new HttpGet(constructedURL), restClient.getContext());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode >= 200 && statusCode < 300) {
			HttpEntity entity = response.getEntity();
			String string = null;
			try {
				string = EntityUtils.toString(entity);

				JSONObject userObj = new JSONObject(string);

				if (userObj.getString("username").trim().equals(restClient.getUserName())) {
					String userRole = userObj.getJSONArray("roles")
							.getJSONObject(0).getString("name").trim();

					if (userRole.equalsIgnoreCase("manager")
							|| userRole.equalsIgnoreCase("lead")) {
						statusMap.clear();
						statusMap.put(true, VALID_USER_ROLE);
					} else if (userRole.contains("manager")	
							|| userRole.contains("Admin")) {
						statusMap.clear();
						statusMap.put(true, VALID_USER_ROLE);
						 
					} else {
						statusMap.clear();
						statusMap.put(false, INVALID_USER_ROLE);
						return statusMap;
					}
				}

			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (statusCode >= 401) {

			return statusMap;

		} else if (statusCode >= 400) {
			statusMap.clear();
			statusMap.put(false, INVALID_USER_ROLE);
			return statusMap;

		} else {
			try {
				throw new ClientProtocolException(
						"Unexpected response status: " + statusCode);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		}
		return statusMap;

	}
	
	public static long getUserId(RestClient restClient, String restVersion) {


		long userId = 0;
		HttpResponse response = null;
		try {
			String constructedURL = URL_GET_USERS
					.replace("{SERVER}", restClient.getUrl()).replace("{REST_VERSION}", restVersion);
			response = restClient.getHttpclient().execute(new HttpGet(constructedURL), restClient.getContext());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode >= 200 && statusCode < 300) {
			HttpEntity entity = response.getEntity();
			String string = null;
			try {
				string = EntityUtils.toString(entity);

				JSONObject userObj = new JSONObject(string);

				userId = userObj.getLong("id");
				
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			try {
				throw new ClientProtocolException(
						"Unexpected response status: " + statusCode);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		}
		return userId;

	}
	
	public static String findZephyrVersion(RestClient restClient) {
		String zephyrVersion = null;
		CloseableHttpClient httpclient = null;
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

		if (httpclient == null) {
			return zephyrVersion;
		}
		HttpResponse response = null;
		try {
			String constructedURL = URL_GET_ZEPHYR_VERSION.replace("{SERVER}",
					restClient.getUrl());

			response = httpclient.execute(new HttpGet(constructedURL));
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode == 200) {
			HttpEntity entity = response.getEntity();
			String string = null;
			try {
				string = EntityUtils.toString(entity);

//				Sample response
//				String xmlStr = "<node><version>4.7</version><build>9213</build><startDate>Sep 27, 2016</startDate><date>Jun 1, 2017</date><licenseEdition>ENTERPRISE</licenseEdition><licenseType>NAMED</licenseType><totalUsers>10</totalUsers><currentlyLogged>0</currentlyLogged><validity><valid till='Jun 1, 2017'>10</valid></validity></node>";

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    factory.setValidating(true);
			    factory.setIgnoringElementContentWhitespace(true);

					InputStream stream = new ByteArrayInputStream(string.getBytes("utf-8"));
			        DocumentBuilder builder = factory.newDocumentBuilder();
			        Document doc = builder.parse(stream);
			        
			        zephyrVersion = doc.getElementsByTagName("version").item(0).getTextContent();
			
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}

		} else {
			try {
				throw new ClientProtocolException(
						"Unexpected response status: " + statusCode);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		}
		return zephyrVersion;
	}
	
	public static void main(String[] args) {
		System.out.println(findZephyrVersion(new RestClient("http://zephyrdemo.yourzephyr.com", "test.lead", "test.lead")));
	}
}
