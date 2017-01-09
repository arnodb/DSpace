package org.dspace.content.dao;

import java.sql.SQLException;
import java.util.List;

import org.dspace.content.ItemList;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

public interface ListDAO extends DSpaceObjectDAO<ItemList> {

    public List<ItemList> findAll(Context context, EPerson eperson) throws SQLException;

    public List<ItemList> findAll(Context context, EPerson eperson, Integer limit, Integer offset) throws SQLException;

}
