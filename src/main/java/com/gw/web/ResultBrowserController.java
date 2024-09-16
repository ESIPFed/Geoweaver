package com.gw.web;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gw.utils.BaseTool;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ResultBrowserController {

    @Autowired BaseTool bt;

    @Value("${geoweaver.follow_symlinks:false}")
    boolean follow_symlinks;

    // Inject the directory path from application.properties

    // Endpoint to list image files in the directory
    @GetMapping("/results")
    @ResponseBody
    public List<Map<String, Object>> listFiles(@RequestParam(defaultValue = "") String subfolder) throws IOException {
        String resultfolder = bt.getResultsFolder();
        
        // Navigate into the subfolder if it's provided
        Path rootLocation = Paths.get(resultfolder, subfolder);
        System.out.println("Received " + subfolder);
        
        Stream<Path> walker = null;

        if(follow_symlinks){
            // 1: look at files in the current folder and subfolders
            walker = Files.walk(rootLocation, 1, FileVisitOption.FOLLOW_LINKS);
        }else{
            walker = Files.walk(rootLocation, 1);
        }
        
        return walker.map(path -> {
                    Map<String, Object> fileDetails = new HashMap<>();
                    try {
                        System.out.println(path);
                        Path relativePath = rootLocation.relativize(path);
                        String pathWithSubfolder = subfolder + "/" + relativePath.toString();
                        pathWithSubfolder = pathWithSubfolder.replaceAll("^/+","");

                        // Check if pathWithSubfolder contains any attempts to go up the directory
                        Path normalizedSubfolderPath = Paths.get(pathWithSubfolder).normalize();
                        System.out.println("normalizedSubfolderPath = " + normalizedSubfolderPath);
                        if (normalizedSubfolderPath.startsWith("..")) {
                            throw new SecurityException("Attempt to access outside of the result folder is not allowed.");
                        }
                        System.out.println("pathWithSubfolder = " + pathWithSubfolder);
                        
                        fileDetails.put("name", rootLocation.relativize(path).toString()); // Relative path
                        fileDetails.put("path", pathWithSubfolder); // Relative path
                        fileDetails.put("isDirectory", Files.isDirectory(path)); // Check if it's a directory
                        if (!Files.isDirectory(path)) {
                            fileDetails.put("size", Files.size(path)); // File size for files
                        }

                        // Get last modified time
                        FileTime fileTime = Files.getLastModifiedTime(path);
                        
                        // Convert FileTime to LocalDateTime
                        LocalDateTime dateTime = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
                        
                        // Format date-time to remove nanoseconds
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        String formattedDateTime = dateTime.format(formatter);
                        
                        // Add formatted last modified time to file details
                        fileDetails.put("modified", formattedDateTime);
                    } catch (IOException e){
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        System.out.println("Error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error occurred"));
                        throw e;
                    }
                    return fileDetails;
                })
                .collect(Collectors.toList());
    }


    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) {
        try {
            String resultfolder = bt.getResultsFolder();
            Path filePath = Paths.get(resultfolder).resolve(path).normalize();
            System.out.println("File path: " + filePath.toAbsolutePath());

            if (!filePath.startsWith(resultfolder)) {
                throw new SecurityException("Attempt to access outside of the result folder is not allowed.");
            }

            // Create a FileSystemResource instead of UrlResource
            Resource resource = new FileSystemResource(filePath.toFile());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(
                            HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + filePath.getFileName().toString() + "\""
                        )
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }catch (SecurityException e) {
            System.out.println("Error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error occurred"));
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint to serve images by filename
    @GetMapping("/results/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        String resultfolder = bt.getResultsFolder();
        Path filePath = Paths.get(resultfolder).resolve(filename).normalize();
        System.out.println("File path: " + filePath.toAbsolutePath());

        if (!filePath.startsWith(resultfolder)) {
            throw new SecurityException("Attempt to access outside of the result folder is not allowed.");
        }

        // Create a FileSystemResource instead of UrlResource
        Resource resource = new FileSystemResource(filePath.toFile());
        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } else {
            throw new RuntimeException("Could not read file: " + filename);
        }
    }
}
