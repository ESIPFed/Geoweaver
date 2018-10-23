package edu.gmu.csiss.earthcube.cyberconnector.servlet;

import java.util.*;
import java.io.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;

import org.apache.commons.fileupload.disk.*; 

/**
 * The servlet for uploading a file
 * @author Ziheng Sun
 */
@WebServlet(name = "FileUploadServlet", urlPatterns = {"/FileUploadServlet"})
public class FileUploadServlet extends HttpServlet {
    
	private String relativePath=null,
			
			filePath=null,
		
			tempPath=null,
		
			prefix_url=null,
		
			callback=null;
    
    private int maxvol = 2000;
    
    Logger logger = Logger.getLogger(this.getClass());
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        
//    	super.init(config); 
        
//        relativePath = config.getInitParameter("filepath");
//        
//        tempPath = config.getInitParameter("temppath");
    	
    	relativePath = SysDir.upload_file_path;
    	
    	tempPath = SysDir.temp_file_path;
        
        filePath = BaseTool.getCyberConnectorRootPath() + relativePath;
        
        tempPath = BaseTool.getCyberConnectorRootPath() + tempPath;
        
        File uploadfolder = new File(filePath);
        
        File tempfolder = new File(tempPath);
        
        if(!uploadfolder.exists()) {
        	
        	uploadfolder.mkdirs();
        	
        }
        
        if(!tempfolder.exists()) {
        	
        	tempfolder.mkdirs();
        	
        }
        
    }
    
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/html; charset=utf-8");
        PrintWriter pw = res.getWriter();
        try{
            //initialize the prefix url
            if(prefix_url==null){
            	
                prefix_url = SysDir.PREFIXURL+"/CyberConnector/"+relativePath+"/";
                
            }
            
            pw.println("<!DOCTYPE html>");
            pw.println("<html>");
            String head = "<head>" + 
		        "<title>File Uploading Response</title>" + 
		        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" + 
		        "<script type=\"text/javascript\" src=\"js/TaskGridTool.js\"></script>"+
		        "</head>";
            pw.println(head);
            pw.println("<body>");
            
            DiskFileItemFactory diskFactory = new DiskFileItemFactory();
            // threshold  4M 
            //extend to 2GB - updated by ziheng - 7/5/2018
            diskFactory.setSizeThreshold(maxvol * 1024);
            // repository 
            logger.fatal("Temp file path: " + tempPath);
            File newrepo = new File(tempPath);
            diskFactory.setRepository(newrepo);
                                 
            ServletFileUpload upload = new ServletFileUpload(diskFactory);
            // 100M
            upload.setSizeMax(maxvol * 1024 * 1024);
            // HTTP
            List fileItems = upload.parseRequest(req);
            Iterator iter = fileItems.iterator();
            while(iter.hasNext())
            {
                FileItem item = (FileItem)iter.next();
                if(item.isFormField())
                {
                    processFormField(item, pw);
                }else{
                    processUploadFile(item, pw);
                }
            }// end while()
            //add some buttons for further process
            pw.println("<input type=\"button\" id=\"bt\" value=\"load\" onclick=\"load();\">");
            pw.println("<input type=\"button\" id=\"close\" value=\"close window\" onclick=\"window.close();\">");
            pw.println("</body>");
            pw.println("</html>");
        }catch(Exception e){
            e.printStackTrace();
            pw.println("ERR:"+e.getClass().getName()+":"+e.getLocalizedMessage());
        }finally{
            pw.flush();
            pw.close();
        }
    }
    /**
     * Information of the fields except file fields
     * @param item
     * @param pw
     * @throws Exception 
     */
    private void processFormField(FileItem item, PrintWriter pw)
        throws Exception
    {
        String name = item.getFieldName();
        String value = item.getString();        
        System.out.println(name + " : " + value + "\r\n");
        if(name.equals("script")){
            pw.println("<script>");
            pw.println(value);
            pw.println("</script>");
        }
//        pw.println(name + " : " + value + "\r\n");
    }
    
    /**
     * Process uploaded file
     * @param item
     * @param pw
     * @throws Exception
     */
    private void processUploadFile(FileItem item, PrintWriter pw)
        throws Exception
    {
        String filename = item.getName();       
        
        System.out.println( filename);
        
        int index = filename.lastIndexOf("\\");
        
        filename = filename.substring(index + 1, filename.length());
                        
        long fileSize = item.getSize();
        
        if("".equals(filename) && fileSize == 0)
        {           
            throw new RuntimeException("You didn't upload a file.");
            //return;
        }
        
        File uploadFile = new File(filePath + "/" + filename);
        
        item.write(uploadFile);
        
        logger.fatal("prefix url : " + prefix_url);
        
        logger.fatal("file name : " + filename);
        
        pw.println("<a id=\"filelink\" href=\""+prefix_url+filename+"\" >Link</a> to the uploaded file : "+filename);
        
        System.out.println( fileSize + "\r\n");
    } 
    
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	PrintWriter pw = response.getWriter();
    	
    	pw.println("wrong way");
    	
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
