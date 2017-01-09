/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.ListDAO;
import org.dspace.content.service.ListService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service implementation for the ItemList object.
 * This class is responsible for all business logic calls for the ItemList object and is autowired by spring.
 * This class should never be accessed directly.
 *
 * @author delacroix at idmgroup.com
 */
public class ListServiceImpl extends DSpaceObjectServiceImpl<ItemList> implements ListService {

    /** log4j category */
    private static final Logger log = Logger.getLogger(ListServiceImpl.class);

    @Autowired(required = true)
    protected ListDAO listDAO;

    @Autowired(required = true)
    protected AuthorizeService authorizeService;

    protected ListServiceImpl()
    {
        super();
    }

	@Override
	public ItemList find(Context context, UUID id) throws SQLException {
		return listDAO.findByID(context, ItemList.class, id);
	}

	@Override
	public void updateLastModified(Context context, ItemList dso) throws SQLException, AuthorizeException {
		// not needed
	}

	@Override
	public void delete(Context context, ItemList dso) throws SQLException, AuthorizeException, IOException {
		log.info("deleting list " + dso.getID());
		authorizeService.authorizeAction(context, dso, Constants.DELETE);
		listDAO.delete(context, dso);
	}

	@Override
	public int getSupportsTypeConstant() {
		return Constants.LIST;
	}

	@Override
	public List<ItemList> findAll(Context context, EPerson eperson) throws SQLException {
		return listDAO.findAll(context, eperson);
	}

	@Override
	public ItemList create(Context context, EPerson eperson) throws SQLException, AuthorizeException {
		if(eperson == null) {
			throw new AuthorizeException("Anonymous lists are not permitted");
		}
		
		ItemList list = new ItemList();
		list.setOwner(eperson);
		return listDAO.create(context, list);
	}

}
