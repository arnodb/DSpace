/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.ListService;
import org.dspace.rest.common.Item;
import org.dspace.rest.common.List;
import org.dspace.rest.exceptions.ContextException;
import org.dspace.usage.UsageEvent;

/**
 * Class which provide all CRUD methods over item lists.
 * 
 * @author delacroix at idmgroup.com
 * 
 */
@Path("/lists")
public class ListsResource extends Resource {

	private static Logger log = Logger.getLogger(ListsResource.class);
	protected ListService listService = ContentServiceFactory.getInstance().getListService();
	protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();
	protected AuthorizeService authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();

	private org.dspace.content.ItemList findList(org.dspace.core.Context context, String listId, int action)
			throws SQLException {
		org.dspace.content.ItemList list = listService.find(context, UUID.fromString(listId));
		if (list == null) {
			context.abort();
			log.warn("List (id=" + listId + ") was not found!");
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		if (!authorizeService.authorizeActionBoolean(context, list, action)) {
			context.abort();
			if (context.getCurrentUser() != null) {
				log.error("User(" + context.getCurrentUser().getEmail() + ") has not permission to read list");
			} else {
				log.error("User(anonymous) has not permission to read list");
			}
			throw new WebApplicationException(Response.Status.UNAUTHORIZED);
		}
		return list;
	}

	@GET
	@Path("/{list_id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List getList(@PathParam("list_id") String listId, @QueryParam("expand") String expand,
			@QueryParam("userIP") String user_ip, @QueryParam("userAgent") String user_agent,
			@QueryParam("xforwardedfor") String xforwardedfor, @Context HttpHeaders headers,
			@Context HttpServletRequest request) throws WebApplicationException {

		log.info("Reading list(id=" + listId + ").");
		org.dspace.core.Context context = null;
		List list = null;

		try {
			context = createContext();
			org.dspace.content.ItemList dspaceList = findList(context, listId, org.dspace.core.Constants.READ);
			writeStats(dspaceList, UsageEvent.Action.VIEW, user_ip, user_agent, xforwardedfor, headers, request,
					context);

			list = new List(dspaceList, servletContext, expand, context, 100, 0);
			context.complete();
		} catch (SQLException e) {
			processException("Could not read list(id=" + listId + "), SQLException. Message:" + e, context);
		} catch (ContextException e) {
			processException("Could not read list(id=" + listId + "), ContextException. Message:" + e.getMessage(), context);
		} catch (Exception e) {
			log.warn("Could not read list(id=" + listId + "), Exception. Message:" + e.getMessage());
			throw e;
		} finally {
			processFinally(context);
		}

		log.trace("list(id=" + list + ") was successfully read.");

		return list;
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List createList(List list, @QueryParam("userIP") String user_ip,
			@QueryParam("userAgent") String user_agent, @QueryParam("xforwardedfor") String xforwardedfor,
			@Context HttpHeaders headers, @Context HttpServletRequest request) throws WebApplicationException {

		log.info("Creating list");
		org.dspace.core.Context context = null;
		List retList = null;

		try {
			context = createContext();

			if(context.getCurrentUser() == null) {
				throw new WebApplicationException(Response.Status.UNAUTHORIZED);
			}
			
			org.dspace.content.ItemList dspaceList = listService.create(context, context.getCurrentUser());
			if (list != null) {
				dspaceList.setName(list.getName());
				if (list.getItems() != null) {
					java.util.List<UUID> itemUuids = new ArrayList<UUID>(list.getItems().size());
					for (Item item : list.getItems()) {
						itemUuids.add(UUID.fromString(item.getUUID()));
					}
					
					Iterator<org.dspace.content.Item> iterator = itemService.findByIds(context, itemUuids);
					while (iterator.hasNext()) {
						dspaceList.getItems().add(iterator.next());
					}
				}
			}
			listService.update(context, dspaceList);
			writeStats(dspaceList, UsageEvent.Action.CREATE, user_ip, user_agent, xforwardedfor, headers, request,
					context);

			retList = new List(dspaceList, servletContext);
			context.complete();
		} catch (SQLException e) {
			processException("Could not create new list, SQLException. Message: " + e.getMessage(), context);
		} catch (ContextException e) {
			processException("Could not create new list, ContextException. Message: " + e.getMessage(), context);
		} catch (AuthorizeException e) {
			processException("Could not create new list, AuthorizeException Message:" + e, context);
		} catch (Exception e) {
			log.warn("Could not create new list, Exception. Message:" + e.getMessage());
			throw e;
		} finally {
			processFinally(context);
		}

		return retList;
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List[] getLists(@QueryParam("expand") String expand,
			@QueryParam("limit") @DefaultValue("100") Integer limit,
			@QueryParam("offset") @DefaultValue("0") Integer offset, @QueryParam("userIP") String user_ip,
			@QueryParam("userAgent") String user_agent, @QueryParam("xforwardedfor") String xforwardedfor,
			@Context HttpHeaders headers, @Context HttpServletRequest request) throws WebApplicationException {

		log.info("Reading all lists (offset=" + offset + ", limit=" + limit + ").");
		org.dspace.core.Context context = null;
		ArrayList<List> lists = new ArrayList<List>();

		if (!((limit != null) && (limit >= 0) && (offset != null) && (offset >= 0))) {
			log.warn("Paging was badly set.");
			limit = 100;
			offset = 0;
		}

		try {
			context = createContext();

			if(context.getCurrentUser() == null) {
				throw new WebApplicationException(Response.Status.UNAUTHORIZED);
			}
			
			java.util.List<org.dspace.content.ItemList> dspaceLists = listService.findAll(context,
					context.getCurrentUser());

			for (org.dspace.content.ItemList dspaceList : dspaceLists) {
				lists.add(new org.dspace.rest.common.List(dspaceList, servletContext, expand, context, limit, offset));
				writeStats(dspaceList, UsageEvent.Action.VIEW, user_ip, user_agent, xforwardedfor, headers, request,
						context);
			}

			context.complete();

		} catch (SQLException e) {
			processException("Could not read lists, SQLException. Message:" + e.getMessage(), context);
		} catch (ContextException e) {
			processException("Could not read lists, ContextException. Message:" + e.getMessage(), context);
		} catch (Exception e) {
			log.warn("Could not read lists, Exception. Message:" + e.getMessage());
			throw e;
		} finally {
			processFinally(context);
		}

		log.trace("All lists successfully read.");
		return lists.toArray(new List[0]);
	}

	@PUT
	@Path("/{list_id}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateList(@PathParam("list_id") String listId, List list,
			@QueryParam("userIP") String user_ip, @QueryParam("userAgent") String user_agent,
			@QueryParam("xforwardedfor") String xforwardedfor, @Context HttpHeaders headers,
			@Context HttpServletRequest request) throws WebApplicationException {

		log.info("Updating list(id=" + listId + ").");
		org.dspace.core.Context context = null;

		try {
			context = createContext();

			org.dspace.content.ItemList dspaceList = findList(context, listId, org.dspace.core.Constants.WRITE);
			writeStats(dspaceList, UsageEvent.Action.UPDATE, user_ip, user_agent, xforwardedfor, headers, request,
					context);
			if (list.getName() != null) {
				dspaceList.setName(list.getName());
			}
			if (list.getAccesstype() != null) {
				dspaceList.setAccessType(org.dspace.content.ItemList.AccessType.valueOf(list.getAccesstype()));
			}
			if (list.getStatus() != null) {
				dspaceList.setStatus(org.dspace.content.ItemList.Status.valueOf(list.getStatus()));
			}
			if (list.getNotes() != null) {
				dspaceList.setNotes(list.getNotes());
			}
			listService.update(context, dspaceList);
			context.complete();
		} catch (SQLException e) {
			processException("Could not update list(id=" + listId + "), SQLException. Message:" + e, context);
		} catch (ContextException e) {
			processException("Could not update list(id=" + listId + "), ContextException Message:" + e, context);
		} catch (AuthorizeException e) {
			processException("Could not update list(id=" + listId + "), AuthorizeException Message:" + e, context);
		} catch (Exception e) {
			log.warn("Could not update list(id=" + listId + "), Exception. Message:" + e.getMessage());
			throw e;
		} finally {
			processFinally(context);
		}

		log.info("List (id=" + listId + ") has been successfully updated.");
		return Response.ok().build();
	}

	@DELETE
	@Path("/{list_id}")
	public Response deleteList(@PathParam("list_id") String listId, @QueryParam("userIP") String user_ip,
			@QueryParam("userAgent") String user_agent, @QueryParam("xforwardedfor") String xforwardedfor,
			@Context HttpHeaders headers, @Context HttpServletRequest request) throws WebApplicationException {

		log.info("Deleting list(id=" + listId + ").");
		org.dspace.core.Context context = null;

		try {
			context = createContext();

			org.dspace.content.ItemList list = findList(context, listId, org.dspace.core.Constants.DELETE);
			writeStats(list, UsageEvent.Action.DELETE, user_ip, user_agent, xforwardedfor, headers, request, context);

			listService.delete(context, list);
			listService.update(context, list);
			context.complete();

		} catch (SQLException e) {
			processException("Could not delete list(id=" + listId + "), SQLException. Message:" + e, context);
		} catch (AuthorizeException e) {
			processException("Could not delete list(id=" + listId + "), AuthorizeException. Message:" + e, context);
		} catch (IOException e) {
			processException("Could not delete list(id=" + listId + "), IOException. Message:" + e, context);
		} catch (ContextException e) {
			processException("Could not delete list(id=" + listId + "), ContextException. Message:" + e.getMessage(), context);
		} catch (Exception e) {
			log.warn("Could not delete list(id=" + listId + "), Exception. Message:" + e.getMessage());
			throw e;
		} finally {
			processFinally(context);
		}

		log.info("list(id=" + listId + ") was successfully deleted.");
		return Response.status(Response.Status.OK).build();
	}

}
