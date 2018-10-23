package edu.gmu.csiss.earthcube.cyberconnector.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.tools.PlaceOrderTool;
import edu.gmu.csiss.earthcube.cyberconnector.user.User;
import edu.gmu.csiss.earthcube.cyberconnector.user.UserTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.EmailValidator;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SpatialExtentValidator;
import edu.gmu.csiss.earthcube.cyberconnector.utils.TimeExtentValidator;

/**
 * Servlet implementation class VirtualProductQueryServlet
 * @author Ziheng Sun
 * @date 2015.8.6
 */
public class VirtualProductQueryServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	Logger logger = Logger.getLogger(this.getClass());
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VirtualProductQueryServlet() {
        super();
    }
    
    private void doit(HttpServletRequest request, HttpServletResponse response) throws IOException{
//    	response.setContentType("text/html; charset=utf-8");
    	response.setContentType("text/plain; charset=utf-8");
		PrintWriter out = response.getWriter();
		try{
			
			logger.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			
			logger.debug("New request arrives.");
			
			Map<String, String[]> pmap = request.getParameterMap();
			
			Map<String, String> parametermap = new HashMap();
			
			Iterator it = pmap.entrySet().iterator();
		    
			while (it.hasNext()) {
		    
				Map.Entry pair = (Map.Entry)it.next();
		        
				logger.debug(pair.getKey() + " = " + pair.getValue());
		        
				parametermap.put((String)pair.getKey(), ((String[])pair.getValue())[0]);
		    
			}
			
			String username = (String)request.getSession().getAttribute("sessionUser");
			
			if(username==null){
				
				logger.debug("The session is not logged in. Check if the requested product is for demo use.");
				
				String pid = parametermap.get("productid");
				
				if(pid.equals("urn:uuid:55918540-56ab-1033-a3e4-942e81aea662") //FVCOM river input
						||pid.equals("urn:uuid:7e364d60-581c-1033-877f-55fbc0a80002") //T_S_condition_fvcom_input
						||pid.equals("urn:uuid:23e43e80-5867-1033-acac-bba0c0a80002") //wind_forcing_fvcom_input
						||pid.equals("urn:uuid:beb9d320-7531-1033-ac74-df4e81aea662")) //CRM_array_averaged_analysis_model
				{
					
					logger.debug("This request is for demo purpose. Give permit.");
					
				}else{
					
					throw new RuntimeException("You need log in first.");
					
				}
				
				String mail = request.getParameter("email");
				
				if(!EmailValidator.validate(mail)){
				
					throw new RuntimeException("The email address is invalid.");
				
				}
				
				parametermap.put("userid", "0"); //this order belongs to nobody - just for test purpose
				
			}else{
				
				User u = UserTool.retrieveInformation(username);
				
				parametermap.put("userid", u.getId());
				
				parametermap.put("email", u.getEmail());
				
			}
			
			PlaceOrderTool t = new PlaceOrderTool();
			
			String orderid = t.placeOrder(parametermap);
			
			out.println(orderid);
			
		}catch(Exception e){
			
			//e.printStackTrace();
			
			logger.error(e.getLocalizedMessage());
			
			out.println("Failure. "+ e.getLocalizedMessage());
			
		}
		
        out.flush();
        
        out.close();
		
        logger.debug("A request is processed.\n^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();
        BaseTool tool = new BaseTool();
        String responsefilelocation = tool.getClassPath()+"/edu/gmu/csiss/earthcube/cyberconnector/config/res_template.html";
        String responsepage = tool.readStringFromFile(responsefilelocation);
        out.println(responsepage);
        out.flush();
        out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doit(request, response);
	}
	
}
