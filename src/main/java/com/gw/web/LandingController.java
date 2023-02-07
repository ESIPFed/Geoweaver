package com.gw.web;

import com.gw.jpa.GWUser;
import com.gw.jpa.History;
import com.gw.jpa.Workflow;
import com.gw.search.GWSearchTool;
import com.gw.tools.DashboardTool;
import com.gw.tools.FileTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.HostTool;
import com.gw.tools.ProcessTool;
import com.gw.tools.WorkflowTool;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;
import java.util.List;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
    UserTool ut;
	
	@Autowired
	HistoryTool hist;

	@Autowired
	DashboardTool dbt;

    @RequestMapping(value="/{wf_id}", method= RequestMethod.GET)
    public String workflow_landingpage(@PathVariable(value="wf_id") final String workflow_id, final ModelMap model){

        //check if the workflow is public. Private workflows have no public landing page. 
        //
        Workflow wf = wt.getById(workflow_id);

        if(!BaseTool.isNull(wf)){

            if("FALSE".equals(wf.getConfidential())){

                GWUser u = ut.getUserById(wf.getOwner());

                model.addAttribute("workflow", wf);

                model.addAttribute("user", u);

                List<History> historylist = hist.getHistoryByWorkflowId(wf.getId());

                int success_num = 0, failed_num = 0, pending_num = 0, unknown_num = 0;

                for(History h: historylist){

                    if("Done".equals(h.getIndicator())){

                        success_num++;

                    }else if("Failed".equals(h.getIndicator())){

                        failed_num++;

                    }else if("Pending".equals(h.getIndicator())){

                        pending_num++;

                    }else{

                        unknown_num++;

                    }

                }

                model.addAttribute("all_history_num", historylist.size());
                model.addAttribute("success_num", success_num);
                model.addAttribute("failed_num", failed_num);
                model.addAttribute("pending_num", pending_num);
                model.addAttribute("unknown_num", unknown_num);

                JSONArray jsonArr = new JSONArray(wf.getNodes());

                model.addAttribute("nodes", jsonArr);

                model.addAttribute("historylist", historylist);

                //get recent activities
                model.addAttribute("workflowlist", wt.getWorkflowListByOwner(u.getId()));

            }else{

                throw new RuntimeException("This workflow isn't public");

            }

        }else{
            throw new RuntimeException("This workflow doesn't exist");
        }

        return "wf_landing_template";

    }
    
}
