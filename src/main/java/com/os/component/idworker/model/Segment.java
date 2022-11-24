package com.os.component.idworker.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 分配的号段、和数据库对应
 *
 * @author pengjunjie
 */
public class Segment {
    private String bizTag;
    private AtomicLong value = new AtomicLong(0);
    private volatile long maxId;
    private volatile int step;

    public long getIdle() {
        return this.getMaxId() - this.getValue().get();
    }

    public String getBizTag() {
        return bizTag;
    }

    public void setBizTag(String bizTag) {
        this.bizTag = bizTag;
    }

    public AtomicLong getValue() {
        return value;
    }

    public void setValue(AtomicLong value) {
        this.value = value;
    }

    public long getMaxId() {
        return maxId;
    }

    public void setMaxId(long maxId) {
        this.maxId = maxId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
}
