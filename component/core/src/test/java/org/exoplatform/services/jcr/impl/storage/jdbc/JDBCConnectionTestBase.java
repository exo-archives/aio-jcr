package org.exoplatform.services.jcr.impl.storage.jdbc;

import java.io.ByteArrayInputStream;
import java.sql.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;

abstract public class JDBCConnectionTestBase extends JcrAPIBaseTest {

	public Connection connect = null;
	public Statement st;
	public ResultSet rs = null;
	protected JDBCStorageConnection jdbcConn = null;
	protected String tableType = null;

	public void setUp() throws Exception {

		super.setUp();
		connect = getJNDIConnection();
		st = connect.createStatement();
	}

	protected void tearDown() throws Exception {

		st.close();
		connect.close();
		super.tearDown();
	}

	private Connection getJNDIConnection() throws Exception {

		Context ctx = new InitialContext();
		DataSource ds = (DataSource) ctx.lookup("jdbcexo");
		Connection conn = ds.getConnection();
		return conn;

	}

	public NodeData giveNode() throws Exception {
		InternalQName[] iqn = { InternalQName
				.parse("[]DbJDBCConnectionEditTest") };
		TransientNodeData tnd = new TransientNodeData(
				Constants.JCR_NODETYPES_PATH, "123", 2, Constants.SV_NODE_NAME,
				iqn, 7, "4512", null);
		return tnd;
	}

	public NodeData setNode() throws Exception {
		InternalQName[] iqn = { InternalQName
				.parse("[]DbJDBCConnectionEditTest") };
		TransientNodeData tnd = new TransientNodeData(
				Constants.JCR_NODETYPES_PATH, "245", 9, Constants.SV_NODE_NAME,
				iqn, 8, "7870", null);
		return tnd;
	}

	public void makeFindDB(String sql) throws Exception {

		rs = st.executeQuery(sql);
		rs.next();
	}

	public void checkDeleted(String sql) throws Exception {

		rs = st.executeQuery(sql);
	}

	public void testAddNode() throws Exception {

		jdbcConn.add(setNode());
		makeFindDB("select * from " + "JCR_" + tableType + "ITEM"
				+ " where N_ORDER_NUM=8");
		assertEquals(8, rs.getInt("N_ORDER_NUM"));
	}

	public void testAddValueData() throws Exception {

		byte data[] = { Byte.parseByte("2") };
		ByteArrayInputStream bas = new ByteArrayInputStream(data);
		jdbcConn.addValueData("45", 2, bas, 13, "000");
		makeFindDB("select * from " + "JCR_" + tableType + "VALUE"
				+ " where PROPERTY_ID='45'");
		assertEquals("45", rs.getString("PROPERTY_ID"));
	}

	public void testRenameNode() throws Exception {

		jdbcConn.renameNode(giveNode());
		makeFindDB("select * from " + "JCR_" + tableType + "SITEM"
				+ " where ID='myContainer123'");
		assertEquals("myContainer4512", rs.getString("PARENT_ID"));
	}

	public void testUpdateNodeByIdentifier() throws Exception {

		jdbcConn.updateNodeByIdentifier(200923, 4512, 20, "12345");
		makeFindDB("select * from " + "JCR_" + tableType + "ITEM"
				+ " where ID='12345'");
		assertEquals(20, rs.getInt("N_ORDER_NUM"));
	}

	public void testUpdatePropertyByIdentifier() throws Exception {

		jdbcConn.updatePropertyByIdentifier(200923, 4512, "12345");
		makeFindDB("select * from " + "JCR_" + tableType + "ITEM"
				+ " where ID='12345'");
		assertEquals(4512, rs.getInt("P_TYPE"));
	}

	public void testDeleteReference() throws Exception {

		jdbcConn.deleteReference("5987");
		checkDeleted("select * from " + "JCR_" + tableType + "REF"
				+ " where PROPERTY_ID='5987'");
		assertEquals(false, rs.next());
	}

	public void testDeleteItemByIdentifier() throws Exception {

		jdbcConn.deleteItemByIdentifier("myContainer123");
		checkDeleted("select * from " + "JCR_" + tableType + "REF"
				+ " where NODE_ID='myContainer123'");
		assertEquals(false, rs.next());
	}

	public void testDeleteValueData() throws Exception {

		jdbcConn.deleteValueData("12345");
		checkDeleted("select * from " + "JCR_" + tableType + "VALUE"
				+ " where PROPERTY_ID='12345'");
		assertEquals(false, rs.next());
	}

	public void testFindItemByIdentifier() throws Exception {

		ResultSet rsRemote = jdbcConn.findItemByIdentifier("12345");
		rsRemote.next();
		makeFindDB("select * from " + "JCR_" + tableType + "ITEM"
				+ " where ID='12345'");
		assertEquals(rsRemote.getString("ID"), rs.getString("ID"));
	}

	public void testFindPropertyByName() throws Exception {

		ResultSet rsRemote = jdbcConn.findPropertyByName("123456", "Sam");
		rsRemote.next();
		makeFindDB("select V.DATA"
				+ " from "
				+ "JCR_"
				+ tableType
				+ "ITEM"
				+ " I, "
				+ "JCR_"
				+ tableType
				+ "VALUE"
				+ " V"
				+ " where I.I_CLASS=2 and I.PARENT_ID='123456' and I.NAME='Sam' and I.ID=V.PROPERTY_ID order by V.ORDER_NUM");
		assertEquals(rsRemote.getString("DATA"), rs.getString("DATA"));
	}

	public void testFindItemByName() throws Exception {

		ResultSet rsRemote = jdbcConn.findItemByName("123456", "Sam", 1233);
		rsRemote.next();
		makeFindDB("select * from "
				+ "JCR_"
				+ tableType
				+ "ITEM"
				+ " where PARENT_ID='123456' and NAME='Sam' and I_INDEX=1233 order by I_CLASS, VERSION DESC");
		assertTrue(rsRemote.getInt("I_INDEX") == rs.getInt("I_INDEX"));
	}

	public void testFindChildNodesByParentIdentifier() throws Exception {

		ResultSet rsRemote = jdbcConn.findChildNodesByParentIdentifier("1235");
		rsRemote.next();
		makeFindDB("select * from " + "JCR_" + tableType + "ITEM"
				+ " where I_CLASS=1 and PARENT_ID='1235'");
		assertEquals(rsRemote.getString("PARENT_ID"), rs.getString("PARENT_ID"));
	}

	public void testFindChildPropertiesByParentIdentifier() throws Exception {
		ResultSet rsRemote = jdbcConn
				.findChildPropertiesByParentIdentifier("123456");
		rsRemote.next();
		makeFindDB("select * from " + "JCR_" + tableType + "ITEM"
				+ " where I_CLASS=2 and PARENT_ID='123456'" + " order by ID");
		assertEquals(rsRemote.getString("PARENT_ID"), rs.getString("PARENT_ID"));
	}

	public void testFindReferences() throws Exception {

		ResultSet rsRemote = jdbcConn.findReferences("45as1");
		rsRemote.next();
		makeFindDB("select P.ID, P.PARENT_ID, P.VERSION, P.P_TYPE, P.P_MULTIVALUED, P.NAME"
				+ " from "
				+ "JCR_"
				+ tableType
				+ "REF"
				+ " R, "
				+ "JCR_"
				+ tableType
				+ "ITEM"
				+ " P"
				+ " where R.NODE_ID='45as1' and P.ID=R.PROPERTY_ID and P.I_CLASS=2");
		assertEquals(rsRemote.getString("ID"), rs.getString("ID"));
	}

	public void testFindValuesByPropertyId() throws Exception {

		ResultSet rsRemote = jdbcConn.findValuesByPropertyId("12345");
		rsRemote.next();
		makeFindDB("select PROPERTY_ID, ORDER_NUM, STORAGE_DESC from " + "JCR_"
				+ tableType + "VALUE"
				+ " where PROPERTY_ID='12345' order by ORDER_NUM");
		assertEquals(rsRemote.getString("PROPERTY_ID"), rs
				.getString("PROPERTY_ID"));
	}

	public void testFindValueByPropertyIdOrderNumber() throws Exception {

		ResultSet rsRemote = jdbcConn.findValueByPropertyIdOrderNumber("12345",
				16);
		rsRemote.next();
		makeFindDB("select DATA from " + "JCR_" + tableType + "VALUE"
				+ " where PROPERTY_ID='12345' and ORDER_NUM=16");
		assertEquals(rsRemote.getString("DATA"), rs.getString("DATA"));
	}
}