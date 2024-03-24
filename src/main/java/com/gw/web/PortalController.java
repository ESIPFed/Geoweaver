package com.gw.web;

import com.gw.utils.BaseTool;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This is the controller of SpringMVC for Geoweaver
 *
 * @author Z.S.
 * @date 20170126
 */
@Controller
public class PortalController {

  Logger logger = Logger.getLogger(this.getClass());

  @Autowired BaseTool bt;

  @RequestMapping({"/", "/geoweaver"})
  public String index() {
    logger.debug("The home page is called");
    return "redirect:web/geoweaver";
  }

  @RequestMapping({"/web", "/web/"})
  public String webroot() {

    return "redirect:web/geoweaver";
  }

  @RequestMapping("/web/{name}")
  public String view(@PathVariable String name, HttpSession session) {

    if (BaseTool.isNull(name)) name = "redirect:web/geoweaver";
    return name;
  }
}
