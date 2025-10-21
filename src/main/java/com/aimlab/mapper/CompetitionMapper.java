package com.aimlab.mapper;

import com.aimlab.entity.Competition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 比赛Mapper接口
 */
@Mapper
public interface CompetitionMapper {
    
    /**
     * 插入新比赛
     * 
     * @param competition 比赛对象
     * @return 影响的行数
     */
    int insert(Competition competition);
    
    /**
     * 根据ID查询比赛
     * 
     * @param id 比赛ID
     * @return 比赛对象，如果不存在则返回null
     */
    Competition findById(@Param("id") Integer id);
    
    /**
     * 更新比赛信息
     * 
     * @param competition 比赛对象
     * @return 影响的行数
     */
    int update(Competition competition);
    
    /**
     * 更新比赛状态
     * 
     * @param id 比赛ID
     * @param status 比赛状态
     * @return 影响的行数
     */
    int updateStatus(@Param("id") Integer id, @Param("status") String status);
    
    /**
     * 查询所有比赛
     * 
     * @return 比赛列表
     */
    List<Competition> findAll();
    
    /**
     * 根据状态查询比赛
     * 
     * @param status 比赛状态
     * @return 比赛列表
     */
    List<Competition> findByStatus(@Param("status") String status);
    
    /**
     * 根据创建者ID查询比赛
     * 
     * @param createdBy 创建者ID
     * @return 比赛列表
     */
    List<Competition> findByCreatedBy(@Param("createdBy") Long createdBy);
    
    /**
     * 删除比赛
     * 
     * @param id 比赛ID
     * @return 影响的行数
     */
    int delete(@Param("id") Long id);

    /**
     * 统计比赛总数
     *
     * @return 比赛数量
     */
    long countAll();

    /**
     * 按状态统计比赛数量
     *
     * @param status 比赛状态
     * @return 比赛数量
     */
    long countByStatus(@Param("status") String status);

    /**
     * 查询最近创建的比赛
     *
     * @param limit 返回数量上限
     * @return 比赛列表
     */
    java.util.List<Competition> findRecent(@Param("limit") int limit);

    /**
     * 查询即将开始的比赛（未开赛）
     *
     * @param limit 返回数量上限
     * @return 比赛列表
     */
    List<Competition> findUpcoming(@Param("limit") int limit);
}
