package com.aimlab.mapper;

import com.aimlab.entity.Athlete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 运动员Mapper接口
 */
@Mapper
public interface AthleteMapper {
    
    /**
     * 插入新运动员信息
     * 
     * @param athlete 运动员对象
     * @return 影响的行数
     */
    int insert(Athlete athlete);
    
    /**
     * 根据ID查询运动员信息
     * 
     * @param id 运动员ID
     * @return 运动员对象，如果不存在则返回null
     */
    Athlete findById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询运动员信息
     * 
     * @param userId 用户ID
     * @return 运动员对象，如果不存在则返回null
     */
    Athlete findByUserId(@Param("userId") Long userId);
    
    /**
     * 更新运动员信息
     * 
     * @param athlete 运动员对象
     * @return 影响的行数
     */
    int update(Athlete athlete);
    
    /**
     * 删除运动员信息
     * 
     * @param id 运动员ID
     * @return 影响的行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询全部运动员
     *
     * @return 运动员列表
     */
    List<Athlete> findAll();

    /**
     * 统计运动员总数
     *
     * @return 运动员数量
     */
    long countAll();

    /**
     * 按审批状态查询运动员
     *
     * @param status 审批状态
     * @param limit  返回数量限制，可为空
     * @return 运动员列表
     */
    List<Athlete> findByApprovalStatus(@Param("status") String status, @Param("limit") Integer limit);

    /**
     * 统计指定审批状态的运动员数量
     *
     * @param status 审批状态
     * @return 数量
     */
    long countByApprovalStatus(@Param("status") String status);
    
    /**
     * 更新运动员头像（BLOB存储）
     *
     * @param id 运动员ID
     * @param avatarData 头像二进制数据
     * @param avatarType 头像MIME类型
     * @return 影响的行数
     */
    int updateAvatar(@Param("id") Long id, @Param("avatarData") byte[] avatarData, @Param("avatarType") String avatarType);
}
