package com.os.component.idworker.dao;

import com.os.component.idworker.model.Segment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

/**
 * segment 持久化层
 *
 * @author pengjunjie
 */
public class SegmentDao {
    private SqlSessionFactory sqlSessionFactory;

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public List<String> getAllTags() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            return sqlSession.selectList("com.os.component.idworker.dao.SegmentMapper.getAllTags");
        } finally {
            sqlSession.close();
        }
    }

    public Segment updateMaxIdAndGetSegment(String tag) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            // 通过数据库行锁保证分布式环境数据的并发性
            sqlSession.update("com.os.component.idworker.dao.SegmentMapper.updateMaxId", tag);
            Segment segment = sqlSession.selectOne("com.os.component.idworker.dao.SegmentMapper.getSegment", tag);
            sqlSession.commit();
            return segment;
        } finally {
            sqlSession.close();
        }
    }
}
