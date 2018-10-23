package edu.gmu.csiss.earthcube.cyberconnector.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.tools.CheckOrderStatusTool;
import edu.gmu.csiss.earthcube.cyberconnector.tools.PlaceOrderTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SpatialExtentValidator;
import edu.gmu.csiss.earthcube.cyberconnector.utils.TimeExtentValidator;

/**
 * Servlet implementation class OrderCheckingServlet
 */
public class OrderCheckingServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	Logger logger = Logger.getLogger(this.getClass());
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OrderCheckingServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		
		response.setContentType("text/plain; charset=utf-8");
		
		try{
			
			logger.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			
			logger.debug("New order checking request arrives.");
			
			String ordernumber = request.getParameter("ordernumber").trim();
			
			if(ordernumber==null||ordernumber.length()!=18){
			
				throw new RuntimeException("Invalid ordernumber.");
			
			}
			
			CheckOrderStatusTool tool = new CheckOrderStatusTool(ordernumber);
			
			out.println(tool.check());
			
		}catch(Exception e){
			
			//e.printStackTrace();
			
			logger.error(e.getLocalizedMessage());
			
			out.println("Failure. "+ e.getLocalizedMessage());
			
		}
		
        out.flush();
        
        out.close();

		logger.debug("A order checking request is processed.\n^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
	}

}
