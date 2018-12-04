<%@ page language="java" %>
<%@ page import="javax.naming.*, java.sql.Connection, javax.sql.DataSource" %>
<html><body><pre><%

	Context initCtx = new InitialContext();

	Context envCtx = (Context) initCtx.lookup("java:comp/env");
	if (envCtx != null) {

		DataSource ds = (DataSource) envCtx.lookup("jdbc/example_db");
		if (ds != null) {
			out.println("DataSource: " + ds.toString());

			Connection conn = ds.getConnection();
			if (conn != null) {
				out.println("Connection: " + conn.toString());

			} else {
				out.println("Connection is null");
			}

		} else {
			out.println("DataSource is null");
		}

	} else {
		out.println("Context 'java:comp/env' is null");
	}
%></pre></body></html>
