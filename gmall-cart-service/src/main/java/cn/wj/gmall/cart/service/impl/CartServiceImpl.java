package cn.wj.gmall.cart.service.impl;

import cn.wj.gmall.bean.OmsCartItem;
import cn.wj.gmall.cart.mapper.OmsCartItemMapper;
import cn.wj.gmall.service.CartService;
import cn.wj.gmall.util.RedisUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private OmsCartItemMapper omsCartItemMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public OmsCartItem getCartItemByMemberIdAndSkuId(String memberId,String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem cartItem = omsCartItemMapper.selectOne(omsCartItem);
        return cartItem;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        //添加商品进购物车
        omsCartItemMapper.insertSelective(omsCartItem);
    }

    @Override
    public void flushCartCache(String memberId) {
        //查询购物车数据
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);

        //购物车缓存数据结构   Hash  key memberId  value Map<> key productSkuId value cartItem
        //同步到缓存 获取jedis
        Map<String,String> map = new HashMap<>();
        Jedis jedis=null;
        try {
            jedis = redisUtil.getJedis();
            String key = "user:"+memberId+":cart";
            for (OmsCartItem cartItem : omsCartItems) {
                cartItem.setTotalPrice(cartItem.getQuantity().multiply(cartItem.getPrice()));
                map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
            }
            //先删除此商品的缓存再跟新
            jedis.del(key);
            jedis.hmset(key,map);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {
        //更新
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("memberId",omsCartItemFromDb.getMemberId()).andEqualTo("productSkuId",omsCartItemFromDb.getProductSkuId());
       omsCartItemMapper.updateByExample(omsCartItemFromDb,e);
    }

    @Override
    public List<OmsCartItem> cartList(String memberId) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        //从缓存中查询数据
        Jedis jedis = null;
         try {
             jedis = redisUtil.getJedis();
             List<String> hvals = jedis.hvals("user:" + memberId + ":cart");
             for (String s : hvals) {
                 OmsCartItem omsCartItem = JSON.parseObject(s, OmsCartItem.class);
                 omsCartItems.add(omsCartItem);
             }
         }catch (Exception e){
             e.printStackTrace();
             return  null;
         }finally {
             jedis.close();
         }

        return omsCartItems;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem,e);
        //同步缓存
        flushCartCache(omsCartItem.getMemberId());
    }

    @Override
    public void deleteBySkuId(String productSkuId) {
       OmsCartItem omsCartItem = new OmsCartItem();
       omsCartItem.setProductSkuId(productSkuId);
        omsCartItemMapper.delete(omsCartItem);
    }
}
