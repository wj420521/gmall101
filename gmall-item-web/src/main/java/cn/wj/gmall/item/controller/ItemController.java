package cn.wj.gmall.item.controller;

import cn.wj.gmall.bean.PmsProductSaleAttr;
import cn.wj.gmall.bean.PmsSkuInfo;
import cn.wj.gmall.bean.PmsSkuSaleAttrValue;
import cn.wj.gmall.service.SkuService;
import cn.wj.gmall.service.SpuService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    private SkuService skuService;
    @Reference
    private SpuService spuService;

    @RequestMapping("index")
    public String index(Model model){
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        model.addAttribute("list",list);
        model.addAttribute("meg","hello");
        model.addAttribute("ch","1");
        return "index";
    }

    @RequestMapping("{skuId}.html")  //10111.html
    public String item(@PathVariable String skuId, ModelMap map){
        PmsSkuInfo pmsSkuInfo = skuService.getSkuInfoById(skuId);
        map.put("skuInfo",pmsSkuInfo);
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(),pmsSkuInfo.getId());
        map.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);
//        List<PmsProductSaleAttr> list2 =  spuService.spuSaleAttrListCheckBySku(pmsSkuInfo);
//        map.put("spuSaleAttrListCheckBySku",list2);
        //通过页面传来的sku 查找当前sku下的spu 下的所有sku
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuInfoBySpu(pmsSkuInfo.getProductId());
        String k="";
        Map<String,String> m = new HashMap<>();
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String v = skuInfo.getId();
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                k+=pmsSkuSaleAttrValue.getSaleAttrValueId()+"|";
            }
            m.put(k,v);
            k="";
        }
        //将map集合转成json字符串

       String hashJson = JSON.toJSONString(m);
        map.put("hashJson",hashJson);
        return "item";
    }

}
