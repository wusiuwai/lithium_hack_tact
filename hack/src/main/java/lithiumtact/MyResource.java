
package lithiumtact;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// The Java class will be hosted at the URI path "/hack"
@Path("/search")
public class MyResource {

	final static int maxResults = 10;

	@GET
	@Produces("text/plain")
	public String getIt() {
		return "Got it!";
	}

	@GET
	@Path("/confluence")
	@Produces({MediaType.APPLICATION_JSON})
	public Response confluence(@QueryParam(value = "q") String query) throws IOException, URISyntaxException, JSONException {

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("query", query));
		nvps.add(new BasicNameValuePair("pageSize", Integer.valueOf(maxResults).toString()));

		URI uri = URIUtils.createURI("http", "confluence.dev.lithium.com", -1, "/rest/prototype/1/search",
				URLEncodedUtils.format(nvps, "UTF-8"), null);

		HttpGet httpGet = new HttpGet(uri);

		httpGet.addHeader("Accept", "application/json");

		String responseBody = callRest(httpGet);
		SearchResults results = new SearchResults();
		JSONObject json = new JSONObject(responseBody);

		JSONArray issues = json.getJSONArray("result");

		for (int i = 0; i < issues.length(); i++) {
			JSONObject issue = issues.getJSONObject(i);
			results.addResult(buildResultFromConfluence(issue));
		}
		return Response.ok(results).header("Access-Control-Allow-Origin", "*").build();
	}

	public SearchResult buildResultFromConfluence(JSONObject json) throws JSONException {
		SearchResult result = new SearchResult();

		JSONArray links = json.getJSONArray("link");

		for (int i = 0; i < links.length(); i++) {
			JSONObject link = links.getJSONObject(0);
			if (link.has("type") && link.getString("type").equals("text/html")) {
				result.url = StringEscapeUtils.escapeHtml(link.getString("href"));
				break;
			}
		}

		result.desc = StringEscapeUtils.escapeHtml(json.getString("title"));
		if (result.desc.length() > 300) {
			result.desc = result.desc.substring(0, 300);
		}
		if (json.has("creator")) {
			result.author = StringEscapeUtils.escapeHtml(json.getJSONObject("creator").getString("name"));
		}
		result.created = StringEscapeUtils.escapeHtml(json.getJSONObject("createdDate").getString("date"));
		result.fileType = StringEscapeUtils.escapeHtml(json.getString("type"));
		result.title = StringEscapeUtils.escapeHtml(json.getString("title"));
		result.jiraType = null;
		result.jiraGroup = null;
		result.confluenceSpace = StringEscapeUtils.escapeHtml(json.getJSONObject("space").getString("name"));

		return result;
	}

	private String callRest(HttpRequestBase httpGet) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			System.out.println("executing request " + httpGet.getURI());

			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				public String handleResponse(
						final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			String responseBody = httpclient.execute(httpGet, responseHandler);
//			System.out.println("----------------------------------------");
//			System.out.println(responseBody);
//			System.out.println("----------------------------------------");

			return responseBody;
		} finally {
			httpclient.close();
		}
	}

	@GET
	@Path("/jira")
	@Produces({MediaType.APPLICATION_JSON})
	public Response jira(@QueryParam(value = "q") String query) throws IOException, URISyntaxException, JSONException {

		String jql = "summary ~ " + query + " OR description ~ " + query + " OR comment ~ " + query;

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("jql", jql));
		nvps.add(new BasicNameValuePair("maxResults", Integer.valueOf(maxResults).toString()));

		URI uri = URIUtils.createURI("http", "jira.dev.lithium.com", -1, "/rest/api/2/search",
				URLEncodedUtils.format(nvps, "UTF-8"), null);

		HttpGet httpGet = new HttpGet(uri);

		String responseBody = callRest(httpGet);

		SearchResults results = new SearchResults();
		JSONObject json = new JSONObject(responseBody);

		JSONArray issues = json.getJSONArray("issues");

		for (int i = 0; i < issues.length(); i++) {
			JSONObject issue = issues.getJSONObject(i);
			results.addResult(buildResultFromJira(issue));
		}

		return Response.ok(results).header("Access-Control-Allow-Origin", "*").build();
	}

	public SearchResult buildResultFromJira(JSONObject json) throws JSONException {
		SearchResult result = new SearchResult();
		result.url = json.getString("self");

		JSONObject fields = json.getJSONObject("fields");

		result.desc = StringEscapeUtils.escapeHtml(fields.getString("description"));
		if (result.desc.length() > 300) {
			result.desc = result.desc.substring(0, 300);
		}
		result.author = StringEscapeUtils.escapeHtml(fields.getJSONObject("reporter").getString("name"));
		result.created = StringEscapeUtils.escapeHtml(fields.getString("created"));
		result.fileType = null;
		result.title = StringEscapeUtils.escapeHtml(fields.getString("summary"));
		result.jiraType = StringEscapeUtils.escapeHtml(fields.getJSONObject("issuetype").getString("name"));
		result.jiraGroup = StringEscapeUtils.escapeHtml(fields.getJSONObject("project").getString("name"));
		result.confluenceSpace = null;

		return result;
	}

	@GET
	@Path("/lithosphere")
	@Produces({MediaType.APPLICATION_JSON})
	public Response lithosphere(@QueryParam(value = "q") String query) throws URISyntaxException, IOException, JSONException {

		String token = getLithosphereSessionKey();

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("q", query));
		nvps.add(new BasicNameValuePair("page_size", Integer.valueOf(maxResults).toString()));
		nvps.add(new BasicNameValuePair("page", Integer.valueOf(1).toString()));
		nvps.add(new BasicNameValuePair("restapi.response_style", "view"));
		nvps.add(new BasicNameValuePair("restapi.format_detail", "full_list_element"));
		nvps.add(new BasicNameValuePair("restapi.session_key", token));

		nvps.add(new BasicNameValuePair("restapi.response_format", "json"));

		URI uri = URIUtils.createURI("http", "lithosphere.lithium.com", -1, "/restapi/vc/search/messages",
				URLEncodedUtils.format(nvps, "UTF-8"), null);

		HttpGet httpGet = new HttpGet(uri);

		String responseBody = callRest(httpGet);
		JSONObject json = new JSONObject(responseBody);

		SearchResults results = new SearchResults();

		JSONArray messages = json.getJSONObject("response").getJSONObject("messages").getJSONArray("message");

		for (int i = 0; i < messages.length(); i++) {
			JSONObject message = messages.getJSONObject(i);
			results.addResult(buildResultFromLithosphere(message));
		}
		return Response.ok(results).header("Access-Control-Allow-Origin", "*").build();
	}

	public SearchResult buildResultFromLithosphere(JSONObject json) throws JSONException {
		SearchResult result = new SearchResult();
		result.url = StringEscapeUtils.escapeHtml(json.getString("view_href"));
		result.desc = StringEscapeUtils.escapeHtml(json.getJSONObject("body").getString("$"));
		if (result.desc.length() > 300) {
			result.desc = result.desc.substring(0, 300);
		}
		result.author = StringEscapeUtils.escapeHtml(json.getJSONObject("author").getJSONObject("login").getString("$"));
		result.created = StringEscapeUtils.escapeHtml(json.getJSONObject("post_time").getString("$"));
		result.fileType = StringEscapeUtils.escapeHtml(json.getString("type"));
		result.title = StringEscapeUtils.escapeHtml(json.getJSONObject("subject").getString("$"));
		result.jiraType = null;
		result.jiraGroup = null;
		result.confluenceSpace = null;

		return result;
	}

	private String getLithosphereSessionKey() throws URISyntaxException, IOException, JSONException {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("user.login", "LiB"));
		nvps.add(new BasicNameValuePair("user.password", "xxx"));
		nvps.add(new BasicNameValuePair("restapi.response_format", "json"));

		URI uri = URIUtils.createURI("http", "lithosphere.lithium.com", -1, "/restapi/vc/authentication/sessions/login",
				URLEncodedUtils.format(nvps, "UTF-8"), null);

		HttpPost httpPost = new HttpPost(uri);

		String responseBody = callRest(httpPost);

		JSONObject jsonObject = new JSONObject(responseBody);

		return jsonObject.getJSONObject("response").getJSONObject("value").getString("$");
	}
}
