package com.aimlab.mapper;

import com.aimlab.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 站内信消息Mapper接口
 */
@Mapper
public interface MessageMapper {
    
    /**
     * 插入消息
     */
    int insert(Message message);
    
    /**
     * 根据ID查询消息
     */
    Message findById(@Param("id") Long id);
    
    /**
     * 查询用户的消息列表
     * @param receiverId 接收者ID
     * @param type 消息类型（可选）
     * @param isRead 是否已读（可选）
     * @param offset 偏移量
     * @param limit 每页数量
     */
    List<Message> findByReceiverId(@Param("receiverId") Long receiverId,
                                    @Param("type") String type,
                                    @Param("isRead") Boolean isRead,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);
    
    /**
     * 统计用户的消息数量
     */
    int countByReceiverId(@Param("receiverId") Long receiverId,
                          @Param("type") String type,
                          @Param("isRead") Boolean isRead);
    
    /**
     * 统计用户未读消息数量
     */
    int countUnreadByReceiverId(@Param("receiverId") Long receiverId);
    
    /**
     * 标记消息为已读
     */
    int markAsRead(@Param("id") Long id);
    
    /**
     * 批量标记消息为已读
     */
    int markAllAsRead(@Param("receiverId") Long receiverId);
    
    /**
     * 删除消息
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 批量删除消息
     */
    int deleteByIds(@Param("ids") List<Long> ids, @Param("receiverId") Long receiverId);
}
