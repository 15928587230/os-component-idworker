CREATE
DATABASE segment
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