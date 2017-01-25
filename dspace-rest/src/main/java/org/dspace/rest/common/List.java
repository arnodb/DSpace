/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.rest.common;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.dspace.core.Context;

/**
 * List object representation in REST API
 * 
 * @author delacroix at idmgroup.com
 * 
 */
@XmlRootElement(name = "list")
public class List extends DSpaceObject {

    @SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(List.class);

	private String notes;
	private String accesstype;
	private String status;
	private Date creationDate;
	private java.util.List<Item> items = new ArrayList<Item>();

    // jersey needs a bare constructor
    public List() {}

    public List(org.dspace.content.ItemList list, ServletContext servletContext) throws WebApplicationException, SQLException {
        super(list, servletContext);
        setup(list, servletContext, null, null, null, null);
    }

    public List(org.dspace.content.ItemList list, ServletContext servletContext, String expand, Context context, Integer limit, Integer offset) throws WebApplicationException, SQLException {
        super(list, servletContext);
        setup(list, servletContext, expand, context, limit, offset);
    }

    public java.util.List<Item> getItems() {
        return items;
    }

    private void setup(org.dspace.content.ItemList list, ServletContext servletContext, String expand, Context context, Integer limit, Integer offset) throws WebApplicationException, SQLException {
    	java.util.List<String> expandFields = new ArrayList<String>();
        if(expand != null) {
            expandFields = Arrays.asList(expand.split(","));
        }

        if(expandFields.contains("items") || expandFields.contains("all")) {
        	for(org.dspace.content.Item item : list.getItems()) {
            	this.items.add(new Item(item, servletContext, null, context));
        	}
        } else {
            this.addExpand("items");
        }
        
        this.notes = list.getNotes();
        this.accesstype = list.getAccessType().toString();
        this.status = list.getStatus().toString();
        this.creationDate = list.getCreationDate();

        if(!expandFields.contains("all")) {
            this.addExpand("all");
        }
    }

    public String getNotes() {
		return notes;
	}

	public String getAccesstype() {
		return accesstype;
	}

	public String getStatus() {
		return status;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setAccesstype(String accesstype) {
		this.accesstype = accesstype;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public void setItems(java.util.List<Item> items) {
		this.items = items;
	}

}
