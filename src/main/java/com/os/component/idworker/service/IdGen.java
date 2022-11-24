package com.os.component.idworker.service;

/**
 * ID生成器
 *
 * @author pengjunjie
 */
public interface IdGen {
    Long genId(String bizTag);
    void init();
}
