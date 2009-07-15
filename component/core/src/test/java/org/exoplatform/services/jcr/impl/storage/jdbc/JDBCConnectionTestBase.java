package org.exoplatform.services.jcr.impl.storage.jdbc;

import java.sql.*;
import java.util.Properties;

import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import junit.framework.TestCase;

abstract public class JDBCConnectionTestBase extends TestCase {
	final String URL = "jdbc:hsqldb:file:../temp/data/portal";
	final String USER = "sa";
	final String PASSWORD = "";
	public Connection connect = null;
	public Statement st;
	public ResultSet rs = null;
	protected JDBCStorageConnection jdbcConn = null;

	public void setUp() throws Exception {

		DriverManager.registerDriver((Driver) Class.forName(
				"org.hsqldb.jdbcDriver").newInstance());
		connect = DriverManager.getConnection(URL, USER, PASSWORD);
		st = connect.createStatement();

	}

	protected void tearDown() throws Exception {

		st.close();
		connect.close();
		// super.tearDown();
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

	public void testRenameNode() throws Exception {

		jdbcConn.renameNode(giveNode());
		assertEquals(renameNode("JCR_SITEM"), rs.getString("PARENT_ID"));
	}

	protected int addNode(String table) throws Exception {

		makeFindDB("select * from " + table + " where N_ORDER_NUM=8");
		return 8;
	}

	protected String addValueData(String table) throws Exception {

		makeFindDB("select * from " + table + " where PROPERTY_ID='45'");
		return "45";
	}

	protected String renameNode(String table) throws Exception {

		makeFindDB("select * from " + table + " where ID='myContainer123'");
		return "myContainer4512";
	}

	protected int updateNodeByIdentifier(String table) throws Exception {

		makeFindDB("select * from " + table + " where ID='12345'");
		return 20;
	}

	protected int updatePropertyByIdentifier(String table) throws Exception {

		makeFindDB("select * from " + table + " where ID='12345'");
		return 4512;
	}

	protected boolean deleteReference(String table) throws Exception {

		checkDeleted("select * from " + table + " where PROPERTY_ID='5987'");
		return false;
	}

	protected boolean deleteItemByIdentifier(String table) throws Exception {

		checkDeleted("select * from " + table
				+ " where NODE_ID='myContainer123'");
		return false;
	}

	protected boolean deleteValueData(String table) throws Exception {

		checkDeleted("select * from " + table + " where PROPERTY_ID='12345'");
		return false;
	}

	protected String findItemByIdentifier(String table, ResultSet rsRemote)
			throws Exception {

		makeFindDB("select * from " + table + " where ID='12345'");
		return rsRemote.getString("ID");
	}

	protected String findPropertyByName(String table1, String table2,
			ResultSet rsRemote) throws Exception {

		makeFindDB("select V.DATA"
				+ " from "
				+ table1
				+ " I, "
				+ table2
				+ " V"
				+ " where I.I_CLASS=2 and I.PARENT_ID='123456' and I.NAME='Sam' and I.ID=V.PROPERTY_ID order by V.ORDER_NUM");
		return rsRemote.getString("DATA");
	}

	protected int findItemByName(String table, ResultSet rsRemote)
			throws Exception {

		makeFindDB("select * from "
				+ table
				+ " where PARENT_ID='123456' and NAME='Sam' and I_INDEX=1233 order by I_CLASS, VERSION DESC");
		return rsRemote.getInt("I_INDEX");
	}

	protected String findChildNodesByParentIdentifier(String table,
			ResultSet rsRemote) throws Exception {

		makeFindDB("select * from " + table
				+ " where I_CLASS=1 and PARENT_ID='1235'");
		return rsRemote.getString("PARENT_ID");
	}

	protected String findChildPropertiesByParentIdentifier(String table,
			ResultSet rsRemote) throws Exception {

		makeFindDB("select * from " + table
				+ " where I_CLASS=2 and PARENT_ID='123456'" + " order by ID");
		return rsRemote.getString("PARENT_ID");
	}

	protected String findReferences(String table1, String table2,
			ResultSet rsRemote) throws Exception {

		makeFindDB("select P.ID, P.PARENT_ID, P.VERSION, P.P_TYPE, P.P_MULTIVALUED, P.NAME"
				+ " from "
				+ table1
				+ " R, "
				+ table2
				+ " P"
				+ " where R.NODE_ID='45as1' and P.ID=R.PROPERTY_ID and P.I_CLASS=2");
		return rsRemote.getString("ID");
	}

	protected String findValuesByPropertyId(String table, ResultSet rsRemote)
			throws Exception {

		makeFindDB("select PROPERTY_ID, ORDER_NUM, STORAGE_DESC from " + table
				+ " where PROPERTY_ID='12345' order by ORDER_NUM");
		return rsRemote.getString("PROPERTY_ID");
	}

	protected String findValueByPropertyIdOrderNumber(String table,
			ResultSet rsRemote) throws Exception {

		makeFindDB("select DATA from " + table
				+ " where PROPERTY_ID='12345' and ORDER_NUM=16");
		return rsRemote.getString("DATA");
	}
}