package org.exoplatform.services.jcr.impl.storage.jdbc.db;

import java.sql.SQLException;
import org.exoplatform.services.jcr.impl.storage.jdbc.JDBCConnectionTestBase;

public class TestSingleDbJDBCConnection extends JDBCConnectionTestBase {

	public SingleDbJDBCConnection jdbcConn = null;

	@Override
	public void setUp() throws Exception {

		super.setUp();
		try {

			st
					.executeUpdate("insert into JCR_SITEM values"
							+ "('12345','123456','Sam',20090525,'myContainer',2,1233,5,10,1)");
			st
					.executeUpdate("insert into JCR_SITEM values"
							+ "('myContainer123','1235','Nick',20090625,'myContainer',1,1233,5,10,1)");
			st.executeUpdate("insert into JCR_SVALUE values"
					+ "(12,'hello',16,'12345','Say')");
			st.executeUpdate("insert into JCR_SVALUE values"
					+ "(127,'love',16,'aa74d2','java')");
			st.executeUpdate("insert into JCR_SREF values"
					+ "('45as1','12345',2)");
			st.executeUpdate("insert into JCR_SREF values"
					+ "('78710','5987',2)");
			jdbcConn = new SingleDbJDBCConnection(connect, "myContainer", null,
					10, null, null);
			tableType = "S";

		} catch (SQLException se) {
			fail(se.toString());
		}
	}

	@Override
	protected void tearDown() throws Exception {

		try {
			st.executeUpdate("delete from JCR_SITEM");
			st.executeUpdate("delete from JCR_SVALUE");
			st.executeUpdate("delete from JCR_SREF");
		} catch (SQLException se) {
			fail(se.toString());
		}
		super.tearDown();
	}

}