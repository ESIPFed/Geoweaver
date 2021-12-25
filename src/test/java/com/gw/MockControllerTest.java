package com.gw;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.gw.search.GWSearchTool;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

// import static org.junit.Assert.assertNotNull;
// import static org.junit.Assert.assertThat;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.contains;
// import static org.mockito.Mockito.when;
// import static org.hamcrest.Matchers.equalTo;
// import static org.hamcrest.Matchers.containsString;

// import com.gw.tools.UserTool;
// import com.gw.web.GeoweaverController;

// import org.junit.Before;
// import org.junit.Test;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.runner.RunWith;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.boot.web.server.LocalServerPort;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.test.context.junit4.SpringRunner;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;
// import org.springframework.web.client.RestTemplate;
// import org.springframework.web.context.WebApplicationContext;

// import static org.hamcrest.Matchers.containsString;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest
// @SpringBootTest
// @RunWith(SpringRunner.class)
// @WebMvcTest(GeoweaverController.class)
// @AutoConfigureMockMvc
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MockControllerTest {

    // @MockBean
	// private RestTemplate restTemplate;

    // @Autowired
	// private MockMvc mockMvc;

    // @Autowired
	// UserTool ut;

    // @LocalServerPort
	// private int port;

    @InjectMocks
    GWSearchTool st;

    @Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

    @Test
    public void testSearch(){

        // when(st.search("", "")).thenReturn("this is the search results");

        // String results = st.search("", "");

        // assertEquals(results, "this is the search results");

    }

    // @Test
	// void contextLoads() {
		
		
	// }

    // @Test
    // void testprocess(){

    //     System.out.println("Test 123456");

    // }


	// @Test
	// public void testFrontPage() throws Exception {
    //     System.out.print("test front page");
    //     mockMvc.perform(MockMvcRequestBuilders.get("/Geoweaver/web/geoweaver"))
    //                     .andExpect(status().isOk())
    //                     .andExpect(content().string(equalTo("Hello main page")));

	// }

    
}
