/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import java.sql.SQLException;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.ItemList;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

/**
 * Service interface class for the ItemList object.
 * The implementation of this class is responsible for all business logic calls for the ItemList object and is autowired by spring
 *
 * @author delacroix at idmgroup.com
 */
public interface ListService extends DSpaceObjectService<ItemList> {

	public ItemList create(Context context, EPerson ePerson) throws SQLException, AuthorizeException;
	
	public List<ItemList> findAll(Context context, EPerson ePerson) throws SQLException;

}
