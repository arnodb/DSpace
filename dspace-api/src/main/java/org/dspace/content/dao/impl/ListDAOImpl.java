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
