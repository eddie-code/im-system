package com.learn.message.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learn.message.dao.ImMessageHistoryEntity;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ImMessageHistoryMapper extends BaseMapper<ImMessageHistoryEntity> {

    /**
     * 批量插入（mysql - mp的批量插入 - EasySqlInjector.java 配置）
     * @param entityList
     * @return
     */
    Integer insertBatchSomeColumn(Collection<ImMessageHistoryEntity> entityList);
}
