package com.gw;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gw.database.ProcessRepository;
import com.gw.jpa.GWProcess;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.web.GeoweaverController;

import org.apache.log4j.Logger;
import org.bouncycastle.jcajce.spec.GOST28147WrapParameterSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MockMVCTest {

    Logger logger = Logger.getLogger(this.getClass());

    @LocalServerPort
	private int port;

    @Autowired
    private WebApplicationContext applicationContext;
  
    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProcessTool pt;

    @Autowired
    private BaseTool bt;
  
    private MockMvc mockMvc;
  
    @BeforeEach
    void setup() {
      this.mockMvc = MockMvcBuilders
              .webAppContextSetup(applicationContext)
              .build();
    }
  
    @Test
    void testMockMVC() throws Exception {

        // String pythonjson = bt.readStringFromFile(bt.testResourceFiles()+ "/new_process.json" );

        // GWProcess gp = pt.fromJSON(pythonjson);

        // gp = this.processRepository.save(gp);

        // logger.debug(gp.getId());
  
        // this.mockMvc.perform(
        //       post("http://localhost:" + this.port + "/Geoweaver/web/detail")
        //               .param("type", "process")
        //               .param("id", gp.getId()))
        //       .andExpect(status().isOk());
    }
    
}
