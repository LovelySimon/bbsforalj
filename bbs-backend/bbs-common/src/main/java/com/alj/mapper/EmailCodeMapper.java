package com.alj.mapper;

import com.alj.pojo.EmailCode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 邮箱验证码 Mapper 接口
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */

public interface EmailCodeMapper extends BaseMapper<EmailCode> {

        void disableEmailCode(@Param("email") String email);
}
