package com.os.component.idworker;

import com.os.component.idworker.service.SegmentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

/**
 * IdWorker 功能测试
 *
 * @author pengjunjie
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class OsComponentIdWorkerApplicationTests {
    @Autowired
    private SegmentService segmentService;

    @Test
    public void contextLoads() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(100);
        for (int i = 0; i < 1000; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (int j = 0; j < 100; j++) {
                        Long test = segmentService.getSegmentId("segment-test");
                        System.out.println(test);
                    }
                }
            });
            thread.setName("并发测试线程" + i);
            thread.start();
            countDownLatch.countDown();
        }

        Thread.sleep(30000);
    }
}
