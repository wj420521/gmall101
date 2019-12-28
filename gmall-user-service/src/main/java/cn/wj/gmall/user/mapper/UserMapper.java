package cn.wj.gmall.user.mapper;

import cn.wj.gmall.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserMapper extends Mapper<UmsMember> {
    public List<UmsMember> selectAllUser();
}
