package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.security.KeyPair;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.ssh.RSAEncryptTool;
import com.gw.tools.ExecutionTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.LocalhostTool;
import com.gw.tools.ProcessTool;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import net.bytebuddy.utility.RandomString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PythonTest {

	@InjectMocks
    @Autowired
	UserTool ut;

	@InjectMocks
	@Autowired
	BaseTool bt;

    @Autowired
	private TestRestTemplate testrestTemplate;

    @LocalServerPort
	private int port;

	@InjectMocks
	@Autowired
	ExecutionTool et;

	@InjectMocks
	@Autowired
	ProcessTool pt;

	@InjectMocks
	@Autowired
	HistoryTool hist;

	@InjectMocks
	@Autowired
	LocalhostTool ltmock;


    Logger logger  = Logger.getLogger(this.getClass());
    
    @Test
    public void testPythonProcess() throws Exception{
		logger.info("Start to test creating, running, deleting python process..");
        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String pythonjson = bt.readStringFromFile(bt.testResourceFiles()+ "/add_python_process.json" );
    	HttpEntity request = new HttpEntity<>(pythonjson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add/process", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("id");

		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(result, Map.class);
		String pid = String.valueOf(map.get("id"));

		//get key
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/key", 
			null, 
			String.class);

		JSONParser jsonParser=new JSONParser();

		JSONObject jsonobj = (JSONObject)jsonParser.parse(result);

		String rsakey = jsonobj.get("rsa_public").toString();
		
		assertNotNull(rsakey);

		//encode the password
		
		KeyPair kpair = RSAEncryptTool.buildKeyPair();

		// PublicKey pubkey = KeyFactory.getInstance("RSA")
        //         .generatePublic(new X509EncodedKeySpec(rsakey.getBytes()));

		String encryppswd = RSAEncryptTool.byte2Base64(RSAEncryptTool.encrypt(kpair.getPublic(), "testpswd"));

		logger.info(encryppswd);
		
		// run the python process
		ltmock = Mockito.spy(ltmock);
		doNothing().when(ltmock).authenticate(anyString());

		String historyid = new RandomString(12).nextString();

		ltmock.executePythonProcess(historyid, pid, "100001", encryppswd, historyid, true, "", "", "");

		// check if history is generated
		String resp = pt.one_history(historyid);
		assertThat(resp).contains("\"hid\": \""+historyid+"\"");

		// remove the history
		hist.deleteById(historyid);
		resp = pt.one_history(historyid);
		assertThat(resp).isEmpty();

		//edit the python process
		pythonjson = bt.readStringFromFile(bt.testResourceFiles()+ "/edit_python_process.json" );
		pythonjson = pythonjson.replace("<python_process_id>", pid);
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
    	request = new HttpEntity<>(pythonjson, headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/edit/process", 
			request, 
			String.class);
		logger.debug("the result is: " + result); //{"id" : "beqbtr"}
		assertThat(result).contains("id");

		//remove the python process
		// id=2avx48&type=process
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	request = new HttpEntity<>("id="+pid+"&type=process", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/del", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("done");

    }

}
