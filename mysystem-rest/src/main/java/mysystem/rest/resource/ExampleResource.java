package mysystem.rest.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mysystem.common.model.Company;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * An example Jersey REST resource.
 */
@Path("/example")
public class ExampleResource {
    private final static Logger LOG = LoggerFactory.getLogger(ExampleResource.class);

    @GET
    @Path("{companyId:\\d+")
    @Produces(MediaType.APPLICATION_JSON)
    public Company get(@PathParam("companyId") final Integer companyId) {
        LOG.info("Getting company with id: {}", companyId);

        return new Company.Builder().setId(companyId).setName("Company Name").build();
    }
}
