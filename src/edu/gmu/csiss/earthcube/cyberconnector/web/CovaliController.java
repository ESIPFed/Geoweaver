package edu.gmu.csiss.earthcube.cyberconnector.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;

import edu.gmu.csiss.earthcube.cyberconnector.ncwms.ncWMSTool;
import edu.gmu.csiss.earthcube.cyberconnector.products.Product;
import edu.gmu.csiss.earthcube.cyberconnector.search.Granule;
import edu.gmu.csiss.earthcube.cyberconnector.search.GranulesRequest;
import edu.gmu.csiss.earthcube.cyberconnector.search.GranulesTool;
import edu.gmu.csiss.earthcube.cyberconnector.search.SearchRequest;
import edu.gmu.csiss.earthcube.cyberconnector.search.SearchResponse;
import edu.gmu.csiss.earthcube.cyberconnector.search.SearchTool;
import edu.gmu.csiss.earthcube.cyberconnector.tools.LocalFileTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.Message;
import edu.gmu.csiss.earthcube.cyberconnector.utils.RandomString;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;

/**
*Class CovaliController.java
*Every bean COVALI used should be put here
*@author Ziheng Sun
*@time Oct 18, 2018 6:02:12 PM
*Original aim is to support COVALI.
*/	
@Controller 
//@SessionAttributes({"sessionUser"})
public class CovaliController {
	
	Logger logger = Logger.getLogger(this.getClass());
	

	@RequestMapping(value = "/search", method = RequestMethod.GET)
    public String productsearch(@ModelAttribute("request") SearchRequest searchreq,  ModelMap model){

    	//int x = 1;
		//model.addAttribute("request", searchreq);

    	return "searchresult";
    }


    @RequestMapping(value = "/searchresult", method = RequestMethod.GET)
    public String displayresultget(@ModelAttribute("resp") SearchResponse searchresp, BindingResult result, ModelMap model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = "message";
    	
    	if(name == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    		
    		Message msg = new Message("search_result_servlet", "browser", "failed", false);
    		
    		msg.setTitle("Page 404");
        	
        	msg.setStrongmsg("Oops! The page doesn't exist.");
        	
        	msg.setDisplaymsg("Sorry for that. Contact our webmaster (zsun@gmu.edu) if any doubts.");
        	
        	model.addAttribute("message", msg);
        	
        	model.addAttribute("forwardURL", "index");
        	
        	model.addAttribute("forward", "redirect to main page");
    		
    	}
    	
    	return resp;
    	
    }
    

    @RequestMapping(value = "/search", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String tableserver(@ModelAttribute("request") SearchRequest searchreq,  WebRequest request) {

		SearchResponse sr = SearchTool.search(searchreq);


    	int start = Integer.parseInt(request.getParameter("start")) + 1;
    	int length = Integer.parseInt(request.getParameter("length"));
    	int pageNum = start/length;
    	
    	searchreq.setPageno(pageNum);

    	int pageno = start/length;
    	
    	searchreq.setPageno(pageno);

    	//for JQuery DataTables
    	String draw = request.getParameter("draw");

    	sr.setDraw(Integer.parseInt(draw));
    	sr.setRecordsFiltered(sr.getProduct_total_number());
    	sr.setRecordsTotal(sr.getProduct_total_number());

		return BaseTool.toJSONString(sr);
    }

	@RequestMapping(value = "/listgranules", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String listgranules_ajax(@ModelAttribute("request") GranulesRequest gRequest, WebRequest webRequest) {
		int start = Integer.parseInt(webRequest.getParameter("start"));
		int length = Integer.parseInt(webRequest.getParameter("length"));


        GranulesTool.indexCollectionGranules(gRequest);

        List<Granule> granules = GranulesTool.getCollectionGranules(gRequest);

		SearchResponse sr = new SearchResponse();

		List products = new ArrayList();
		for(int i = start; i < start + length && i < granules.size() ; i++) {
			Granule g = granules.get(i);
			Product p = g.toProduct(gRequest);

			products.add(p);
		}

		sr.setProducts(products);

		int draw = Integer.parseInt(webRequest.getParameter("draw"));
		sr.setDraw(draw);

		sr.setRecordsFiltered(granules.size());
		sr.setRecordsTotal(granules.size());


		return BaseTool.toJSONString(sr);
    }

	@RequestMapping(value = "/listgranules", method = RequestMethod.GET)
    public String listgranules(@ModelAttribute("request") GranulesRequest request, ModelMap model){

		return "listgranules";
	}

    /**
     * List local files in the shared folder
     * @param model
     * @param request
     * @param status
     * @param session
     * @return
     */
    @RequestMapping(value = "/localfilelist", method = RequestMethod.POST)
    public @ResponseBody String localfilelist(ModelMap model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String resp = null;
    	
//    	String querystr = request.getQueryString();
    	
    	String rootlocation = request.getParameter("root");
    	
    	try {
    		
    		//have some potential threats. Restrict the folder that COVALI can publish later
    		
    		if(rootlocation==null) {
    			
    			resp = LocalFileTool.getLocalFileList("/");
    			
    		}else
    		
    			resp = LocalFileTool.getLocalFileList(rootlocation);
    		
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		
    		resp = "{\"output\":\"failure\",\"reason\": \""+
    				
    				e.getLocalizedMessage() +
    				
    				"\"}";
    				
    	}
    	
    	return resp;
    	
    }
    

    @RequestMapping(value = "/cachecasual", method = RequestMethod.POST)
    public @ResponseBody String cachecasualdata(ModelMap model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String resp = null;
    	
    	String dataurl = request.getParameter("data");
    	
    	String fileurl = BaseTool.cacheDataLocally(dataurl);
    	
    	resp = "{\"output\":\"success\", \"file_url\": \""+fileurl+"\"}";
    	
    	return resp;
    	
    }
    
	
	@RequestMapping(value = "/cache", method = RequestMethod.POST)
    public @ResponseBody String cachedata(ModelMap model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String resp = null;
    	
    	String dataurl = request.getParameter("data");
    	
    	//update the metadata in CSW
    	
    	String id = request.getParameter("id");
    	
    	//the updating function is disabled for now, as the transaction function in PyCSW is disabled. 
    	
//    	if(SearchTool.updatePyCSWDataURL(id, dataurl)){
    	
    	String fileurl = BaseTool.cacheDataLocally(dataurl);
    	
    	if(!BaseTool.isNull(fileurl)) {
    		
    		resp = "{\"output\":\"success\"}";
    		
    	}else{
    		
    		resp = "{\"output\":\"failure\"}";
    		
    	}
    	
    	return resp;
    	
    }
	
	/**
     * Add dataset into ncWMS
     * add by Z.S. on 7/6/2018
     * @param model
     * @param request
     * @param status
     * @param session
     * @return
     */
    @RequestMapping(value = "/adddata", method = RequestMethod.POST)
    public @ResponseBody String adddata(ModelMap model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String resp = null;
    	
//    	String querystr = request.getQueryString();
    	
    	String id = RandomString.get(8);
    	
    	String location = request.getParameter("location");
    	
    	try {
    		
    		if(location.startsWith(SysDir.PREFIXURL)) {
    			
    			location = BaseTool.getCyberConnectorRootPath() + "/" + location.replaceAll(SysDir.PREFIXURL+"/CyberConnector/","");
    			
    			logger.debug("the new location is : " + location);
    			
    		}else if(location.startsWith(SysDir.covali_file_path)){
    			
//    			location = location;
    			
    		}else {    			
    			
    			location = SysDir.covali_file_path + location;
    			
    		}
    		
    		ncWMSTool.addDataset("id="+id + "&location=" + location);
    		
    		resp = "{\"output\":\"success\",\"id\":\""+id+"\"}";
    		
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		
    		resp = "{\"output\":\"failure\",\"reason\": \""+
    				
    				e.getLocalizedMessage() +
    				
    				"\"}";
    				
    	}
    	
    	return resp;
    	
    }

}
