package com.ch.filesdemo;

import com.ch.filesdemo.property.FileProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    FileProperties.class
})
public class FilesDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilesDemoApplication.class, args);
    }

}
