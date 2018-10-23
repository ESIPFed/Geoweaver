package edu.gmu.csiss.earthcube.cyberconnector.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.gmu.csiss.earthcube.cyberconnector.tools.FetchProductListTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;

/**
 * Servlet implementation class FetchProductListServlet
 */
public class FetchProductListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FetchProductListServlet() {
        super();
    }
    
    protected void doit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	PrintWriter out = response.getWriter();
		response.setContentType("application/json; charset=utf-8");
		try{
			String action = request.getParameter("action");
			if(BaseTool.isNull(action)||!action.equals("getplist"))
				throw new RuntimeException("Incorrect input.");
			FetchProductListTool tool = new FetchProductListTool();
			out.print(tool.getProductListJSON());
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("Unable to fetch product list. "+e.getLocalizedMessage());
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
