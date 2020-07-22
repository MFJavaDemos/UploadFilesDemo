package com.ch.filesdemo.controller;

import com.ch.filesdemo.dto.UploadFileResponse;
import com.ch.filesdemo.service.FileService;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabGroup;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabSimpleRepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: File controller
 * @author: chang
 * @create: 2020-07-21 16:05
 **/
@RestController
public class FileController {


    private final String GitLab_URL = "http://192.168.110.89/";
    private final String api_Token = "eNjSzLSh3B_RBhG7uNgs";

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file){

        String fileName = fileService.storeFile(file);
        String filePath = fileService.getFilePath();

        String group = "chtest";
        String projectName = "demo";
        String folderName = "documents";


        String fileDownloadUri = "上传文件失败！";
        try {
            fileDownloadUri = uploadToGitlab(group,projectName,folderName,fileName,filePath);
        }catch (Exception e){
            System.out.println("上传文件失败： " + e.getMessage());
        }
        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }


    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.stream(files)
                .map(this::uploadFile)
                .collect(Collectors.toList());
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


    /**
     * 上传文件到 gitlab
     * @param group 组织名称 如 chtest
     * @param projectName 工程名称，如 demo
     * @param folderName 文件夹，可多层，如 ch/test
     * @param fileName 文件名，如 readme.txt
     * @param filePath 文件所在电脑路径
     * @throws IOException
     */
    private String uploadToGitlab(String group, String projectName, String folderName, String fileName, String filePath) throws IOException {


        String gitlabUrl = GitLab_URL;
        String apiToken = api_Token;

        String branchName = "master";

        GitlabAPI api = GitlabAPI.connect(gitlabUrl, apiToken);

        GitlabProject project = null;
        try {

            GitlabGroup gitlabGroup = api.getGroup(group);

            try {
                project = api.getProject(group,projectName);
                if (project == null) {
                    project = api.createProjectForGroup(projectName, gitlabGroup);
                }
            }catch (Exception e){
                System.out.println("不存在项目，新建工程 " + e.getMessage());
                project = api.createProjectForGroup(projectName, gitlabGroup);
            }

            String base64file = fileToBase64(filePath);
            String gitlabPath = folderName+"/"+fileName;

            System.out.println("开始上传:" + gitlabPath);
            GitlabSimpleRepositoryFile repositoryFile = api.createRepositoryFile(project, gitlabPath, branchName,
                    "上传文件", base64file);

            System.out.println("结束上传:" + repositoryFile.getFilePath());

            return project.getWebUrl()+"/blob/master/"+gitlabPath;
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    private String fileToBase64(String path) {
        String base64 = null;
        InputStream in = null;
        try {
            File file = new File(path);
            in = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            in.read(bytes);
            base64 = Base64.getEncoder().encodeToString(bytes);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return base64;
    }

}
