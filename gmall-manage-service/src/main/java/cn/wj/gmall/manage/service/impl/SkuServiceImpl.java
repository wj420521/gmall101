package cn.wj.gmall.manage.service.impl;

import cn.wj.gmall.bean.*;
import cn.wj.gmall.util.RedisUtil;
import cn.wj.gmall.manage.mapper.PmsSkuAttrValueMapper;
import cn.wj.gmall.manage.mapper.PmsSkuImageMapper;
import cn.wj.gmall.manage.mapper.PmsSkuInfoMapper;
import cn.wj.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import cn.wj.gmall.service.SkuService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Override
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        //插入skuInfo 信息
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        //插入skuImage信息
        List<PmsSkuImage> pmsSkuImages = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : pmsSkuImages) {
            pmsSkuImage.setSkuId(pmsSkuInfo.getId());
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }
        //插入skuAttrValue
        List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : pmsSkuAttrValues) {
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }
        //插入skuSaleAttrValue
        List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValues = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuSaleAttrValues) {
            pmsSkuSaleAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
        return "success";
    }


    public PmsSkuInfo getSkuInfoByIdFromDb(String skuId) {
        //根据id（主键）查询sku信息
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo= pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //查询图片信息
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> skuImages = pmsSkuImageMapper.select(pmsSkuImage);

        skuInfo.setSkuImageList(skuImages);
        return skuInfo;
    }

    public PmsSkuInfo getSkuInfoById(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //连接缓存
        Jedis jedis = redisUtil.getJedis();
        //查询缓存,通过 指定的k格式
        String skuKey = "PmsSkuInfo:"+skuId+":info";
        String valueJson = jedis.get(skuKey);
        //不为空
        if(StringUtils.isNotBlank(valueJson)){
             pmsSkuInfo = JSON.parseObject(valueJson, PmsSkuInfo.class);
        }else{
            //设置缓存分布式锁
            String token = UUID.randomUUID().toString();
            String ok = jedis.set("PmsSkuInfo:"+skuId+":lock",token,"nx","px",3*1000);//有10的时间访问mysql;
            //如果返回的字符串 为 OK 则此时这个key还没有设置进缓存
            if(StringUtils.isNotBlank(ok)&&ok.equals("OK")){
                //为空，此时拿到了锁 可以查询数据库  添加进缓存
                pmsSkuInfo = getSkuInfoByIdFromDb(skuId);
                if(pmsSkuInfo!=null){
                    //转换为json字符串，设置进缓存
                    jedis.set(skuKey,JSON.toJSONString(pmsSkuInfo));
                }else{
                    //如果数据库也为空，防止 缓存穿透  可以将这个key设置进缓存并设置过期时间,并将其值设为空串
                    jedis.setex(skuKey,3,JSON.toJSONString(""));
                }
                //访问完mysql后将锁释放    防止锁过期了 还没删除，吧别人的锁给删除了 lua脚本
                String tokenLock = jedis.get("PmsSkuInfo:"+skuId+":lock");
                if(StringUtils.isNotBlank(tokenLock)&&tokenLock.equals(token));{
                    jedis.del("PmsSkuInfo:"+skuId+":lock");
                }
            }else{
                //不是OK  自旋 则表示 此 锁 已被别人拿到 等待一会，在重新访问此方法
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //返回 重新调此方法 不会创建新的线程,  如果此处， 去掉return 则会创建一个新的线程来调用此方法
                return getSkuInfoById(skuId);
            }
        }
        //返回结果
        jedis.close();
        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuInfoBySpu(String productId) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuInfoBySpu(productId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getSkuAll(String catalog3Id) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setCatalog3Id(catalog3Id);
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.select(pmsSkuInfo);

        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String skuId = skuInfo.getId();
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            skuInfo.setSkuAttrValueList(select);
            }
        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal price) {
        boolean flag = false;
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        if(pmsSkuInfo1.getPrice().compareTo(price)==0){
            //价格相等
            flag = true;
        }
        return flag;
    }
}
