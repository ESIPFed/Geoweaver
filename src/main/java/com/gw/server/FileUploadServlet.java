package com.gw.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.gw.utils.BaseTool; 

/**
 * The servlet for uploading a file
 */
@WebServlet(name = "FileUploadServlet", urlPatterns = {"/FileUploadServlet"})
public class FileUploadServlet extends HttpServlet {
    
	private String relativePath=null,
			
			filePath=null,
		
			tempPath=null,
		
			prefix_url=null,
		
			callback=null;
    
    private int maxvol = 2000;
    
    Logger logger = LoggerFactory.getLogger(getClass());
    
    @Value("${geoweaver.upload_file_path}")
    String upload_file_path;
    
    @Value("${geoweaver.temp_file_path}")
    String temp_file_path;
    
    @Value("${geoweaver.workspace}")
    String workspace;
    
    
    
    @Autowired
    BaseTool bt;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        
//    	super.init(config); 
        
//        relativePath = config.getInitParameter("filepath");
//        
//        tempPath = config.getInitParameter("temppath");
    	
    	relativePath = upload_file_path;
    	
    	tempPath = temp_file_path;
        
//        filePath = bt.getWebAppRootPath() + relativePath;
//        
//        tempPath = bt.getWebAppRootPath() + tempPath;
    	
    	filePath = bt.getFileTransferFolder();
    	
    	tempPath = bt.normalizedPath(workspace) + "/" + tempPath;
        
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
            	
            	prefix_url = "download/" + relativePath + "/";
                
            }
            
            DiskFileItemFactory diskFactory = new DiskFileItemFactory();
            // threshold  4M 
            //extend to 2GB - updated by ziheng - 7/5/2018
            diskFactory.setSizeThreshold(maxvol * 1024);
            // repository 
            logger.debug("Temp file path: " + tempPath);
            File newrepo = new File(tempPath);
            diskFactory.setRepository(newrepo);
                                 
            ServletFileUpload upload = new ServletFileUpload(diskFactory);
            // 2000M
//            upload.setSizeMax(maxvol * 1024 * 1024);
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
            }
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
        logger.debug(name + " : " + value + "\r\n");
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
        
        logger.debug("prefix url : " + prefix_url);
        
        logger.info("file name : " + filename);
        
//        pw.println("<a id=\"filelink\" href=\""+prefix_url+filename+"\" >Link</a> to the uploaded file : "+filename);
        pw.print("{ \"url\": \"" + prefix_url + filename + "\", \"filename\": \"" + filename + "\" }");
        
        logger.debug( fileSize + "\r\n");
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
    	
    	pw.flush();
    	
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
