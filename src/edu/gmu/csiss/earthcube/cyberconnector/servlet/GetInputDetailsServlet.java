package edu.gmu.csiss.earthcube.cyberconnector.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.gmu.csiss.earthcube.cyberconnector.tools.FetchProductListTool;
import edu.gmu.csiss.earthcube.cyberconnector.tools.GetProductInputDetailsTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;

/**
 * Servlet implementation class GetInputDetailsServlet
 */
public class GetInputDetailsServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetInputDetailsServlet() {
        super();
    }
    
	protected void doit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json; charset=utf-8");
		try{
			String action = request.getParameter("action");
			String productid = request.getParameter("productid");
			if(BaseTool.isNull(action)||!action.equals("getinputdetails"))
				throw new RuntimeException("Incorrect input.");
			GetProductInputDetailsTool tool = new GetProductInputDetailsTool();
			out.print(tool.getInputDetailsJSON(productid));
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("Unable to fetch product input details. "+e.getLocalizedMessage());
		}
		out.flush();
		out.close();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
//	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		// TODO Auto-generated method stub
//	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doit(request, response);
	}

}
