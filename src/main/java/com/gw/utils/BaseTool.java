package com.gw.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.HostRepository;
import com.gw.jpa.Host;

import org.apache.commons.text.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;



/**
 *Class BaseTool.java
 *@author ziheng
 */
@Service
public class BaseTool {
	
	public static int BUFFER_SIZE = 16384;

	private String _classpath = null;

	Logger logger = LoggerFactory.getLogger(getClass());

	String path_env = null;
	
	@Value("${geoweaver.workspace}")
	String workspace;
	
	@Value("${geoweaver.upload_file_path}")
	String upload_file_path;
	
	@Value("${geoweaver.prefixurl}")
	String prefixurl;

	@Autowired
	HostRepository hostRepository;

	String secretfilename = ".secret";

	public String getSecretfilename() {
		return this.secretfilename;
	}

	public void setSecretfilename(String secretfilename) {
		this.secretfilename = secretfilename;
	}
	
	
	public BaseTool() {
		
		
	}

	public String getLocalhostIdentifier() throws Exception{

		String keystr = null;

		try{

			SystemInfo systemInfo = new SystemInfo();
			OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
			HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
			CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();
			ComputerSystem computerSystem = hardwareAbstractionLayer.getComputerSystem();

			String vendor = operatingSystem.getManufacturer();
			String processorSerialNumber = computerSystem.getSerialNumber();
			String uuid = computerSystem.getHardwareUUID();
			String processorIdentifier = centralProcessor.getProcessorIdentifier().getIdentifier();
			int processors = centralProcessor.getLogicalProcessorCount();

			String delimiter = "-";

			keystr = String.format("%08x", vendor.hashCode()) + delimiter
					+ String.format("%08x", processorSerialNumber.hashCode()) + delimiter
					+ String.format("%08x", uuid.hashCode()) + delimiter
					+ String.format("%08x", processorIdentifier.hashCode()) + delimiter + processors;

		}catch(Exception e){

			e.printStackTrace();

			keystr = "GeoweaverWorkflowManagementSoftwareForAll";

		}
		
		return keystr;
		
	}

	public boolean checkLocalhostPassword(String received_password) throws Exception{

		String encodedreceivedpassword = this.get_SHA_512_SecurePassword(received_password, getLocalhostIdentifier());

		String readpassword = this.getLocalhostPassword();

		return readpassword.equals(encodedreceivedpassword);

	}

	public String getLocalhostPassword(){

		logger.info("get existing workspace dir: " + workspace);

		workspace = this.isNull(workspace)?"~/gw-workspace":workspace;

		logger.info("new workspace dir: " + workspace);

		String secretfile = this.normalizedPath(workspace) + FileSystems.getDefault().getSeparator() + secretfilename;

		if(new File(secretfile).exists()){
			return this.readStringFromFile(this.normalizedPath(workspace) + FileSystems.getDefault().getSeparator() + secretfilename);
		}
		
		return null;

	}

	public void setLocalhostPassword(String originalpassword, boolean force){

		try{

			String encodedpassword = getLocalhostPassword();

			if(this.isNull(encodedpassword) || force){
	
				originalpassword = this.isNull(originalpassword)? new RandomString(30).nextString(): originalpassword;
	
				encodedpassword = this.get_SHA_512_SecurePassword(originalpassword, getLocalhostIdentifier());

				workspace = this.isNull(workspace)?"~/gw-workspace":workspace;
	
				this.writeString2File(encodedpassword, 
									this.normalizedPath(workspace) + FileSystems.getDefault().getSeparator() + secretfilename);
	
			}

		}catch(Exception e){

			e.printStackTrace();

		}

	}

	/**
	 * Judge if the host is localhost
	 * @param hid
	 * @return
	 */
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

	/**
	 * Escape the code text
	 * @param code
	 * @return
	 */
	public String escape(String code) {
		
		String resp = null;
		
		if(!this.isNull(code)) {

			// resp = code.replaceAll("\\\\", "\\\\\\\\")
			// 		.replaceAll("\"", "\\\\\"")
			// 		.replaceAll("(\r\n|\r|\n|\n\r)", "<br/>")
			// 		.replaceAll("	", "\\\\t");
			resp = StringEscapeUtils.escapeJson(code);
			
		}
			
		return resp;
		
	}

	public String testResourceFiles(){

		Path resourceDirectory = Paths.get("src","test","resources");
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();

		logger.debug(absolutePath);
		return absolutePath;
	}

	public String getBody(HttpServletRequest req) {
		String body = "";
		StringBuilder sb = new StringBuilder();
		BufferedReader bufferedReader = null;
	
		try {
			bufferedReader =  req.getReader();
			char[] charBuffer = new char[128];
			int bytesRead;
			while ((bytesRead = bufferedReader.read(charBuffer)) != -1) {
				sb.append(charBuffer, 0, bytesRead);
			}
		} catch (IOException ex) {
			// swallow silently -- can't get body, won't
		} finally {
			if (bufferedReader != null) {
				try {
				bufferedReader.close();
				} catch (IOException ex) {
				// swallow silently -- can't get body, won't
				}
			}
		}
		body = sb.toString();
		return body;
	  }
	
	/**
	 * Normalize the path
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public String normalizedPath(String path) {
		
		String p = path;
		
		try {

			if(!this.isNull(path) && path.startsWith("~")) {
				
				String homedir = System.getProperty("user.home") + File.separator;
				path = path.replace("~", homedir);

				p =  Paths.get(path).normalize().toAbsolutePath().toString();
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return p;
	}
	
	public String removeLob(String lob) {
		
//		clob79: STRINGDECODE('Begin to sleep\nEnd of sleep.\n')
		
		if(!this.isNull(lob)) lob = lob.substring(lob.indexOf("STRINGDECODE(") + "STRINGDECODE(".length() + 1, lob.length()-2);
		
		return lob;
		
	}
	
	public String encodeValue(String value) {
		
	    try {
			value = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    return value;
	}

	public String getGWWorkspaceFolder(){

		return this.normalizedPath(workspace) + FileSystems.getDefault().getSeparator();

	}
	
	/**
	 * Get the temp folder for file transfer
	 * @return
	 */
	public String getFileTransferFolder() {
		
		String tempfolder = this.normalizedPath(workspace) + FileSystems.getDefault().getSeparator() + this.upload_file_path + FileSystems.getDefault().getSeparator();
		
		File tf = new File(tempfolder);
		
		if(!tf.exists()) tf.mkdirs();
		
		return tempfolder;
		
	}
	
	
	/**
	 * Judge whether an object is null
	 * @param obj
	 * @return
	 * true or false
	 */
	public static boolean isNull(Object obj){
		boolean isnull=false;
		if(obj==null || obj == "" || "".equals(obj)){
			isnull = true;
		}
		return isnull;
	}
	
	public String array2String(String[] arr, String splitter) {
		
		if (arr.length > 0) {
			
		    StringBuilder nameBuilder = new StringBuilder();

		    for (String n : arr) {
		        nameBuilder.append(n).append(splitter);
		        // can also do the following
		        // nameBuilder.append("'").append(n.replace("'", "''")).append("',");
		    }

//		    nameBuilder.deleteCharAt(nameBuilder.length() - 1);

		    return nameBuilder.toString();
		    
		} else {
		    
			return "";
			
		}
		
		
	}
	
	public String long2Date(long time) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		return sdf.format(time);
		
	}
	
	/**
	 * add on 10/31/2018
	 * @param passwordToHash
	 * @param salt
	 * @return
	 */
	public String get_SHA_512_SecurePassword(String passwordToHash, String salt){
		String generatedPassword = null;
	   try {
		   if(passwordToHash!=null){
				MessageDigest md = MessageDigest.getInstance("SHA-512");
				md.update(salt.getBytes(StandardCharsets.UTF_8));
				byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
				StringBuilder sb = new StringBuilder();
				for(int i=0; i< bytes.length ;i++){
					sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
				}
				generatedPassword = sb.toString();
		   }
	       
       } 
       catch (NoSuchAlgorithmException e){
    	   
         e.printStackTrace();
       }
	    return generatedPassword;
	}

	/**
	 * Match Email string
	 * @param email
	 * @return
	 */
	public static boolean validate(String email){
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
	}

	public String getErrorReturn(String message){

		StringBuffer json = new StringBuffer("{\"status\": \"failed\", \"reason\":\"");
		
		json.append(message).append("\"}");

		return json.toString();

	}

	public List<String> executeLocal(List<String> cmds){
		
		List envlist = new ArrayList();

		try{

			ProcessBuilder builder = new ProcessBuilder();
				
			builder.command(cmds); //bash.exe of cygwin must be in the $PATH

			builder.redirectErrorStream(true);

			Process process = builder.start();

			process.waitFor();

			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()), BaseTool.BUFFER_SIZE);
			String s = null;
			

			while ((s = in.readLine()) != null) {
				System.out.println(s);

				envlist.add(s);
				
			}

		}catch(Exception e){

			e.printStackTrace();
			
		}


		return envlist;

	}

	public String toJSON(Object h) {
			
		String json = "{}";
		ObjectMapper mapper = new ObjectMapper();
		try {
			json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(h);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
		
	}
	
	public void createWorkspace(String filepath) {
		
		File workfolder = new File(filepath).getParentFile();
		
		if(!workfolder.exists()) {
			
			workfolder.mkdirs();
			
		}
		
	}

	public void sleep(long timelen){

		try {
			
			Thread.sleep(timelen);                 //1000 milliseconds is one second.

		} catch(InterruptedException ex) {
			
			// Thread.currentThread().interrupt();
			ex.printStackTrace();

		}
	}

	/**
	 * Write string to file
	 * @param content
	 * @param filepath
	 */
	public void writeString2File(String content, String filepath){
		
		
		try {
			
			filepath = this.normalizedPath(filepath);
			
			// logger.info("Writing to file: " + filepath);
			
			createWorkspace(filepath);
			
			File nf = new File(filepath);
			
			if(nf.exists()) nf.delete();
			
			PrintWriter out;
			
			out = new PrintWriter(filepath);
			
			out.println(content);
			
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Escape the reserved characters
	 * @param msg
	 * @return
	 */
	// public String escape(String msg){
	// 	msg = msg.replaceAll("\\'", "").replaceAll("\\\n", "");
	// 	return msg;
	// }
	
	public Document parseString(String xml){
		
		SAXReader reader = new SAXReader();
		
		Document document = null;
		
		try {
			
			InputStream stream = new ByteArrayInputStream(xml.trim().getBytes("UTF-8"));
			
			document = reader.read(stream);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
	    return document;
	    
	}
	/**
	 * Parse file from URL
	 * @param url
	 * @return
	 */
	public Document parseURL(String url) 
    {
		
		URL myURL;
	       
        SAXReader reader = new SAXReader();
        Document document = null;
		try {

		    myURL = new URL(url);
			document = reader.read(myURL);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return document;
    }
	
	
	
	/**
	 * Get Local Environment Path
	 */
	public void getLocalPATHEnvironment() {
		
		if(isNull(path_env)) {
			
			if(OSValidator.isWindows()) {
				
				path_env = runCmdNoEnv("cmd.exe /C echo %PATH%");
				
			}else if(OSValidator.isMac() || OSValidator.isUnix()) {
				
				path_env = runCmdNoEnv("bash -c echo $PATH");
				
			}else {
				
				logger.error("This operating system is not supported as localhost.");
				
				
				
			}
			
			if(isNull(path_env)) {
				
				path_env = "/usr/bin/:/bin/:/usr/local/bin"; //use this default path
				
			}
			
//			logger.info("Got Path: " + path_env);
			
		}
		
	}
	
	/**
	 * Run the command without setting environment variables
	 * @param cmd
	 * @return
	 */
	public String runCmdNoEnv(String cmd) {
		
		StringBuffer logrec = new StringBuffer();
		
		try {
//			String[] env = {
//					"PATH=.:/bin/:/usr/bin:/usr/local/bin:/opt/local/bin",
//					"LD_LIBRARY_PATH=.:/usr/lib:/usr/local/lib"};
			
			ProcessBuilder builder = new ProcessBuilder(cmd.split(" "));
			
			builder.directory(new File(workspace));
			builder.redirectErrorStream(true);
			Process proc = builder.start(); // may throw IOException
//			Process proc = Runtime.getRuntime().exec(cmd.split(" "), env);
	        InputStream ips = proc.getInputStream();
	        BufferedReader brd = new BufferedReader(new InputStreamReader(ips), BaseTool.BUFFER_SIZE);
	        String str = null;
	        proc.waitFor();
	        
	        int exit_value = proc.exitValue();
	       
	        do
	        {
	        	if ((str = brd.readLine()) != null)
	        	{
	        		logrec.append(str).append("\n");
	        	}
	        }
	        while ((str != null));
	       
	        logger.debug(logrec.toString());
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return logrec.toString();
		
	}
	
	/**
	 * Run Local Command with local path environment
	 * @param command
	 * @return
	 */
	public String runCmdEnv(String cmd){
		
		StringBuffer logrec = new StringBuffer();
		
		try {
			
			getLocalPATHEnvironment();
			
			ProcessBuilder builder = new ProcessBuilder(cmd.split(" "));
			if(!isNull(path_env)) {
				builder.environment().put("PATH", path_env);
				logger.debug("Builder PATH Environment: " + builder.environment().get("PATH"));
			}
			builder.directory(new File(workspace));
			builder.redirectErrorStream(true);
			Process proc = builder.start(); // may throw IOException
	        InputStream ips = proc.getInputStream();
	        BufferedReader brd = new BufferedReader(new InputStreamReader(ips), BaseTool.BUFFER_SIZE);
	        String str = null;
	        proc.waitFor();
	        
	        int exit_value = proc.exitValue();
	        
	        do
	        {
	        	if ((str = brd.readLine()) != null)
	        	{
	        		logger.debug("PROC OUT: " + str);
	        		logrec.append(str).append("\n");
	        	}
	        }
	        while ((str != null));
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return logrec.toString();
		
	 }
	
//	 public boolean run(String script) throws InterruptedException, IOException{
//	// 	String scriptFile = "/usr/local/apache-tomcat-6.0.36/webapps/temp/"+getUUID()+".sh";
//         String scriptFile = SysDir.tempdir + getUUID()+".sh";
//         writeStringIntoFile(script, scriptFile);
//
//         Runtime.getRuntime().exec("chmod +x " + scriptFile).waitFor();
//         String[] env = {"GISBASE="+SysDir.GISBASE,
//                         "GISDBASE="+SysDir.GISDBASE,
//                         "HOME="+SysDir.HOME,
//                         "GISRC="+SysDir.GISRC,
//                         "GRASS_GUI="+SysDir.GRASS_GUI,
//                         "GIS_LOCK="+SysDir.GIS_LOCK,
//                         "PATH="+SysDir.PATH,
//                         "DYLD_LIBRARY_PATH="+SysDir.DYLD_LIBRARY_PATH,
//                         "LD_LIBRARY_PATH="+SysDir.LD_LIBRARY_PATH,
//                         "GRASS_PERL="+SysDir.GRASS_PERL,
//                         "GRASS_PAGER="+SysDir.GRASS_PAGER};
//         Runtime.getRuntime().exec("chmod +x " + scriptFile).waitFor();
//         Process proc = Runtime.getRuntime().exec(scriptFile, env);
//
//         //[GISBASE=/usr/lib/grass64/,
//         //GISDBASE=/usr/local/apache-tomcat-6.0.36/webapps/temp/gis1366695299419, 
//         //HOME=/usr/local/apache-tomcat-6.0.36/webapps/temp/gis1366695299419, 
//         //GISRC=/usr/local/apache-tomcat-6.0.36/webapps/temp/gis1366695299419/.grassrc, 
//         //GRASS_GUI=text, 
//         //GIS_LOCK=0, 
//         //PATH=/usr/lib/grass64//bin:/usr/lib/grass64//scripts:/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/usr/local/grasslib/bin:.,
//         //DYLD_LIBRARY_PATH=/usr/lib//lib:/usr/lib/grass64//lib:/lib:/usr/lib:/usr/local/lib:/usr/local/grasslib/lib, 
//         //LD_LIBRARY_PATH=/usr/lib//lib:/usr/lib/grass64//lib:/lib:/usr/lib:/usr/local/lib:/usr/local/grasslib/lib,
//         //GRASS_PERL=/usr/bin/perl, 
//         //GRASS_ORGANIZATION=LIESMARS, 
//         //GRASS_PAGER=more]
//
//       InputStream ips = proc.getInputStream();
//       InputStream eps = proc.getErrorStream();
//       BufferedReader brd = new BufferedReader(new InputStreamReader(ips));
//       BufferedReader ebrd = new BufferedReader(new InputStreamReader(eps));
//       String str = null; String estr = null;
//       proc.waitFor();
//       int exit_value = proc.exitValue();
//       StringBuffer logrec = new StringBuffer();
//       if (exit_value != 0)
//       {
//         err.append("The following error was generated while running the script:\n"  +scriptFile+ "\n");
//         do
//         {
//           if ((estr = ebrd.readLine()) == null)
//             continue;
//           err.append("PROC ERR: " + estr + "\n");
//           logrec.append( estr + "\n");
//         }
//         while ((str != null) || (estr != null));
//         System.out.println(err);
//         return false;
//       }
//
//       do
//       {
//         if ((str = brd.readLine()) != null)
//         {
//           System.out.println("PROC OUT: " + str);
//           logrec.append(str).append("\n");
//         }
//         if ((estr = ebrd.readLine()) == null)
//           continue;
//         System.out.println("PROC ERR: " + estr);
//         logrec.append(estr).append("\n");
//       }
//       while ((str != null) || (estr != null));
//       System.out.println(err);
//       log = logrec.toString();
//       return true;
// 	}

	
	public String[] parseJupyterURL(String url) {
		
		String[] cc = new String[4];
		
		try {
			
			URL aURL = new URL(url);

			// logger.debug("protocol = " + aURL.getProtocol());
			// logger.debug("authority = " + aURL.getAuthority());
			// logger.debug("host = " + aURL.getHost());
			// logger.debug("port = " + aURL.getPort());
			// logger.debug("path = " + aURL.getPath());
			// logger.debug("query = " + aURL.getQuery());
			// logger.debug("filename = " + aURL.getFile());
			// logger.debug("ref = " + aURL.getRef());
			
			cc[0] = aURL.getProtocol();
			cc[1] = aURL.getHost();
			
			if(aURL.getPort()!=-1) {

				cc[2] = String.valueOf(aURL.getPort());
				
			}else {
				
				cc[2] = "80";
				
			}
			
			cc[3] = aURL.getPath();
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		
//		if(url!=null) {
//			
//			String[] ss = url.split(":");
//			
//			String current_scheme = ss[0];
//			
//			String current_ip = ss[1].substring(2);
//			
////			int current_port = Integer.parseInt(ss[2].replaceAll("\\D", ""));
//			
//			cc[0] = current_scheme;
//			
//			cc[1] = current_ip;
//			
//			if(ss.length<2) {
//				
//				cc[2] = "80";
//				
//			}else {
//				
//				System.out.println(ss[2]);
//				
//				cc[2] = ss[2].replaceAll("\\D", "");
//				
//			}
//			
//		}
		
		return cc;
		
	}
	
	public String[] parseGoogleEarthURL(String url) {
		
		String[] cc = new String[4];
		
		try {
			
			URL aURL = new URL(url);

			// logger.debug("protocol = " + aURL.getProtocol());
			// logger.debug("authority = " + aURL.getAuthority());
			// logger.debug("host = " + aURL.getHost());
			// logger.debug("port = " + aURL.getPort());
			// logger.debug("path = " + aURL.getPath());
			// logger.debug("query = " + aURL.getQuery());
			// logger.debug("filename = " + aURL.getFile());
			// logger.debug("ref = " + aURL.getRef());
			
			cc[0] = aURL.getProtocol();
			cc[1] = aURL.getHost();
			
			if(aURL.getPort()!=-1) {

				cc[2] = String.valueOf(aURL.getPort());
				
			}else {
				
				cc[2] = "443";
				
			}
			
			cc[3] = aURL.getPath();
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		
//		if(url!=null) {
//			
//			String[] ss = url.split(":");
//			
//			String current_scheme = ss[0];
//			
//			String current_ip = ss[1].substring(2);
//			
////			int current_port = Integer.parseInt(ss[2].replaceAll("\\D", ""));
//			
//			cc[0] = current_scheme;
//			
//			cc[1] = current_ip;
//			
//			if(ss.length<2) {
//				
//				cc[2] = "80";
//				
//			}else {
//				
//				System.out.println(ss[2]);
//				
//				cc[2] = ss[2].replaceAll("\\D", "");
//				
//			}
//			
//		}
		
		return cc;
		
	}


	/**
	 * Run Local Command
	 * @param command
	 * @return
	 */
	public String runLocalNuxCommand(String command) {
		
		StringBuffer output = new StringBuffer();
		
		try {
			
			logger.debug("PATH enviroment: " + System.getenv("PATH"));
			
			ProcessBuilder builder = new ProcessBuilder();
    		
			builder.command("bash", "-c", command);
    		
    		builder.redirectErrorStream(true);
    		
    		Process process = builder.start();
    		
    		InputStream stdout = process.getInputStream ();
    		
            logger.debug("Local session established");
            
            BufferedReader input = new BufferedReader(new InputStreamReader(stdout), BaseTool.BUFFER_SIZE);
            
            String line;
        	while ((line = input.readLine()) != null) {
        		output.append(line + "\n");
        	}

        	int exitVal = process.waitFor();
        	if (exitVal == 0) {
        		logger.debug("Success!");
        	} else {
        		//abnormal...
        		output.append("Failed");
        	}
            
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return output.toString();
		
		
	}
	
	/**
	 * Get the root file path of Geoweaver
	 * @return
	 */
	public String getWebAppRootPath(){
		
		String classpath = getClassPath();
		
		String rootpath = classpath;
		
		if(classpath.indexOf("WEB-INF")!=-1)
			
			rootpath = classpath.substring(0, classpath.indexOf("WEB-INF")) + "/";
		
		return rootpath;
	}

	public void sleep(int seconds){

		try {
			TimeUnit.SECONDS.sleep(seconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Delete directory
	 * @param directoryToBeDeleted
	 * @return
	 */
	public boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	public void printoutCallStack(){

        System.out.println("Printing stack trace:");
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < elements.length; i++) {
            StackTraceElement s = elements[i];
            System.out.println("\tnull trace at " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
        }
    }

	/**
	 * Get the absolute path of the file which has workflow.json if not found,
	 * return empty string
	 * 
	 * @param folderPath
	 */
	public String getWorkflowJsonPath(String folderPath) {
		File[] files = new File(folderPath).listFiles();
		for (File entry : files) {
			// Check if it is a directory
			if (entry.isDirectory()) {
				// call getWorkflowJsonPath(entry)
				String jsonpath = getWorkflowJsonPath(entry.getAbsolutePath());
				if(jsonpath!=""){
					return jsonpath;
				}
			} else {
				// check if it is workflow.json
				if (entry.getName().equals("workflow.json")) {
					return entry.getAbsolutePath();
				}
			}
		}
		return ""; // no workflow.json found
	}
	
	/**
	 * Unzip file to folder
	 * @param filepath
	 * @param targetfolder
	 */
	public void unzip(String filepath, String targetfolder){

		try{
			logger.debug("Unzipping " + filepath + " to " + targetfolder);
			String fileZip = filepath;
			File destDir = new File(targetfolder);
			byte[] buffer = new byte[1024];
			ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				File newFile = newFile(destDir, zipEntry);
				if (zipEntry.isDirectory()) {
					if (!newFile.isDirectory() && !newFile.mkdirs()) {
						throw new IOException("Failed to create directory " + newFile);
					}
				} else {
					// fix for Windows-created archives
					File parent = newFile.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						throw new IOException("Failed to create directory " + parent);
					}
					
					// write file content
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
				}
				zipEntry = zis.getNextEntry();
		   	}
			zis.closeEntry();
			zis.close();
	
		}catch(Exception e){

			e.printStackTrace();

		}

	}

	public File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());
	
		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();
	
		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}
	
		return destFile;
	}

	public void zipFolder(String folderpath, String targetfile){

		try{

			String sourceFile = folderpath;
			FileOutputStream fos = new FileOutputStream(targetfile);
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			File fileToZip = new File(sourceFile);
	
			zipFile(fileToZip, "", zipOut);
			zipOut.close();
			fos.close();
	
		}catch(Exception e){

			e.printStackTrace();

		}
		
	}

	private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
				String newchildfile = null;
				if(this.isNull(fileName)){
					newchildfile = childFile.getName();
				}else{
					newchildfile = fileName + "/" + childFile.getName();
				}
                zipFile(childFile, newchildfile, zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();

    }

	public void tar(String folderpath, String targetfile){

		try {
			
			List<String> files = Files.list(Paths.get(folderpath))
                        .map(Path::toString)
                        .collect(Collectors.toList());
            
			//zip the files into a tar file
			this.tar(files, targetfile);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}

	}
	
	/**
	 * Zip the files into a tar file
	 * @param files
	 * @param targetfile
	 */
	public void tar(List<String> files, String targetfile) {
		
		try {
			
			// Output file stream
			FileOutputStream dest = new FileOutputStream( targetfile );
			
			// Create a TarOutputStream
			TarOutputStream out = new TarOutputStream( new BufferedOutputStream( dest ) );
			  
			// Files to tar
			for(String fp:files){
				 File f = new File(fp);
			     out.putNextEntry(new TarEntry(f, f.getName()));
			     BufferedInputStream origin = new BufferedInputStream(new FileInputStream( f ));
			     int count;
			     byte data[] = new byte[2048];
			  
			     while((count = origin.read(data)) != -1) {
			        out.write(data, 0, count);
			     }
			  
			     out.flush();
			     origin.close();
			}
			  
			out.close();
			
		}catch(Exception e) {
			
			e.printStackTrace();
			throw new RuntimeException("Fail to tar the files" + e.getLocalizedMessage());
			
		}
		
	}
	
	/**
	 * Get classpath
	 * @return
	 * class path
	 */
	public String getClassPath(){
		if(isNull(_classpath)){
			String dir = this.getClass().getClassLoader().getResource("").getPath();
			try {
				dir = URLDecoder.decode(dir,"utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			_classpath = dir;
		}
		return _classpath;
	}
	
	/**
     * 
     * @param savedir
     * @param imgUrl
     */
	public void down(String savedir, String imgUrl) {
		File f = new File(savedir);
        byte[] buffer = new byte[8 * 1024];
        URL u;
        URLConnection connection = null;
        try {
                u = new URL(imgUrl);
                connection = u.openConnection();
        } catch (Exception e) {
                logger.error("ERR:" + imgUrl);
                return;
        }
        connection.setReadTimeout(1000000); //milliseconds
        InputStream is = null;
        FileOutputStream fos = null;
        try {
                f.createNewFile();
                is = connection.getInputStream();
                fos = new FileOutputStream(f);
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                }

        } catch (Exception e) {
                f.delete();
                if (fos != null) {
                        try {
                                fos.close();
                        } catch (IOException e1) {
                        	e1.printStackTrace();
                        }
                }
                if (is != null) {
                        try {
                                is.close();
                        } catch (IOException e1) {
                        	e1.printStackTrace();
                        }
                }
                throw new RuntimeException("Fail to download the image from the link.."+e.getClass().getName()+":"+e.getLocalizedMessage());
        }
        logger.info(imgUrl+" is downloaded!");
//        buffer = null;
        // System.gc();
	}
	
	/**
	 * Read the string from a file
	 * @param path
	 * @return
	 */
	public String readStringFromFile(String path){
		StringBuffer strLine = new StringBuffer();
		try{
			  // Open the file that is the first 
			  // command line parameter
			  FileInputStream fstream = new FileInputStream(path);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  FileReader fr = new FileReader(path);
			  BufferedReader br = new BufferedReader(fr);
			  String str = null;
			  //Read File Line By Line
			  while ((str = br.readLine()) != null)   {
			  // Print the content on the console
				  strLine.append(str).append("\n");
//				  System.out.println (strLine);
			  }
			  //Close the input stream
			  in.close();
			  fstream.close();
		}catch (Exception e){
			  //Catch exception if any
			  System.err.println("Error: " + e.getMessage());
	    }
		return strLine.toString().trim();
	}
	
	/**
    /**
     * Get the DATETIME format of current time
     * @return
     * DATETIME
     */
    public String getCurrentMySQLDatetime(){
    	java.util.Date dt = new java.util.Date();
    	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String currentTime = sdf.format(dt);
    	return currentTime;
    }

	public static long calculateDuration(Date d1, Date d2){

		long seconds = (d2.getTime()-d1.getTime())/1000;

		return seconds;

	}
    
    public static Date getCurrentSQLDate(){
    	
    	Date newdate = new Date();
    	
    	return newdate;
    	
    }

	public Date parseSQLDateStr(String datestr){

		// 2021-07-06 20:35:11.535
		Date date1 = null;
		try {
			if(!this.isNull(datestr))
				date1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS").parse(datestr);
		} catch (ParseException e) {

			e.printStackTrace();
		}  
		
		return date1;

	}

	public String formatDate(Date date){

		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");  
    	
		return formatter.format(date);  
		
	}
    
}
