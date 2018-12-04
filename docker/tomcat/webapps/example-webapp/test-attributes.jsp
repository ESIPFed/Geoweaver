<%@ page language="java" %><%
if (request.getParameter("output") != null) {
	out.println("<html><body><pre>");
	printAttributes(pageContext);
	out.println("</pre></body></html>");
}
%><%!
public void printHiddenAttributes(PageContext page) throws java.io.IOException {
	JspWriter out = page.getOut();
	out.println("<!--");
	out.println(getAttributes(page));
	out.println("-->");
}

public void printAttributes(PageContext page) throws java.io.IOException {
	JspWriter out = page.getOut();
	out.println(getAttributes(page));
}

public String getAttributes(PageContext page) throws java.io.IOException {
	StringBuffer sb = new StringBuffer();
	HttpServletRequest request = (HttpServletRequest) page.getRequest();

	sb.append("Request Parameters\n******************\n");
	for (java.util.Enumeration<java.lang.String> names = request.getParameterNames(); names.hasMoreElements(); ) {
		String name = names.nextElement();
		String value = request.getParameter(name);
		sb.append(getString(name, value));
	}

	sb.append("\n\nRequest Headers\n***************\n");
	for (java.util.Enumeration<java.lang.String> names = request.getHeaderNames(); names.hasMoreElements(); ) {
		String name = names.nextElement();
		String value = request.getHeader(name);
		sb.append(getString(name, value));
	}

	sb.append("\n\nRequest Cookies\n***************\n");
	for (Cookie cookie : request.getCookies()) {
		sb.append("Path    : '" + cookie.getPath() + "'\n");
		sb.append("Name    : '" + cookie.getName() + "'\n");
		sb.append("Value   : '" + cookie.getValue() + "'\n");
		sb.append("MaxAge  : '" + cookie.getMaxAge() + "'\n");
		sb.append("Domain  : '" + cookie.getDomain() + "'\n");
		sb.append("Comment : '" + cookie.getComment() + "'\n");
		sb.append("\n");
	}

	sb.append("\n\nPage Attributes\n***************\n");
	sb.append(getAttributes(page, PageContext.PAGE_SCOPE));

	sb.append("\n\nRequest Attributes\n******************\n");
	sb.append(getAttributes(page, PageContext.REQUEST_SCOPE));

	sb.append("\n\nSession Attributes\n******************\n");
	sb.append(getAttributes(page, PageContext.SESSION_SCOPE));

	sb.append("\n\nApplication Attributes\n**********************\n");
	sb.append(getAttributes(page, PageContext.APPLICATION_SCOPE));

	sb.append("\n\nApplication Attributes\n**********************\n");
	sb.append(getAttributes(page, PageContext.APPLICATION_SCOPE));

	sb.append("\n\nSystem Properties\n**********************\n");
	sb.append(getProperties(System.getProperties()));

	return sb.toString();
}

public String getAttributes(PageContext page, int scope) throws java.io.IOException {
	StringBuffer sb = new StringBuffer();
	for (java.util.Enumeration<java.lang.String> names = page.getAttributeNamesInScope(scope); names.hasMoreElements(); ) {
		String name = names.nextElement();
		Object value = page.getAttribute(name, scope);
		sb.append(getString(name, value));
	}
	return sb.toString();
}

public String getProperties(java.util.Properties properties) throws java.io.IOException {
	StringBuffer sb = new StringBuffer();
	for (java.util.Enumeration<?> names = properties.propertyNames(); names.hasMoreElements(); ) {
		String name = names.nextElement().toString();
		Object value = properties.getProperty(name);
		sb.append(getString(name, value));
	}
	return sb.toString();
}

public String getString(String name, Object value) {
	StringBuffer sb = new StringBuffer();
	sb.append("Name  : '").append(name).append("'");
	if (value != null) {
		sb.append("\nClass : ").append(value.getClass().getName());
		sb.append("\nString: '").append(String.valueOf(value)).append("'");

	} else {
		sb.append(" (null)");
	}
	return sb.append("\n\n").toString();
}
%>