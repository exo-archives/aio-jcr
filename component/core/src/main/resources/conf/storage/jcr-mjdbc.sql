CREATE TABLE JCR_CONTAINER(
  VERSION VARCHAR(96) NOT NULL PRIMARY KEY
  );
CREATE TABLE JCR_MITEM(
	ID VARCHAR(96) NOT NULL PRIMARY KEY,
	NAME VARCHAR(512) NOT NULL,
	VERSION INTEGER NOT NULL, 
	PATH VARCHAR(4096) NOT NULL
	);
CREATE UNIQUE INDEX JCR_IDX_MITEM_PATH ON JCR_MITEM(PATH, ID, VERSION);
CREATE INDEX JCR_IDX_MITEM_NAME ON JCR_MITEM(NAME, ID, VERSION);
CREATE TABLE JCR_MNODE(
	ID VARCHAR(96) NOT NULL PRIMARY KEY,
	PARENT_ID VARCHAR(96),
	NODE_INDEX INTEGER NOT NULL,
	ORDER_NUM INTEGER NOT NULL, 
	CONSTRAINT JCR_FK_MNODE_PARENT FOREIGN KEY(PARENT_ID) REFERENCES JCR_MNODE(ID), 
	CONSTRAINT JCR_FK_MNODE_ITEM FOREIGN KEY(ID) REFERENCES JCR_MITEM(ID)
	);
CREATE UNIQUE INDEX JCR_IDX_MNODE_PARENTID ON JCR_MNODE(PARENT_ID, ID, NODE_INDEX);
CREATE TABLE JCR_MPROPERTY(
	ID VARCHAR(96) NOT NULL PRIMARY KEY,
	PARENT_ID VARCHAR(96) NOT NULL,
	TYPE INTEGER NOT NULL, 
	MULTIVALUED BOOLEAN NOT NULL, 
	CONSTRAINT JCR_FK_MPROPERTY_NODE FOREIGN KEY(PARENT_ID) REFERENCES JCR_MNODE(ID), 
	CONSTRAINT JCR_FK_MPROPERTY_ITEM FOREIGN KEY(ID) REFERENCES JCR_MITEM(ID) 
	);
CREATE UNIQUE INDEX JCR_IDX_MPROPERTY_PARENTID ON JCR_MPROPERTY(PARENT_ID, ID);
CREATE TABLE JCR_MVALUE(
	ID BIGINT generated by default as identity (START WITH 2, INCREMENT BY 1) NOT NULL PRIMARY KEY, 
	DATA VARBINARY(65535) NOT NULL, 
	ORDER_NUM INTEGER, 
	PROPERTY_ID VARCHAR(96) NOT NULL, 
	CONSTRAINT JCR_FK_MVALUE_PROPERTY FOREIGN KEY(PROPERTY_ID) REFERENCES JCR_MPROPERTY(ID)
	);
CREATE UNIQUE INDEX JCR_IDX_MVALUE_PROPERTY ON JCR_MVALUE(PROPERTY_ID, ORDER_NUM);
CREATE TABLE JCR_MREF(
  NODE_ID VARCHAR(96) NOT NULL, 
  PROPERTY_ID VARCHAR(96) NOT NULL,
  ORDER_NUM INTEGER NOT NULL,
  CONSTRAINT JCR_PK_MREF PRIMARY KEY(NODE_ID, PROPERTY_ID, ORDER_NUM)
);
CREATE UNIQUE INDEX JCR_IDX_MREF_PROPERTY ON JCR_MREF(PROPERTY_ID, ORDER_NUM);