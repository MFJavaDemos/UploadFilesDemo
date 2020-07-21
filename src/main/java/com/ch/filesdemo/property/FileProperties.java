package com.ch.filesdemo.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @description: FileProperties
 * @author: chang
 * @create: 2020-07-21 16:07
 **/
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }
    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

}
