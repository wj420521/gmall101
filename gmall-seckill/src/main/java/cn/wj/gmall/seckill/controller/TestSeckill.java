package cn.wj.gmall.seckill.controller;

import cn.wj.gmall.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class TestSeckill {

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("redissKill")
    @ResponseBody
    public String redissKill(){
        Jedis jedis = redisUtil.getJedis();
        //监控key
        jedis.watch("119");
       //获取剩余数量
        int stock = Integer.parseInt(jedis.get("119"));
        //开启redis事务
        Transaction multi = jedis.multi();
        if(stock>0){
            //削减
            multi.incrBy("119",-1);
            //执行
            List<Object> exec = multi.exec();
            if(exec!=null&&exec.size()>0){
                System.out.println("当前库存剩余数量"+stock+",某用户抢购成功，当前抢购人数："+(1000-stock));
                   System.out.println("发送消息给订单");
              }else{
                System.out.println("当前库存剩余数量"+stock+",某用户抢购失败");
            }
        }
        jedis.close();
        return "1";
    }

    //先到先得式秒杀
    @RequestMapping("redissionKill")
    @ResponseBody
    public String redissionKill(){
        RSemaphore semaphore = redissonClient.getSemaphore("119");
        Jedis jedis = redisUtil.getJedis();
        boolean b = semaphore.tryAcquire();
        //获取剩余数量
        int stock = Integer.parseInt(jedis.get("119"));
            if(b){
                System.out.println("当前库存剩余数量"+stock+",某用户抢购成功，当前抢购人数："+(1000-stock));
                System.out.println("发送消息给订单");
        }else{
                System.out.println("当前库存剩余数量"+stock+",某用户抢购失败");
            }
        jedis.close();
        return "1";
    }
}
