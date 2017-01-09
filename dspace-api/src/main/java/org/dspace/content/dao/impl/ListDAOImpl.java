/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.dspace.content.ItemList;
import org.dspace.content.dao.ListDAO;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

/**
 * Hibernate implementation of the Database Access Object interface class for the ItemList object.
 * This class is responsible for all database calls for the ItemList object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author delacroix at idmgroup.com
 */
public class ListDAOImpl extends AbstractHibernateDSODAO<ItemList> implements ListDAO {

    protected ListDAOImpl()
    {
        super();
    }

	@Override
	public List<ItemList> findAll(Context context, EPerson eperson) throws SQLException {
		return findAll(context, eperson, null, null);
	}
	
	@Override
	public List<ItemList> findAll(Context context, EPerson eperson, Integer limit, Integer offset) throws SQLException {
        
		Criteria criteria = createCriteria(context, ItemList.class);
        
		criteria.add(Restrictions.eq("owner", eperson));
        
        if(offset != null)
        {
        	criteria.setFirstResult(offset);
        }

        if(limit != null){
        	criteria.setMaxResults(limit);
        }

        return list(criteria);
	}

}
