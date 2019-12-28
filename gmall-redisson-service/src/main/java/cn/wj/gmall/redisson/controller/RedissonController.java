package cn.wj.gmall.redisson.controller;

import cn.wj.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

@Controller
public class RedissonController {

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("redissonTest")
    @ResponseBody
    public String redissonTest(){
        Jedis jedis = redisUtil.getJedis();
        RLock lock  = redissonClient.getLock("lock"); //声明锁
        lock.lock();//上锁

        try {
            String v = jedis.get("kk");
            if(StringUtils.isBlank(v)){
                v="1";
            }
            System.out.println("->"+v);
            jedis.set("kk",(Integer.parseInt(v)+1)+"");

        }finally {
            jedis.close();
            lock.unlock(); //解锁
        }
        return  "success";
    }

}
