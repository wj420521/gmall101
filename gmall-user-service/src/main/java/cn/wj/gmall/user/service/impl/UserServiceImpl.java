package cn.wj.gmall.user.service.impl;

import cn.wj.gmall.bean.UmsMember;
import cn.wj.gmall.bean.UmsMemberReceiveAddress;
import cn.wj.gmall.service.UserService;
import cn.wj.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import cn.wj.gmall.user.mapper.UserMapper;
import cn.wj.gmall.util.RedisUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;
    @Autowired
    RedisUtil redisUtil;
    public List<UmsMember> findAllUser() {
        return userMapper.selectAll();
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        // 封装的参数对象
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);


//       Example example = new Example(UmsMemberReceiveAddress.class);
//       example.createCriteria().andEqualTo("memberId",memberId);
//       List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(example);

        return umsMemberReceiveAddresses;
    }

    @Override
    public UmsMember getUser(UmsMember member) {
        Jedis jedis = null;
        UmsMember umsMember = new UmsMember();
        try{
            //从缓存中查询   缓存数据结构
            jedis =  redisUtil.getJedis();
            if(jedis!=null){
                String userJson = jedis.get("user:"+member.getPassword()+member.getUsername()+":info");
                if(StringUtils.isNotBlank(userJson)){
                    //能查到
                    umsMember = JSON.parseObject(userJson, UmsMember.class);
                    return umsMember;
                }
            }
                //jdeis为空开启数据库
                umsMember.setUsername(member.getUsername());
                umsMember.setPassword(member.getPassword());
                //用集合  防止 真的出现 相同的用户名和密码。。背锅（找写注册的人）
                List<UmsMember> members = userMapper.select(umsMember);
                if(members!=null){
                    umsMember = members.get(0);
                    //写入缓存
                    jedis.setex("user:"+member.getPassword()+member.getUsername()+":info",60*60*24,JSON.toJSONString(umsMember));
                    return umsMember;
                }else{
                    //缓存和数据库都为空  用户名或密码错误
                    return null;
            }
        }finally {
            jedis.close();
        }
    }

    @Override
    public void addUserToken(String token, String memeberId) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:"+memeberId+":token",60*60*3,token);
        jedis.close();
    }

    @Override
    public void addUserFromSina(UmsMember umsMember) {
        userMapper.insertSelective(umsMember);
    }

    @Override
    public UmsMember checkUserUid(String idstr) {
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceUid(idstr);
        UmsMember umsMember1 = userMapper.selectOne(umsMember);
        return umsMember1;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(receiveAddressId);

        return umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);
    }
}
