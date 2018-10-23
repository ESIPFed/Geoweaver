package edu.gmu.csiss.earthcube.cyberconnector.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.gmu.csiss.earthcube.cyberconnector.tools.PlaceOrderTool;
import edu.gmu.csiss.earthcube.cyberconnector.tools.QueryOrderStatusTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;

/**
 * Servlet implementation class MetadataQueryServlet
 * @author Ziheng Sun
 * @date 2015.8.11
 * Aim to support CyberConnector
 */
public class MetadataQueryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
//	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		
//	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain; charset=utf-8");
		try{
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("New metadata querying request arrives.");
			String action = request.getParameter("action");
			QueryOrderStatusTool tool = new QueryOrderStatusTool();
			if("orderstatus".equals(action)){
				String oid = request.getParameter("orderid");
				String status = tool.queryStatusByOrderId(oid);
				out.println(status);
			}else if("virtualproductlist".equals(action)){
				String listjson = tool.queryVirtualProductList();
				out.println(listjson);
			}else{
				throw new RuntimeException("The action '"+action+"' is not supported.");
			}
		}catch(RuntimeException e){
			out.println("Failure. "+ e.getLocalizedMessage());
		}
        out.flush();
        out.close();

		System.out.println("A metadata querying request is processed.\n^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
	}

}
