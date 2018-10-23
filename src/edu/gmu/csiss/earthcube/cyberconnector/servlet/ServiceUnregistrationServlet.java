package edu.gmu.csiss.earthcube.cyberconnector.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.gmu.csiss.earthcube.cyberconnector.services.RegisterServiceTool;

/**
 * Servlet implementation class ServiceUnregistrationServlet
 */
public class ServiceUnregistrationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServiceUnregistrationServlet() {
        super();
    }
    /**
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void doit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String desc = null;
		RegisterServiceTool tool  = new RegisterServiceTool();
		response.setContentType("text/plain; charset=utf-8");
		PrintWriter out = response.getWriter();
		try{
			if((desc=request.getParameter("wsdl"))!=null){
	    		tool.unregisterWSDL(desc);
	    	}else if((desc=request.getParameter("ogc"))!=null){
	    		throw new RuntimeException("OGC Web services are not supported yet.");
	    	}else{
	    		throw new RuntimeException("We don't support such kind of service description right now.");
	    	}
			out.print("Done.");
		}catch(Exception e){
			throw new RuntimeException("Fail. "+e.getLocalizedMessage());
		}
    	out.flush();
    	out.close();
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
//	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		doit(request, response);
//	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doit(request, response);
	}

}
