package com.os.component.idworker.service;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * IDçææå¡
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
