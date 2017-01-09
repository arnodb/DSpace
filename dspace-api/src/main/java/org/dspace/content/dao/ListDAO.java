/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao;

import java.sql.SQLException;
import java.util.List;

import org.dspace.content.ItemList;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

/**
 * Database Access Object interface class for the ItemList object.
 * The implementation of this class is responsible for all database calls for the ItemList object and is autowired by spring
 * This class should only be accessed from a single service and should never be exposed outside of the API
 *
 * @author delacroix at idmgroup.com
 */
public interface ListDAO extends DSpaceObjectDAO<ItemList> {

    public List<ItemList> findAll(Context context, EPerson eperson) throws SQLException;

    public List<ItemList> findAll(Context context, EPerson eperson, Integer limit, Integer offset) throws SQLException;

}
