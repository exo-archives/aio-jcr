CREATE TABLE JCR_CONTAINER(
  VERSION VARCHAR(96) NOT NULL PRIMARY KEY
  );
CREATE TABLE JCR_SITEM(
	ID VARCHAR(96) NOT NULL PRIMARY KEY,
	VERSION INTEGER NOT NULL,
	CONTAINER_NAME VARCHAR(96) NOT NULL,
	PATH VARCHAR(4096) NOT NULL
	);
CREATE UNIQUE INDEX JCR_IDX_SITEM_PATH ON JCR_SITEM(PATH(800), CONTAINER_NAME, ID, VERSION);
CREATE TABLE JCR_SNODE(
	ID VARCHAR(96) NOT NULL PRIMARY KEY,
	ORDER_NUM INTEGER,
	PARENT_ID VARCHAR(96),
	CONSTRAINT JCR_FK_SNODE_PARENT FOREIGN KEY(PARENT_ID) REFERENCES JCR_SNODE(ID), 
	CONSTRAINT JCR_FK_SNODE_ITEM FOREIGN KEY(ID) REFERENCES JCR_SITEM(ID)
	);
CREATE UNIQUE INDEX JCR_IDX_SNODE_PARENTID ON JCR_SNODE(PARENT_ID, ID, ORDER_NUM);
CREATE TABLE JCR_SPROPERTY(
	ID VARCHAR(96) NOT NULL PRIMARY KEY,
	TYPE INTEGER NOT NULL,
	PARENT_ID VARCHAR(96) NOT NULL,
	MULTIVALUED BOOLEAN NOT NULL,
	CONSTRAINT JCR_FK_SPROPERTY_NODE FOREIGN KEY(PARENT_ID) REFERENCES JCR_SNODE(ID), 
	CONSTRAINT JCR_FK_SPROPERTY_ITEM FOREIGN KEY(ID) REFERENCES JCR_SITEM(ID) 
	);	
CREATE UNIQUE INDEX JCR_IDX_SPROPERTY_PARENTID ON JCR_SPROPERTY(PARENT_ID, ID);
CREATE TABLE JCR_SVALUE(
	ID SERIAL PRIMARY KEY, 
	DATA LONGBLOB NOT NULL, 
	ORDER_NUM INTEGER,
	PROPERTY_ID VARCHAR(96) NOT NULL,
	CONSTRAINT JCR_FK_SVALUE_PROPERTY FOREIGN KEY(PROPERTY_ID) REFERENCES JCR_SPROPERTY(ID)
	);
CREATE UNIQUE INDEX JCR_IDX_SVALUE_PROPERTY ON JCR_SVALUE(PROPERTY_ID, ORDER_NUM);
CREATE TABLE JCR_SREF(
  NODE_ID VARCHAR(96) NOT NULL, 
  PROPERTY_ID VARCHAR(96) NOT NULL,
  ORDER_NUM INTEGER NOT NULL,
  CONSTRAINT JCR_PK_SREF PRIMARY KEY(NODE_ID, PROPERTY_ID, ORDER_NUM)
);
CREATE UNIQUE INDEX JCR_IDX_SREF_PROPERTY ON JCR_SREF(PROPERTY_ID, ORDER_NUM);