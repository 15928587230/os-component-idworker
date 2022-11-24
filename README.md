# 一 创建数据库和表

```sql
CREATE DATABASE IF NOT EXISTS segment;
USE segment;
CREATE TABLE `segment`
(
    `biz_tag`     varchar(128) NOT NULL DEFAULT '', -- your biz unique name
    `max_id`      bigint(20) NOT NULL DEFAULT '1',
    `step`        int(11) NOT NULL,
    `description` varchar(256)          DEFAULT NULL,
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`biz_tag`)
) ENGINE=InnoDB;

insert into segment(biz_tag, max_id, step, description)
-- 初始化业务模块和号段
values ('segment-test', 1, 10, '系统测试')
```

# 二、配置yml

> 单独配置数据源

```yaml
component:
  idworker:
    enabled: true
    jdbc-url: jdbc:mysql://127.0.0.1:3306/segment?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&nullCatalogMeansCurrent=true&useInformationSchema=false
    username: root
    password: qwer1234
```
开启IdWorker
```java
@EnableIdWorker
@SpringBootApplication
public class OsComponentIdWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OsComponentIdWorkerApplication.class, args);
    }
}
```

# 三、并发测试

> 模拟1000个线程共获取100000个Id，最后查看数据库maxId能否对应上

```java
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
            thread.setName("" + i);
            thread.start();
            countDownLatch.countDown();
        }

        Thread.sleep(30000);
    }
}
```
# 四 项目中使用
> 开箱即用
> 删除test目录、OsComponentIdWorkerApplication.java和application.yml
> 使用maven package生成jar包上传到Nexus, 引入依赖。