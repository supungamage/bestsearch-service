package com.bestsearch.bestsearchservice.order.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AmazonClientService {
	
	private AmazonS3 s3client;

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;
    
    @Value("${amazonProperties.bucketName}")
    private String bucketName;
    
    @Value("${amazonProperties.accessKey}")
    private String accessKey;
    
    @Value("${amazonProperties.secretKey}")
    private String secretKey;
    
    @PostConstruct
    private void initializeAmazon() {
    	BasicAWSCredentials creds = new BasicAWSCredentials(this.accessKey, this.secretKey);

        this.s3client = AmazonS3ClientBuilder
          .standard().withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion("us-east-1").build();
    }
    
    
    public String uploadFile(MultipartFile multipartFile) {

        String fileUrl = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile);
            // fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
            String path = LocalDate.now() + "/" + fileName;
            fileUrl = endpointUrl + "/" + path;

            uploadFileTos3bucket(path, file);
            file.delete();
        } catch (Exception e) {
           e.printStackTrace();
        }
        return fileUrl;
    }
    
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
    
    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }
    
    private void uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }
    
    public void deleteFileFroms3bucket(String fileUrl) {
    	s3client.deleteObject(new DeleteObjectRequest(bucketName, fileUrl.substring(fileUrl.lastIndexOf("/") + 1)));
    }
}
