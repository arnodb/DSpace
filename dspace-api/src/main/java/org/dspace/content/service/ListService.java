package org.dspace.content.service;

import java.sql.SQLException;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.ItemList;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

public interface ListService extends DSpaceObjectService<ItemList> {

	public ItemList create(Context context, EPerson ePerson) throws SQLException, AuthorizeException;
	
	public List<ItemList> findAll(Context context, EPerson ePerson) throws SQLException;

}
