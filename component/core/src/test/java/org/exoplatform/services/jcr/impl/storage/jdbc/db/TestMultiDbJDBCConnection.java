package org.exoplatform.services.jcr.impl.storage.jdbc.db;

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
			tableType = "M";
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
}