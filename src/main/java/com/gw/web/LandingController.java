package com.gw.web;

import com.gw.jpa.Workflow;
import com.gw.search.GWSearchTool;
import com.gw.tools.DashboardTool;
import com.gw.tools.FileTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.HostTool;
import com.gw.tools.ProcessTool;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller 
@RequestMapping(value="/landing")
public class LandingController {

    Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	ProcessTool pt;
	
	@Autowired
	WorkflowTool wt;
	
	@Autowired
	HostTool ht;
	
	@Autowired
	BaseTool bt;
	
	@Autowired
	GWSearchTool st;
	
	@Autowired
	FileTool ft;
	
	@Autowired
	HistoryTool hist;

	@Autowired
	DashboardTool dbt;

    @RequestMapping(value="/{wf_id}", method= RequestMethod.GET)
    public String workflow_landingpage(@PathVariable(value="wf_id") final String workflow_id, final ModelMap model){

        //check if the workflow is public. Private workflows have no public landing page. 
        //
        Workflow wf = wt.getById(workflow_id);

        if(!bt.isNull(wf)){

            if("FALSE".equals(wf.getConfidential())){

                String ownername = wt.getOwnerNameByID(wf.getOwner());

                model.addAttribute("workflow", wf);

                model.addAttribute("username", ownername);

            }else{

                

            }

        }

        return "wf_landing_template";

    }
    
}
