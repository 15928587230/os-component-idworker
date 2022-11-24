package com.os.component.idworker.service;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * ID生成服务
 *
 * @author pengjunjie
 */
public class SegmentService {

    @Autowired
    private IdGen idGen;

    public Long getSegmentId(String bizTag) {
        return idGen.genId(bizTag);
    }
}
