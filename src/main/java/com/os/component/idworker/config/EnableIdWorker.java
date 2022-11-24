package com.os.component.idworker.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(IdWorkerAutoConfigure.class)
public @interface EnableIdWorker {
}