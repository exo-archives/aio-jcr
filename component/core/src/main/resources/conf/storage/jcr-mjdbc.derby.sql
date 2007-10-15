CREATE TABLE JCR_CONTAINER(
  	VERSION VARCHAR(96) NOT NULL,
	CONSTRAINT JCR_PK_CONTAINER PRIMARY KEY(VERSION)
);
CREATE TABLE JCR_MITEM(
	ID VARCHAR(96) NOT NULL,
	VERSION INTEGER NOT NULL,
	PATH VARCHAR(4096) NOT NULL,
	CONSTRAINT JCR_PK_MITEM PRIMARY KEY(ID)
);
CREATE UNIQUE INDEX JCR_IDX_MITEM_PATH ON JCR_MITEM(PATH, ID, VERSION DESC);
CREATE TABLE JCR_MNODE(
	ID VARCHAR(96) NOT NULL,
	ORDER_NUM INTEGER,
	PARENT_ID VARCHAR(96),
	CONSTRAINT JCR_PK_MNODE PRIMARY KEY(ID),
	CONSTRAINT JCR_FK_MNODE_PARENT FOREIGN KEY(PARENT_ID) REFERENCES JCR_MNODE(ID), 
	CONSTRAINT JCR_FK_MNODE_ITEM FOREIGN KEY(ID) REFERENCES JCR_MITEM(ID)
);
CREATE UNIQUE INDEX JCR_IDX_MNODE_PARENTID ON JCR_MNODE(PARENT_ID, ID, ORDER_NUM);
CREATE TABLE JCR_MPROPERTY(
	ID VARCHAR(96) NOT NULL,
	TYPE INTEGER NOT NULL,
	PARENT_ID VARCHAR(96) NOT NULL,
	MULTIVALUED INTEGER NOT NULL,
	CONSTRAINT JCR_PK_MPROPERTY PRIMARY KEY(ID),
	CONSTRAINT JCR_FK_MPROPERTY_NODE FOREIGN KEY(PARENT_ID) REFERENCES JCR_MNODE(ID), 
	CONSTRAINT JCR_FK_MPROPERTY_ITEM FOREIGN KEY(ID) REFERENCES JCR_MITEM(ID) 
	);
CREATE UNIQUE INDEX JCR_IDX_MPROPERTY_PARENTID ON JCR_MPROPERTY(PARENT_ID, ID);
CREATE TABLE JCR_MVALUE(
	ID BIGINT generated by default as identity (START WITH 2, INCREMENT BY 1) NOT NULL, 
    DATA BLOB(56M) NOT NULL, 
	ORDER_NUM INTEGER,
	PROPERTY_ID VARCHAR(96) NOT NULL,
	CONSTRAINT JCR_PK_MVALUE PRIMARY KEY(ID),
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