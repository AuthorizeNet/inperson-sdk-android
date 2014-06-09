package net.authorize.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.authorize.Environment;
import net.authorize.ITransaction;
import net.authorize.ResponseField;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;

import sun.rmi.runtime.Log;

/**
 * Transportation object used to facilitate the communication with the
 * respective gateway.
 * 
 */
public class HttpClient {

	public static Logger logger = java.util.logging.Logger
			.getLogger("net.authorize.util.HttpClient");
	public static final String ENCODING = "UTF-8";
	public static int HTTP_CONNECTION_TIMEOUT = 60000;
	public static int HTTP_SOCKET_CONNECTION_TIMEOUT = 60000;

	/**
	 * Creates the http post object for an environment and transaction
	 * container.
	 * 
	 * @param env
	 * @param transaction
	 * @return HttpPost object
	 * 
	 * @throws Exception
	 */
	private static HttpPost createHttpPost(Environment env,
			ITransaction transaction) throws Exception {
		URI postUrl = null;
		HttpPost httpPost = null;

		if (!(transaction instanceof net.authorize.sim.Transaction)) {
			postUrl = new URI(env.getXmlBaseUrl() + "/xml/v1/request.api");
			httpPost = new HttpPost(postUrl);
			httpPost.setHeader("Content-Type", "text/xml; charset=utf-8");
			httpPost.setEntity(new StringEntity(transaction
					.toAuthNetPOSTString()));
		}

		return httpPost;
	}

	/**
	 * Creates a response map for a given response string and transaction
	 * container.
	 * 
	 * @param transaction
	 * @param responseString
	 * @return Map<ResponseField, String> container
	 * @throws UnsupportedEncodingException
	 */
	private static Map<ResponseField, String> createResponseMap(
			ITransaction transaction, String responseString)
			throws UnsupportedEncodingException {

		Map<ResponseField, String> responseMap = null;

		// sim
		if (transaction instanceof net.authorize.sim.Transaction) {

			String decodedResponseData = URLDecoder.decode(responseString,
					HTTP.UTF_8);
			if (Environment.SANDBOX.equals(transaction.getMerchant()
					.getEnvironment())
					|| Environment.SANDBOX_TESTMODE.equals(transaction
							.getMerchant().getEnvironment())) {

				//
			}

			responseMap = ResponseParser
					.parseResponseString(decodedResponseData);
		}

		return responseMap;
	}

	/**
	 * Executes a Transaction against a given Environment.
	 * 
	 * @param environment
	 * @param transaction
	 * @return Return a HashMap<ResponseField> that contains semi-processed data
	 *         after a request was posted.
	 */
	public static Map<ResponseField, String> execute(Environment environment,
			ITransaction transaction) {
		Map<ResponseField, String> responseMap = new HashMap<ResponseField, String>();

		if (environment != null && transaction != null) {
			try {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpParams httpParams = httpClient.getParams();
				HttpConnectionParams.setConnectionTimeout(httpParams,
						HTTP_CONNECTION_TIMEOUT);
				HttpConnectionParams.setSoTimeout(httpParams,
						HTTP_SOCKET_CONNECTION_TIMEOUT);

				// create the HTTP POST object
				HttpPost httpPost = createHttpPost(environment, transaction);

				// get the raw data being sent for logging while in sandbox type
				// modes
				if (Environment.SANDBOX.equals(environment)
						|| Environment.SANDBOX_TESTMODE.equals(environment)) {
					InputStream outstream = (InputStream) httpPost.getEntity()
							.getContent();
					String requestData = convertStreamToLoggableString(
							transaction, outstream);
					//

				}

				// execute the request
				HttpResponse httpResponse = httpClient.execute(httpPost);
				String rawResponseString = "";
				if (httpResponse != null
						&& httpResponse.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = httpResponse.getEntity();

					// get the raw data being received
					InputStream instream = (InputStream) entity.getContent();
					rawResponseString = convertStreamToLoggableString(
							transaction, instream);
				}
				// handle HTTP errors
				else {
					StringBuilder responseBuilder = new StringBuilder();
					responseBuilder
							.append(3)
							.append(net.authorize.sim.Transaction.TRANSACTION_FIELD_DELIMITER);
					responseBuilder
							.append(3)
							.append(net.authorize.sim.Transaction.TRANSACTION_FIELD_DELIMITER);
					responseBuilder
							.append(22)
							.append(net.authorize.sim.Transaction.TRANSACTION_FIELD_DELIMITER);
					responseBuilder.append(httpResponse != null ? httpResponse
							.getStatusLine().getReasonPhrase() : " ");
					rawResponseString = responseBuilder.toString();
				}

				httpClient.getConnectionManager().shutdown();

				responseMap = HttpClient.createResponseMap(transaction,
						rawResponseString);
			} catch (Exception e) {
				//

			}
		}

		return responseMap;
	}

	/**
	 * Converts a response inputstream into a string.
	 * 
	 * @param transaction
	 *            Transaction object
	 * @param responseInputStream
	 *            response input stream
	 * 
	 * @return string containing the response with sensitive data removed
	 */
	public static String convertStreamToLoggableString(
			ITransaction transaction, InputStream responseInputStream) {
		InputStreamReader responseReader = new InputStreamReader(
				responseInputStream);
		BufferedReader bufferedResponseReader = new BufferedReader(
				responseReader);
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = bufferedResponseReader.readLine()) != null) {
				if (transaction.getMerchant() != null
						&& transaction.getMerchant()
								.getMerchantAuthentication() != null
						&& transaction.getMerchant()
								.getMerchantAuthentication().getSecret() != null) {
					line = line.replaceAll(transaction.getMerchant()
							.getMerchantAuthentication().getSecret(), "");
				}
				if (transaction.getBankAccount() != null
						&& transaction.getBankAccount().getBankAccountNumber() != null) {
					line = line.replaceAll(transaction.getBankAccount()
							.getBankAccountNumber(), "");
				}
				if (transaction.getCreditCard() != null) {
					if (transaction.getCreditCard().getCardCode() != null) {
						line = line.replaceAll(transaction.getCreditCard()
								.getCardCode(), "");
					}
					if (transaction.getCreditCard().getCreditCardNumber() != null) {
						line = line.replaceAll(transaction.getCreditCard()
								.getCreditCardNumber(), Luhn.safeFormat('X',
								transaction.getCreditCard()
										.getCreditCardNumber()));
					}
				}
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedResponseReader.close();
				responseReader.close();
				responseInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * Executes a Transaction against a given Environment.
	 * 
	 * @param environment
	 * @param transaction
	 * @return Return a HashMap<ResponseField> that contains semi-processed data
	 *         after a request was posted.
	 */
	public static String executeXML(Environment environment,
			ITransaction transaction) {

		
		DefaultHttpClient httpClient = null;
		String rawResponseString = "";

		if (environment != null && transaction != null) {
			try {
				httpClient = new DefaultHttpClient();

				// create the HTTP POST object
				HttpPost httpPost = createHttpPost(environment, transaction);
				HttpParams httpParams = httpClient.getParams();
				HttpConnectionParams.setConnectionTimeout(httpParams,
						HTTP_CONNECTION_TIMEOUT);
				HttpConnectionParams.setSoTimeout(httpParams,
						HTTP_SOCKET_CONNECTION_TIMEOUT);

				// get the raw data being sent for logging while in sandbox type
				// modes
				if (Environment.SANDBOX.equals(environment)
						|| Environment.SANDBOX_TESTMODE.equals(environment)) {
					InputStream outstream = (InputStream) httpPost.getEntity()
							.getContent();
					String loggableRequestData = convertStreamToLoggableString(
							transaction, outstream);

				}

				// execute the request
				HttpResponse httpResponse = httpClient.execute(httpPost);
				if (httpResponse != null
						&& httpResponse.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = httpResponse.getEntity();

					// get the raw data being received
					InputStream instream = (InputStream) entity.getContent();
					rawResponseString = convertStreamToLoggableString(
							transaction, instream);
				} else {
					rawResponseString = createErrorResponse(httpResponse != null ? httpResponse
							.getStatusLine().getReasonPhrase()
							: "HTTP Response is null.");
				}

			} catch (Exception e) {

				rawResponseString = createErrorResponse("Unknown error : "
						+ e.getMessage());
			} finally {
				httpClient.getConnectionManager().shutdown();

				if (rawResponseString == null)
					return null;

				if (Environment.SANDBOX.equals(environment)
						|| Environment.SANDBOX_TESTMODE.equals(environment)) {
					//
				}
			}
		}

		return rawResponseString;
	}

	/**
	 * Create an custom error response for events where Authorize.Net data can't
	 * be extracted.
	 * 
	 * @param text
	 * @return
	 */
	private static String createErrorResponse(String text) {
		StringBuilder responseBuilder = new StringBuilder();
		responseBuilder.append("<?xml version=\"1.0\" ?>");
		responseBuilder.append("<messages><resultCode>Error</resultCode>");
		responseBuilder.append("<message><code>E00000</code>");
		responseBuilder.append("<text>");
		responseBuilder.append(text);
		responseBuilder.append("</text></message></messages>");

		return responseBuilder.toString();
	}

	public static class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

}
