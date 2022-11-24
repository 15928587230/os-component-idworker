package com.os.component.idworker.service.impl;

import com.os.component.idworker.dao.SegmentDao;
import com.os.component.idworker.model.Segment;
import com.os.component.idworker.model.SegmentBuffer;
import com.os.component.idworker.service.IdGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * mysql号段模式Id生成器
 *
 * @author pengjunjie
 */
public class SegmentIdGenImpl implements IdGen {
    private static final Logger logger = LoggerFactory.getLogger(SegmentIdGenImpl.class);
    private SegmentDao segmentDao;
    private Map<String, SegmentBuffer> cache = new ConcurrentHashMap<>();
    private boolean idCacheInitOk = false;
    private ExecutorService service = new ThreadPoolExecutor(5, Integer.MAX_VALUE, 60L,
            TimeUnit.SECONDS, new SynchronousQueue<>(), new UpdateThreadFactory());

    public static class UpdateThreadFactory implements ThreadFactory {
        private static int threadInitNumber = 0;
        private static synchronized int nextThreadNum() {
            return threadInitNumber++;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Thread-Segment-Update-" + nextThreadNum());
        }
    }

    public void setSegmentDao(SegmentDao segmentDao) {
        this.segmentDao = segmentDao;
    }

    @Override
    public Long genId(String bizTag) {
        if (!idCacheInitOk) {
            throw new RuntimeException("IdCache is not ready.");
        }

        if (cache.containsKey(bizTag)) {
            SegmentBuffer segmentBuffer = cache.get(bizTag);
            if (!segmentBuffer.isInitOk()) {
                synchronized (segmentBuffer) {
                    if (!segmentBuffer.isInitOk()) {
                        updateSegmentBufferFromDB(segmentBuffer.getTag(), segmentBuffer.getCurrent());
                        segmentBuffer.setInitOk(true);
                    }
                }
            }
            return getIdFromSegmentBuffer(segmentBuffer);
        }
        throw new RuntimeException("SegmentBuffer is not ready.");
    }

    /**
     * 初始化SegmentBuffer、去掉动态Step
     *
     * @param tag
     * @param current
     * @return void
     * @author pengjunjie
     */
    protected void updateSegmentBufferFromDB(String tag, Segment current) {
        // 每次更新maxId，获取一个新的号段
        try {
            Segment segment = segmentDao.updateMaxIdAndGetSegment(tag);
            current.setBizTag(segment.getBizTag());
            current.setMaxId(segment.getMaxId());
            current.setStep(segment.getStep());
            current.getValue().set(current.getMaxId() - current.getStep());
        } catch (Exception e) {
            logger.warn("update segment {} from db {}", tag, current);
        }
    }

    /**
     * 第一次初始化的Buffer号段用完之后，这里获取需要动态加载下一个号段
     *
     * @param buffer
     * @return
     */
    protected Long getIdFromSegmentBuffer(final SegmentBuffer buffer) {
        while (true) {
            buffer.rLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                // 如果nextSegment未准备好，其它线程阻塞，只允许一个线程进来预加载segment
                if (!buffer.isNextReady()
                        && segment.getIdle() < 0.5 * segment.getStep()
                        && buffer.getAtomicBoolean().compareAndSet(false, true)) {
                    service.execute(() -> {
                        Segment next = buffer.getSegments()[buffer.nextPos()];
                        boolean updateOK = false;
                        try {
                            updateSegmentBufferFromDB(buffer.getTag(), next);
                            updateOK = true;
                        } catch (Exception e) {
                            throw new RuntimeException(e.getCause());
                        } finally {
                            if (updateOK) {
                                // 该代码必须和下面的buffer.switchPos互斥，因此需要单开线程池利用buffer的写锁互斥功能
                                buffer.wLock().lock();
                                buffer.setNextReady(true);
                                buffer.getAtomicBoolean().set(false);
                                buffer.wLock().unlock();
                            } else {
                                buffer.getAtomicBoolean().set(false);
                            }
                        }
                    });
                }
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMaxId()) {
                    return value;
                }
            } finally {
                buffer.rLock().unlock();
            }

            //假如处于segment预加载中，则等加载完毕
            waitAndSleep(buffer);
            // 同时只能有一个线程进行切换
            buffer.wLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMaxId()) {
                    return value;
                }

                if (buffer.isNextReady()) {
                    buffer.switchPos();
                    buffer.setNextReady(false);
                } else {
                    // 如果到这里，说明双buff不够用了，需要调整buffer中Id号码个数，加大step
                    logger.warn("Both segment is not ready, step must be increased!");
                }
            } finally {
                buffer.wLock().unlock();
            }
        }
    }

    /**
     * 假如处于segment预加载中，则等加载完毕
     *
     * @param buffer
     * @return void
     */
    protected void waitAndSleep(SegmentBuffer buffer) {
        int roll = 0;
        while (buffer.getAtomicBoolean().get()) {
            roll += 1;
            if (roll > 10000) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                    break;
                } catch (InterruptedException e) {
                    logger.warn("Thread {} Interrupted",Thread.currentThread().getName());
                    break;
                }
            }
        }
    }

    @Override
    public void init() {
        updateIdCacheFromDB();
        scheduledUpdateIdCacheFromDB();
        idCacheInitOk = true;
        logger.info("IdCache initialized...");
    }

    /**
     * 数据库和当前cache保持一致, buffer初始化
     *
     * @return void
     * @author pengjunjie
     */
    protected void updateIdCacheFromDB() {
        List<String> insertTas = segmentDao.getAllTags();
        if (insertTas == null || insertTas.size() == 0) {
            return;
        }
        Set<String> curTags = cache.keySet();
        List<String> dbTags = new ArrayList<>(insertTas);
        Set<String> cacheTags = new HashSet<>(curTags);

        insertTas.removeAll(curTags);
        for (String insertTag : insertTas) {
            SegmentBuffer segmentBuffer = new SegmentBuffer();
            segmentBuffer.setTag(insertTag);
            Segment segment = segmentBuffer.getCurrent();
            segment.setMaxId(0);
            segment.setStep(0);
            segment.setValue(new AtomicLong(0));
            cache.put(insertTag, segmentBuffer);
        }

        // 删除数据库没有的Tags
        cacheTags.removeAll(dbTags);
        for (String removeTag : cacheTags) {
            if (cache.containsKey(removeTag)) {
                cache.remove(removeTag);
            }
        }
    }

    /**
     * 5分钟更新一次IDCache
     *
     * @return void
     * @author pengjunjie
     */
    protected void scheduledUpdateIdCacheFromDB() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("Update-IdCache-Thread");
            t.setDaemon(true);
            return t;
        });
        service.scheduleWithFixedDelay(() -> updateIdCacheFromDB(), 60, 300, TimeUnit.SECONDS);
    }

}
