
package lithiumtact;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

// The Java class will be hosted at the URI path "/hack"
@Path("/search")
public class MyResource {

	@GET
	@Produces("text/plain")
	public String getIt() {
		return "Got it!";
	}

	@GET
	@Path("/confluence")
	@Produces({MediaType.APPLICATION_JSON})
	public String confluence(@QueryParam(value = "q") String query) throws IOException {
		HttpGet httpGet = new HttpGet("http://confluence.dev.lithium.com/rest/prototype/1/search?max-results=10&query=" + query);
		String responseBody = callRest(httpGet);
		return responseBody;
	}

	private String callRest(HttpGet httpGet) throws IOException {
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
			System.out.println("----------------------------------------");
			System.out.println(responseBody);
			System.out.println("----------------------------------------");

			return responseBody;
		} finally {
			httpclient.close();
		}
	}

	@GET
	@Path("/jira")
	@Produces({MediaType.APPLICATION_JSON})
	public String jira(@QueryParam(value = "q") String query) {
		System.out.println(query);
		// todo: search jira
		return "{results:[],message:'jira'}";
	}

	@GET
	@Path("/lithosphere")
	@Produces({MediaType.APPLICATION_JSON})
	public String lithosphere(@QueryParam(value = "q") String query) {
		System.out.println(query);
		// todo: search lithosphere
		return "{results:[],message:'lithosphere'}";
	}
}
