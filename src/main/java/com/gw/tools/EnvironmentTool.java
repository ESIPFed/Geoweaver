package com.gw.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.EnvironmentRepository;
import com.gw.database.HostRepository;
import com.gw.jpa.Environment;
import com.gw.jpa.Host;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class EnvironmentTool {

    @Autowired
    EnvironmentRepository envrep;

    @Autowired
    BaseTool bt;

    // @Autowired
	// LocalhostTool lt;

	// @Autowired
	// RemotehostTool rt;

    @Autowired
    EnvironmentRepository environmentrepository;

    @Autowired
    HostRepository hostRepository;

    Logger logger = Logger.getLogger(this.getClass());

    public Environment getEnvironmentById(String id){

        Optional<Environment> envop = envrep.findById(id);

        if(envop.isPresent()){

            return envop.get();
            
        }else{
            return null;
        }

    }

    public void addNewEnvironment(String pypath, List<Environment> old_envlist, String hostid, String name){

		Environment theenv = this.getEnvironmentByBin(pypath, old_envlist);

		if(BaseTool.isNull(theenv)){

			Environment env = new Environment();
			env.setId(new RandomString(6).nextString());
			env.setBin(pypath);
			env.setName(name);
			// env.setHost(hostid);
			env.setHostobj(hostRepository.findById(hostid).get());
			// env.setBasedir(line); //the execution place which is unknown at this point
			if(pypath.contains("conda"))
				env.setPyenv("anaconda");
			else
				env.setPyenv("pip");
			env.setSettings(""); //set the list of dependencies like requirements.json or .yaml
			env.setType("python"); //could be python or shell. R is not supported yet. 
			env.setBasedir("~");
			this.saveEnvironment(env);

		}

	}

    /**
	 * 
	 * @param hid
	 * @return
	 */
	public List<Environment> getEnvironmentsByHostId(String hid){

		List<Environment> envlist = new ArrayList();
		
		try {
			
			Collection<Environment> envquerylist = environmentrepository.findEnvByHost(hid);
			
			Iterator<Environment> it = envquerylist.iterator();
			
			while(it.hasNext()) {
				
				Environment newenv = it.next();

				envlist.add(newenv);
			
			}
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}
		
		return envlist;
	}

    public void saveEnvironment(Environment newenv){
		environmentrepository.save(newenv);
	}

    /**
	 * Get environments by host
	 * @param hid
	 * @return
	 */
	public String getEnvironments(String hid) {
		
		String resp = null;
		
		try {
			
			List<Environment> envlist = getEnvironmentsByHostId(hid);
			
			StringBuffer envstr = new StringBuffer("[");

			int num = 0;

			for(Environment env: envlist) {
				
				if(num!=0) {
					
					envstr.append(", ");
					
				}
				
				envstr.append(toJSON(env));
				
				num++;
				
			}
			
			envstr.append("]");
			
			resp = envstr.toString();
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}
		
		return resp;
	}
    
    public String toJSON(Environment env) {
		
		String json = "{}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(env);
            // logger.debug("ResultingJSONstring = " + json);
            //System.out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return json;
		
	}

    public void showAllEnvironment() {
		

		String resp = null;
		
		try {
			
			Iterator<Environment> envit = environmentrepository.findAll().iterator();
			
			StringBuffer envstr = new StringBuffer();
			
			envstr.append("[");
			
			int num = 0;
			
			while(envit.hasNext()) {
				
				if(num!=0) {
					
					envstr.append(", ");
					
				}
				
				Environment newenv = envit.next();
				
				envstr.append(toJSON(newenv));
				
			}
			
			envstr.append("]");
			
			resp = envstr.toString();
			
			logger.debug(resp);
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}
	}

    public String getEnvironmentByBEB(String hostid, String bin, String env, String basedir) {
		
		String resp = null;
		
		try {
			
			Collection<Environment> envlist = environmentrepository.findEnvByID_BIN_ENV_BaseDir(hostid, bin, env, basedir);
			
			StringBuffer envstr = new StringBuffer();
			
			envstr.append("[");
			
			int num = 0;
			
			Iterator<Environment> it = envlist.iterator();
			
			while(it.hasNext()) {
				
				if(num!=0) {
					
					envstr.append(", ");
					
				}
				
				Environment newenv = it.next();
				
				envstr.append(toJSON(newenv));
				
			}
			
			envstr.append("]");
			
			resp = envstr.toString();
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}
		
		return resp;
		
	}

    

    public boolean islocal(String hid) {
		
		boolean is = false;
		
		Optional<Host> opthost = hostRepository.findById(hid);

		if(opthost.isPresent()){
			Host h = hostRepository.findById(hid).get();
		
			if("127.0.0.1".equals(h.getIp()) || "localhost".equals(h.getIp())) {
				
				is = true;
				
			}
		}
		
		
		return is;
		
	}

	public Environment getEnvironmentByBin(String bin, List<Environment> envlist){
		
		Environment theenv = null;

		if(bin.length()>255){

			logger.info("The BIN is too long to save. Pass.");
			
		}else{
			
			for(Environment env: envlist){

				if(!BaseTool.isNull(env.getBin()) && env.getBin().equals(bin)){
	
					theenv = env;
	
					break;
				}
	
			}
		
		}


		return theenv;
	}

	public boolean checkIfEnvironmentExist(String bin, List<Environment> envlist){

		boolean exists = false;

		for(Environment env: envlist){

			if(!BaseTool.isNull(env.getBin()) && env.getBin().equals(bin)){

				exists = true;

				break;
			}

		}

		return exists;

	}

    /**
	 * Add environment to database
	 * @param historyid
	 * @param bin
	 * @param env
	 * @param basedir
	 * @return
	 */
	public String addEnv(String historyid, String hostid, String type, String bin, String env, String basedir, String settings) {
		
		String resp = null;
		
		try {
			
			if(!BaseTool.isNull(bin) && !BaseTool.isNull(env) && !BaseTool.isNull(basedir)){
				
				Iterator<Environment> eit = environmentrepository.findEnvByID_BIN(hostid, bin).iterator();
				
				Environment newenv = new Environment();
				
				if(eit.hasNext()) {
					
					newenv = eit.next();
					
				}else {
					
					newenv.setId(historyid);
					
					newenv.setName(bin+"-"+env+"-"+basedir);
					
					newenv.setType(type);
					
					newenv.setBin(bin);
					
					newenv.setPyenv(env);
					
					newenv.setBasedir(basedir);
					
					newenv.setSettings(settings);
					
					// newenv.setHost(hostid);
					newenv.setHostobj(hostRepository.findById(hostid).get());
					
					environmentrepository.save(newenv);
					
				}

			}else{

				logger.debug("one of the bin, env, basedir, settings is null and the environment will not be saved into database.");

			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return resp;
		
	}
}
