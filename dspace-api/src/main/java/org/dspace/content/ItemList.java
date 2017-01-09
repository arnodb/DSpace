package org.dspace.content;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.dspace.core.Constants;
import org.dspace.eperson.EPerson;

@SuppressWarnings("serial")
@Entity
@Table(name="list")
public class ItemList extends DSpaceObject {

    @SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ItemList.class);
    
    @Column(name = "name")
    private String name;

    @Column(name = "notes")
    @Lob
    private String notes;

    @Column(name = "accesstype", length = 1)
    @Enumerated(EnumType.STRING)
    private AccessType accesstype = AccessType.P;
    
	public enum AccessType {
		/** PRIVATE */
		P,
		/** SHARED */
		S,
		/** ADMIN */
        A;
	}
    
    @Column(name = "status", length = 1)
    @Enumerated(EnumType.STRING)
    private Status status = Status.A;

	public enum Status {
		/** ACTIVE */
		A,
		/** INACTIVE */
		I;
	}

    @Column(name = "creation_date")
    private Date creationDate = new Date();
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private EPerson owner;

    /** The bundles in this item - kept in sync with DB */
    @ManyToMany(fetch = FetchType.LAZY, cascade={CascadeType.PERSIST})
    @JoinTable(
            name = "list2item",
            joinColumns = {@JoinColumn(name = "list_id") },
            inverseJoinColumns = {@JoinColumn(name = "item_id") }
    )
    private final List<Item> items = new ArrayList<>();

    @Override
	public int getType() {
        return Constants.LIST;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Item> getItems() {
		return items;
	}
	
	public EPerson getOwner() {
		return owner;
	}
	
	public void setOwner(EPerson owner) {
		this.owner = owner;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public AccessType getAccessType() {
		return accesstype;
	}
	
	public void setAccessType(AccessType accesstype) {
		this.accesstype = accesstype;
	}
	
}
