package com.gw.web;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gw.utils.BaseTool;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.stream.Collectors;
import java.util.List;

@Controller
public class ResultBrowserController {

    @Autowired BaseTool bt;

    // Inject the directory path from application.properties

    // Endpoint to list image files in the directory
    @GetMapping("/results")
    @ResponseBody
    public List<String> listImageFiles() throws IOException {
        String resultfolder = bt.getFileTransferFolder();
        Path rootLocation = Paths.get(resultfolder);
        return Files.walk(rootLocation, 1) // 1: only files in the current folder
                .filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString()) // Return file names
                .collect(Collectors.toList());
    }

    // Endpoint to serve images by filename
    @GetMapping("/results/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            String resultfolder = bt.getFileTransferFolder();
            Path file = Paths.get(resultfolder).resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }
}
