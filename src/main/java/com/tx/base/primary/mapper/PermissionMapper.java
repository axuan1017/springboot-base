package com.tx.base.primary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tx.base.primary.entity.Permission;

import java.util.List;

/**
 * <p>
 * 权限 Mapper 接口
 * </p>
 * @author testjava
 * @since 2020-01-12
 */
public interface PermissionMapper extends BaseMapper<Permission> {

    List<String> selectPermissionValueByUserId(String id);

    List<String> selectAllPermissionValue();

    List<Permission> selectPermissionByUserId(String userId);
}
