package com.badal.zip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@RestController
public class Upload {

    @PostMapping("/uplaod")
    private ResponseEntity<?> uploadZIP(@RequestParam("file") MultipartFile file) throws IOException {

        File tempFile = File.createTempFile("upload", null);
        file.transferTo(tempFile);
        ZipFile zipFile = new ZipFile(tempFile);

        Metadata metadata;
        try (InputStream jsonInputStream = getJsonStream(zipFile)) {

            ObjectMapper mapper = new ObjectMapper();
            metadata = mapper.readValue(jsonInputStream, Metadata.class);
        }

        FileDetails fileDetails = getFileDetails(zipFile, metadata.getFilename());


        System.out.println(metadata.getFilename());
        System.out.println(fileDetails.getFileSize());
        System.out.println(fileDetails.getFileBytes());

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private FileDetails getFileDetails(ZipFile zipFile, String filename) throws IOException {

        FileDetails fileDetails = new FileDetails();

        ZipEntry zipEntry = zipFile
                .stream()
                .filter(z -> ((ZipEntry) z).getName().endsWith(filename))
                .collect(Collectors.toList()).get(0);


        InputStream inputStream = zipFile.getInputStream(zipEntry);
        byte[] buf = IOUtils.readFully(inputStream, -1, false);

        fileDetails.setFileSize(zipEntry.getSize());
        fileDetails.setFileBytes(buf);

        return fileDetails;
    }

    private InputStream getJsonStream(ZipFile zipFile) throws IOException {
        return zipFile.getInputStream(zipFile.stream()
                .filter(z -> ((ZipEntry) z).getName().endsWith("json"))
                .collect(Collectors.toList()).get(0));
    }
}
