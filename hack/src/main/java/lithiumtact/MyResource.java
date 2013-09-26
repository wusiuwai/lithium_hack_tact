
package lithiumtact;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
	public String confluence(@QueryParam(value = "q") String query) {
		/**
		 * confluence serach url: http://confluence.dev.lithium.com/rest/prototype/1/search?query=<query>
		 */
		// todo: search confluence
		return "{results:[],message:'confluence'}";
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
