package com.os.component.idworker;

import com.os.component.idworker.config.EnableIdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * IdWork、Id生成器
 *
 * @author pengjunjie
 */
@EnableIdWorker
@SpringBootApplication
public class OsComponentIdWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OsComponentIdWorkerApplication.class, args);
    }
}
