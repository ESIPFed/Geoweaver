package com.gw.web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class FileDownloadController {
	
	Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	BaseTool bt;

	
    //	http://localhost:8070/Geoweaver/download/temp/testoutput.nc
	@RequestMapping(value="/download/temp/{filename}", method=RequestMethod.GET)
	public void  downloadFile(HttpServletRequest request, HttpServletResponse response, @PathVariable(value="filename") String filename) {
		
		logger.info("Get file downloading request: " + filename);
		
		try {
			
			String fileloc = bt.getFileTransferFolder() + "/" + filename;
			
			File file = new File(fileloc);
			
			if (file.exists()) {

				String mimeType = "application/octet-stream";
				response.setContentType(mimeType);

				/**
				 * In a regular HTTP response, the Content-Disposition response header is a
				 * header indicating if the content is expected to be displayed inline in the
				 * browser, that is, as a Web page or as part of a Web page, or as an
				 * attachment, that is downloaded and saved locally.
				 * 
				 */

				/**
				 * Here we have mentioned it to show inline
				 */
				response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

				//Here we have mentioned it to show as attachment
				//response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + file.getName() + "\""));

				response.setContentLength((int) file.length());

				InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
				
				FileCopyUtils.copy(inputStream, response.getOutputStream());

			}
		
		} catch (IOException e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("Cannot download the file " + e.getLocalizedMessage());
			
		}
		
	}
	
}