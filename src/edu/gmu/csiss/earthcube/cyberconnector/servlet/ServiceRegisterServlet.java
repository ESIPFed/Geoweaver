package edu.gmu.csiss.earthcube.cyberconnector.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.gmu.csiss.earthcube.cyberconnector.services.RegisterServiceTool;
import edu.gmu.csiss.earthcube.cyberconnector.user.UserTool;

/**
 * Servlet implementation class ServiceRegisterServlet
 */
public class ServiceRegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServiceRegisterServlet() {
        super();
    }
    /**
     * Register a service
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
			
			String username = UserTool.checkLogin(request);
				
			if((desc=request.getParameter("wsdl"))!=null){
				
	    		tool.registerWSDL(desc, username);
	    	
			}else if((desc=request.getParameter("ogc"))!=null){
	    	
				throw new RuntimeException("OGC Web services are not supported yet.");
	    	
			}else{
	    	
				throw new RuntimeException("We don't support such kind of service description right now.");
	    	
			}
			
			out.print("Done");
			
		}catch(Exception e){
			
			e.printStackTrace();
			
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
