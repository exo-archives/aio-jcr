package org.exoplatform.services.jcr.impl.storage.jdbc.db;

import java.io.ByteArrayInputStream;
import java.sql.*;

import org.exoplatform.services.jcr.impl.storage.jdbc.JDBCConnectionTestBase;

public class TestMultiDbJDBCConnection extends JDBCConnectionTestBase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		try {

			st.executeUpdate("insert into JCR_MITEM values"
					+ "('12345','123456','Sam',20090525,2,1233,5,10,1)");
			st
					.executeUpdate("insert into JCR_MITEM values"
							+ "('myContainer123','myContainer4512','Nick',20090625,1,1233,5,10,1)");
			st.executeUpdate("insert into JCR_MITEM values"
					+ "('124','1235','Intel',20090825,1,1233,5,10,1)");
			st.executeUpdate("insert into JCR_MVALUE values"
					+ "(12,'hello',16,'12345','Say')");
			st.executeUpdate("insert into JCR_MVALUE values"
					+ "(127,'love',16,'aa74d2','java')");
			st.executeUpdate("insert into JCR_MREF values"
					+ "('45as1','12345',2)");
			st.executeUpdate("insert into JCR_MREF values"
					+ "('78710','5987',2)");
			jdbcConn = new MultiDbJDBCConnection(connect, "mycontainer", null,
					10, null, null);
		} catch (SQLException se) {
			fail(se.toString());
		}
	}

	@Override
	protected void tearDown() throws Exception {

		try {

			st.executeUpdate("delete from JCR_MITEM");
			st.executeUpdate("delete from JCR_MVALUE");
			st.executeUpdate("delete from JCR_MREF");

		} catch (SQLException se) {
			fail(se.toString());
		}
		super.tearDown();
	}

//	public void testAddNode() throws Exception {
//
//		jdbcConn.addNodeRecord(setNode());
//		assertEquals(addNode("JCR_MITEM"), rs.getInt("N_ORDER_NUM"));
//	}
//
//	public void testAddValueData() throws Exception {
//
//		byte data[] = { Byte.parseByte("2") };
//		ByteArrayInputStream bas = new ByteArrayInputStream(data);
//		jdbcConn.addValueData("45", 2, bas, 13, "000");
//		assertEquals(addValueData("JCR_MVALUE"), rs.getString("PROPERTY_ID"));
//	}
//
//	public void testRenameNode() throws Exception {
//
//		jdbcConn.renameNode(setNode());
//		assertEquals(renameNode("JCR_MITEM"), rs.getString("PARENT_ID"));
//	}
//
//	public void testUpdateNodeByIdentifier() throws Exception {
//
//		jdbcConn.updateNodeByIdentifier(200923, 4512, 20, "12345");
//		assertEquals(updateNodeByIdentifier("JCR_MITEM"), rs
//				.getInt("N_ORDER_NUM"));
//	}
//
//	public void testUpdatePropertyByIdentifier() throws Exception {
//
//		jdbcConn.updatePropertyByIdentifier(200923, 4512, "12345");
//		assertEquals(updatePropertyByIdentifier("JCR_MITEM"), rs
//				.getInt("P_TYPE"));
//	}
//
//	public void testDeleteReference() throws Exception {
//
//		jdbcConn.deleteReference("5987");
//		assertEquals(deleteReference("JCR_MREF"), rs.next());
//	}
//
//	public void testDeleteItemByIdentifier() throws Exception {
//
//		jdbcConn.deleteItemByIdentifier("myContainer123");
//		assertEquals(deleteItemByIdentifier("JCR_MREF"), rs.next());
//	}
//
//	public void testDeleteValueData() throws Exception {
//
//		jdbcConn.deleteValueData("12345");
//		assertEquals(deleteValueData("JCR_MVALUE"), rs.next());
//	}
//
//	public void testFindItemByIdentifier() throws Exception {
//		ResultSet rsRemote = jdbcConn.findItemByIdentifier("12345");
//		rsRemote.next();
//		assertEquals(findItemByIdentifier("JCR_MITEM", rsRemote), rs
//				.getString("ID"));
//	}
//
//	public void testFindPropertyByName() throws Exception {
//		ResultSet rsRemote = jdbcConn.findPropertyByName("123456", "Sam");
//		rsRemote.next();
//		assertEquals(findPropertyByName("JCR_MITEM", "JCR_MVALUE", rsRemote),
//				rs.getString("DATA"));
//	}
//
//	public void testFindItemByName() throws Exception {
//		ResultSet rsRemote = jdbcConn.findItemByName("123456", "Sam", 1233);
//		rsRemote.next();
//		assertTrue(findItemByName("JCR_MITEM", rsRemote) == rs
//				.getInt("I_INDEX"));
//	}
//
//	public void testFindChildNodesByParentIdentifier() throws Exception {
//
//		ResultSet rsRemote = jdbcConn.findChildNodesByParentIdentifier("1235");
//		rsRemote.next();
//		assertEquals(findChildNodesByParentIdentifier("JCR_MITEM", rsRemote),
//				rs.getString("PARENT_ID"));
//	}
//
//	public void testFindChildPropertiesByParentIdentifier() throws Exception {
//		ResultSet rsRemote = jdbcConn
//				.findChildPropertiesByParentIdentifier("123456");
//		rsRemote.next();
//		assertEquals(findChildPropertiesByParentIdentifier("JCR_MITEM",
//				rsRemote), rs.getString("PARENT_ID"));
//	}
//
//	public void testFindReferences() throws Exception {
//
//		ResultSet rsRemote = jdbcConn.findReferences("45as1");
//		rsRemote.next();
//		assertEquals(findReferences("JCR_MREF", "JCR_MITEM", rsRemote), rs
//				.getString("ID"));
//	}
//
//	public void testFindValuesByPropertyId() throws Exception {
//
//		ResultSet rsRemote = jdbcConn.findValuesByPropertyId("12345");
//		rsRemote.next();
//		assertEquals(findValuesByPropertyId("JCR_MVALUE", rsRemote), rs
//				.getString("PROPERTY_ID"));
//	}
//
//	public void testFindValueByPropertyIdOrderNumber() throws Exception {
//
//		ResultSet rsRemote = jdbcConn.findValueByPropertyIdOrderNumber("12345",
//				16);
//		rsRemote.next();
//		assertEquals(findValueByPropertyIdOrderNumber("JCR_MVALUE", rsRemote),
//				rs.getString("DATA"));
//	}
}