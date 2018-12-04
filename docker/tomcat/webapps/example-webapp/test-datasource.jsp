<%@ page language="java" %><%!

private String getResultSet(java.sql.ResultSet resultSet, boolean displayTables) throws Exception {
	StringBuffer sb = new StringBuffer();
	int columnCount = resultSet.getMetaData().getColumnCount();
	for (int i = 0; i < 1000 && resultSet.next(); i++) {
		for (int j = 1; j <= columnCount; j++) {
			if (displayTables) {
				String result = resultSet.getString(1);
				sb.append("<a href=\"?tab=").append(result).append("\">").append(result).append("</a>");
			} else {
				sb.append(resultSet.getString(j));
				if (j < columnCount) {
					sb.append(" | ");
				}
			}
		}
		sb.append("\n");
	}
	return sb.toString();
}

%>
<html><body><pre><%

	String jndiUrl = "java:comp/env/jdbc/example_db";

	String showTables = "show tables"; // For MySQL
	// String showTables = "select from tab"; // For Oracle

	String table = request.getParameter("tab");

	javax.naming.Context ctx = new javax.naming.InitialContext();
	if (ctx == null) {
		out.println("Context is null");

	} else {
		javax.sql.DataSource ds = (javax.sql.DataSource) ctx.lookup(jndiUrl);
		if (ds != null) {
			java.sql.Connection conn = ds.getConnection();

			if (conn != null) {
				java.sql.Statement stmt = conn.createStatement();

				boolean displayTables = (table == null);
				java.sql.ResultSet rst = stmt.executeQuery(displayTables ? showTables : "select * from " + table);
				out.println(getResultSet(rst, displayTables));
				conn.close();
			}
		}
	}
%></pre></body></html>