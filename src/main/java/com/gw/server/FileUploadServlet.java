package com.gw.server;

import com.gw.utils.BaseTool;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/** The servlet for uploading a file (Jakarta Servlet multipart API). */
@WebServlet(
    name = "FileUploadServlet",
    urlPatterns = {"/FileUploadServlet"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 2000L * 1024 * 1024, maxRequestSize = 2000L * 1024 * 1024)
public class FileUploadServlet extends HttpServlet {

  private String relativePath = null,
      filePath = null,
      tempPath = null,
      prefix_url = null;

  Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${geoweaver.upload_file_path}")
  String upload_file_path;

  @Value("${geoweaver.temp_file_path}")
  String temp_file_path;

  @Value("${geoweaver.workspace}")
  String workspace;

  @Autowired BaseTool bt;

  @Override
  public void init(ServletConfig config) throws ServletException {
    relativePath = upload_file_path;
    tempPath = temp_file_path;
    filePath = bt.getFileTransferFolder();
    tempPath = bt.normalizedPath(workspace) + "/" + tempPath;

    File uploadfolder = new File(filePath);
    File tempfolder = new File(tempPath);
    if (!uploadfolder.exists()) {
      uploadfolder.mkdirs();
    }
    if (!tempfolder.exists()) {
      tempfolder.mkdirs();
    }
  }

  protected void processRequest(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("text/html; charset=utf-8");
    PrintWriter pw = res.getWriter();
    try {
      if (prefix_url == null) {
        prefix_url = "download/" + relativePath + "/";
      }

      for (Part part : req.getParts()) {
        String name = part.getName();
        String submitted = part.getSubmittedFileName();
        if (submitted == null || submitted.isEmpty()) {
          if ("script".equals(name)) {
            String value = new String(part.getInputStream().readAllBytes());
            logger.debug(name + " : " + value);
            pw.println("<script>");
            pw.println(value);
            pw.println("</script>");
          }
          continue;
        }

        String filename = submitted;
        int index = filename.lastIndexOf('\\');
        filename = filename.substring(index + 1);
        if (filename.isEmpty() && part.getSize() == 0) {
          throw new RuntimeException("You didn't upload a file.");
        }

        File uploadFile = new File(filePath + "/" + filename);
        try (InputStream in = part.getInputStream()) {
          Files.copy(in, uploadFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        logger.debug("prefix url : " + prefix_url);
        logger.info("file name : " + filename);
        pw.print("{ \"url\": \"" + prefix_url + filename + "\", \"filename\": \"" + filename + "\" }");
        logger.debug(part.getSize() + "\r\n");
      }
    } catch (Exception e) {
      e.printStackTrace();
      pw.println("ERR:" + e.getClass().getName() + ":" + e.getLocalizedMessage());
    } finally {
      pw.flush();
      pw.close();
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    PrintWriter pw = response.getWriter();
    pw.println("wrong way");
    pw.flush();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  @Override
  public String getServletInfo() {
    return "Short description";
  }
}
