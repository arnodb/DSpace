-- add list table

CREATE TABLE list(
  uuid uuid NOT NULL PRIMARY KEY REFERENCES dspaceobject(uuid),
  owner_id uuid REFERENCES eperson(uuid),
  name varchar(64),
  notes text,
  accesstype varchar(1),
  status varchar(1),
  creation_date timestamp
);

CREATE TABLE list2item(
  list_id uuid REFERENCES list(uuid),
  item_id uuid REFERENCES item(uuid)
);

-- drop table list2item;
-- drop table list;
