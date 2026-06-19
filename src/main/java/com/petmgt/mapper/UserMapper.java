package com.petmgt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petmgt.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    /**
     * 查找最小的可用ID（填补删除后留下的空缺）
     * 例如：用户ID为 1,2,4,5 时，返回 3；表为空时返回 1
     */
    @Select("SELECT COALESCE(MIN(t1.id + 1), 1) FROM user t1 " +
            "LEFT JOIN user t2 ON t1.id + 1 = t2.id " +
            "WHERE t2.id IS NULL")
    Long findMinAvailableId();
}
