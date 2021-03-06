package sbgjava;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class to make requests and receive responses to and from the Seven Bridges Genomics API. 
 * 
 * A request is specified by its headers, the request type (GET, PUT, POST, DELETE), request path, associated query and body. 
 * 
 * @author avasoleimany
 *
 */
public class SBG {
	
	private final String baseURL = "https://api.sbgenomics.com/" ;
	private String requestURL;
	private final String authToken;
	private String version = "1.1";
	private String path;
	private final String method;
	private JSONObject queryParams = null;
	private JSONObject bodyParams = null;
	private final HashMap<String, String> headers;
	private HttpClient sbgClient;
	
	/**
	 * Construct the base class for making an HTTP request to the Seven Bridges Genomics API. 
	 * 
	 * @param authToken
	 * @param version
	 * @param path
	 * @param method
	 * @param queryParams
	 * @param bodyParams
	 */
	public SBG(String authToken, String path, String method, JSONObject queryParams, JSONObject bodyParams){
		this.authToken = authToken;
		this.path = path;
		this.method = method;
		this.queryParams = queryParams;
		this.bodyParams = bodyParams;
		
		this.headers = new HashMap<String, String>();
		headers.put("X-SBG-Auth-Token", authToken);
		headers.put("Accept", "application/json");
		headers.put("Content-Type", "application/json");
		
		this.requestURL = baseURL + version + "/" + path;
		
		sbgClient = HttpClientBuilder.create().build();
		
	}
	
	/**
	 * Update the version attribute of the SBG API representation. 
	 * 
	 * @param newVersion
	 */
	public void setVersion(String newVersion){
		this.version = newVersion;
	}
	
	/**
	 * Retrieve the URL of the request associated with this instance of an SBG API call. 
	 * 
	 * @return URL of request
	 */
	public String getRequestURL(){
		return requestURL;
	}
	
	/**
	 * Generate and make the necessary HTTP request. 
	 * 
	 * @return
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws URISyntaxException 
	 * @throws Exception
	 */
	public HttpResponse generateRequest() throws ClientProtocolException, IOException, URISyntaxException {
		
		String URL = this.getRequestURL();
		// List<NameValuePair> queryParamsNVPair = null;
		HttpRequestBase request = null; 
		
		switch (method){
			case "GET":
				request = new HttpGet(URL);
				for (String headerKey : this.headers.keySet()){
					request.addHeader(headerKey, headers.get(headerKey));
				}
				break;
			case "POST":
				HttpPost postRequest = new HttpPost(URL);
				for (String headerKey : this.headers.keySet()){
					postRequest.addHeader(headerKey, headers.get(headerKey));
				}
				if (bodyParams != null){
					StringEntity requestBodySE = new StringEntity(bodyParams.toString());
					requestBodySE.setContentType("application/json");
					postRequest.setEntity(requestBodySE);
				}
				request = postRequest;
				break;
			case "PUT":
				HttpPut putRequest = new HttpPut(URL);
				for (String headerKey : this.headers.keySet()){
					putRequest.addHeader(headerKey, headers.get(headerKey));
				}
				if (bodyParams != null){
					StringEntity requestBodySE = new StringEntity(bodyParams.toString());
					System.out.println(requestBodySE.toString());
					requestBodySE.setContentEncoding("application/json");
					putRequest.setEntity(requestBodySE);

				}
				request = putRequest;
				System.out.println(request.getURI().toString());
				break;
			case "DELETE":
				request = new HttpDelete(URL);
				for (String headerKey : this.headers.keySet()){
					request.addHeader(headerKey, headers.get(headerKey));
				}
				break;	
			default:
				break;
		}
		
		if (queryParams != null){
			URIBuilder getUriBuilder = new URIBuilder(request.getURI());
			Iterator<?> paramKeys = queryParams.keys();
			while (paramKeys.hasNext()){
				String param = (String) paramKeys.next();
				try {
					getUriBuilder.addParameter(param, queryParams.getString(param));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} 
			URI getUri = getUriBuilder.build();
			((HttpRequestBase) request).setURI(getUri);
		}
		
		HttpResponse response = sbgClient.execute(request);
		
		return response;
		
	}
	
	/**
	 * Evaluate the status of the HTTP Request and return the appropriate response. 
	 * 
	 * @param requestResult
	 * @return
	 * @throws Exception
	 */
	public JSONObject checkAndRetrieveResponse(HttpResponse requestResult) throws Exception{
		
		int requestStatusCode = requestResult.getStatusLine().getStatusCode();
		if (requestStatusCode != 200 && requestStatusCode != 201 && requestStatusCode != 204){
			JSONObject unsuccessfulOperation = new JSONObject();
		//	unsuccessfulOperation.put("OServer responded with status code: " + Integer.toString(requestStatusCode) + " . " + requestResult.getStatusLine().getReasonPhrase(), requestStatusCode);
		//	return unsuccessfulOperation;
			throw new Exception("Server responded with status code: " + Integer.toString(requestStatusCode) + " . " + requestResult.getStatusLine().getReasonPhrase());
		}
		
		else {
			JSONObject successfulOperation = new JSONObject();
			successfulOperation.put("Operation finished successfully.", requestStatusCode);
			if (requestResult.getEntity() == null){
				return successfulOperation;
			} 
			String JSONString = EntityUtils.toString(requestResult.getEntity());
			JSONObject responseJSON = new JSONObject(JSONString);
			return responseJSON;
		}
	}
	
	
	
	
}

