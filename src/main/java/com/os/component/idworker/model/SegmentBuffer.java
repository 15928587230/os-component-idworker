package com.os.component.idworker.model;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 双segment模式
 *
 * @author pengjunjie
 */
public class SegmentBuffer {
    private String tag;
    private Segment[] segments;
    private volatile int currentPos;
    private volatile boolean initOk;
    private volatile boolean nextReady;
    private ReentrantReadWriteLock lock;
    private AtomicBoolean atomicBoolean;

    public SegmentBuffer() {
        segments = new Segment[] {new Segment(), new Segment()};
        currentPos = 0;
        initOk = false;
        lock = new ReentrantReadWriteLock();
        nextReady = false;
        atomicBoolean = new AtomicBoolean(false);
    }

    public ReentrantReadWriteLock.ReadLock rLock() {
        return lock.readLock();
    }

    public ReentrantReadWriteLock.WriteLock wLock() {
        return lock.writeLock();
    }

    public int nextPos() {
        return  (currentPos + 1) % 2;
    }

    public void switchPos() {
        this.currentPos = nextPos();
    }

    public AtomicBoolean getAtomicBoolean() {
        return atomicBoolean;
    }

    public void setAtomicBoolean(AtomicBoolean atomicBoolean) {
        this.atomicBoolean = atomicBoolean;
    }

    public long getIdle() {
        return this.getCurrent().getMaxId() - this.getCurrent().getValue().get();
    }

    public Segment getCurrent() {
        return segments[currentPos];
    }

    public boolean isNextReady() {
        return nextReady;
    }

    public void setNextReady(boolean nextReady) {
        this.nextReady = nextReady;
    }

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    public void setLock(ReentrantReadWriteLock lock) {
        this.lock = lock;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Segment[] getSegments() {
        return segments;
    }

    public void setSegments(Segment[] segments) {
        this.segments = segments;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(int currentPos) {
        this.currentPos = currentPos;
    }

    public boolean isInitOk() {
        return initOk;
    }

    public void setInitOk(boolean initOk) {
        this.initOk = initOk;
    }
}
