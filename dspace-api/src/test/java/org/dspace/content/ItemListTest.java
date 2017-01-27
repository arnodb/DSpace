/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ListService;
import org.dspace.core.Constants;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit Tests for class Item
 * @author delacroix at idmgroup.com
 */
public class ItemListTest extends AbstractDSpaceObjectTest {

    /** log4j category */
    private static final Logger log = Logger.getLogger(ItemListTest.class);

    private ListService listService = ContentServiceFactory.getInstance().getListService();

    /**
     * ItemList instance for the tests
     */
    private ItemList list;

    private Community owningCommunity;
    private Collection collection1;
    private Item item1;
    private Collection collection2;
    private Item item2;
	
	private void commitAndReloadEntities() throws SQLException {
        context.commit();
        
        owningCommunity = context.reloadEntity(owningCommunity);
        collection1 = context.reloadEntity(collection1);
        collection2 = context.reloadEntity(collection2);
        item1 = context.reloadEntity(item1);
        item2 = context.reloadEntity(item2);
        list = context.reloadEntity(list);
	}

    /**
     * This method will be run before every test as per @Before. It will
     * initialize resources required for the tests.
     *
     * Other methods can be annotated with @Before here or in subclasses
     * but no execution order is guaranteed
     */
    @Before
    @Override
    public void init()
    {
        super.init();
        try
        {
            //we have to create a new community in the database
            context.turnOffAuthorisationSystem();
            this.owningCommunity = communityService.create(null, context);

            this.collection1 = collectionService.create(context, owningCommunity);
            WorkspaceItem workspaceItem1 = workspaceItemService.create(context, collection1, false);
            this.item1 = installItemService.installItem(context, workspaceItem1);
            itemService.update(context, item1);

            this.collection2 = collectionService.create(context, owningCommunity);
            WorkspaceItem workspaceItem2 = workspaceItemService.create(context, collection2, false);
            this.item2 = installItemService.installItem(context, workspaceItem2);
            itemService.update(context, item2);
            
            list = listService.create(context, eperson);
            
            context.restoreAuthSystemState();

            this.dspaceObject = list;
            
            //we need to commit the changes so we don't block the table for testing
            commitAndReloadEntities();
        }
        catch (AuthorizeException ex)
        {
            log.error("Authorization Error in init", ex);
            fail("Authorization Error in init: " + ex.getMessage());
        }
        catch (SQLException ex)
        {
            log.error("SQL Error in init", ex);
            fail("SQL Error in init: " + ex.getMessage());
        }
    }

    /**
     * This method will be run after every test as per @After. It will
     * clean resources initialized by the @Before methods.
     *
     * Other methods can be annotated with @After here or in subclasses
     * but no execution order is guaranteed
     */
    @After
    @Override
    public void destroy()
    {
        try {
            context.turnOffAuthorisationSystem();
            listService.delete(context, list);
            list = null;
            collectionService.delete(context, collection1);
            collection1 = null;
            collectionService.delete(context, collection2);
            collection2 = null;
            communityService.delete(context, owningCommunity);
            owningCommunity = null;
            context.restoreAuthSystemState();
            super.destroy();
			context.commit();
		} catch (SQLException ex) {
            log.error("SQL Error in destroy", ex);
            fail("SQL Error in destroy: " + ex.getMessage());
		} catch (Exception ex) {
			log.error("Error in destroy", ex);
			fail("Error in destroy: " + ex.getMessage());
		}
    }

    @Override
	public void testGetType() {
        assertThat("testGetType 0", list.getType(), equalTo(Constants.LIST));
	}

	@Override
	public void testGetID() {
        assertTrue("testGetID 0", list.getID() != null);
	}

	@Override
	public void testGetHandle() {
        // no handle for lists
        assertThat("testGetHandle 0", list.getHandle(), nullValue());
	}

	@Override
	public void testGetName() {
		assertThat("testGetName 0", list.getName(), nullValue());
	}
	
    @Test(expected = AuthorizeException.class)
	public void testCreateListWithNoUser() throws SQLException, AuthorizeException {
		listService.create(context, null);
	}

    /**
     * Test of find method
     */
    @Test
    public void testItemListFind() throws Exception
    {
        // Get ID of item created in init()
        UUID id = list.getID();
        
        // Make sure we can find it via its ID
        ItemList found =  listService.find(context, id);
        assertThat("testItemListFind 0", found, notNullValue());
        assertThat("testItemListFind 1", found.getID(), equalTo(id));
        assertThat("testItemListFind 2", found.getName(), nullValue());
    }

    @Test
    public void testAddItems() throws SQLException, AuthorizeException {
        list.getItems().add(item1);
        list.getItems().add(item2);
        
        commitAndReloadEntities();
        
        ItemList found = listService.find(context, list.getID());
        assertEquals("testAddItems 0", found.getItems().size(), 2L);
    }

    @Test
    public void testChangeOwner() throws SQLException, AuthorizeException {
    	final String newName = "new owner";
    	
        context.turnOffAuthorisationSystem();
        EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();
        EPerson newOwner = ePersonService.create(context);
        newOwner.setLastName(context, newName);
        context.restoreAuthSystemState();

        list.setOwner(newOwner);

        commitAndReloadEntities();

        ItemList found = listService.find(context, list.getID());
        assertThat("testChangeOwner 0", found.getOwner().getLastName(), equalTo(newName));
    }

    @Test
    public void testOwner() throws SQLException, AuthorizeException {
        assertEquals("testOwner 0", list.getOwner(), context.getCurrentUser());
    }

    @Test
    public void testName() throws SQLException {
    	final String newName = "new name";
    	list.setName(newName);

        commitAndReloadEntities();

        ItemList found = listService.find(context, list.getID());
        assertEquals("testName 0", found.getName(), newName);
    }

    @Test
    public void testNotes() throws SQLException {
    	final String newNotes = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum eu ipsum lacinia, aliquet sapien in, tincidunt nisi. Donec consectetur feugiat arcu, vitae interdum orci convallis et. Pellentesque mollis gravida urna non lobortis. Etiam a commodo ligula. Integer varius metus aliquam, posuere est nec, volutpat metus. Aliquam malesuada pharetra rutrum. Fusce in leo porttitor, varius velit vitae, ullamcorper ligula. Nullam venenatis, mauris ultrices bibendum blandit, eros orci vehicula dui, sit amet imperdiet ante lectus in nulla. Integer mi orci, scelerisque id erat sed, auctor iaculis tortor. Ut ac mauris pharetra, posuere lacus ac, scelerisque ex. Maecenas mi ex, ornare id purus vestibulum, rhoncus vulputate eros.";
    	list.setNotes(newNotes);

        commitAndReloadEntities();

        ItemList found = listService.find(context, list.getID());
        assertEquals("testNotes 0", found.getNotes(), newNotes);
    }
    
    @Test
    public void testGetCreationDate()
    {
        assertThat("testGetCreationDate 0", list.getCreationDate(), notNullValue());
        assertTrue("testGetCreationDate 1", DateUtils.isSameDay(list.getCreationDate(), new Date()));
    }

    @Test
    public void testStatus() throws SQLException {
    	assertEquals("testStatus 0", list.getStatus(), ItemList.Status.A);
    	
    	list.setStatus(ItemList.Status.I);
        
    	commitAndReloadEntities();

        ItemList found = listService.find(context, list.getID());

        assertEquals("testStatus 1", found.getStatus(), ItemList.Status.I);
    }

    @Test
    public void testAccessType() throws SQLException {
    	assertEquals("testAccessType 0", list.getAccessType(), ItemList.AccessType.P);
    	
    	list.setAccessType(ItemList.AccessType.S);
        
    	commitAndReloadEntities();

        ItemList found = listService.find(context, list.getID());

        assertEquals("testAccessType 1", found.getAccessType(), ItemList.AccessType.S);
    }

    @Test
    public void testDeleteItem() throws SQLException, AuthorizeException, IOException {
    	list.getItems().add(item1);
    	list.getItems().add(item2);
    	
    	commitAndReloadEntities();

        context.turnOffAuthorisationSystem();
    	itemService.delete(context, item1);
        context.restoreAuthSystemState();

    	commitAndReloadEntities();

        ItemList found = listService.find(context, list.getID());
        assertEquals("testDeleteItem 0", 1L, found.getItems().size());
    }

}
