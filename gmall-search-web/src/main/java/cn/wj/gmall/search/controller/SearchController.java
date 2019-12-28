package cn.wj.gmall.search.controller;

import cn.wj.gmall.annotations.LoginRequired;
import cn.wj.gmall.bean.*;
import cn.wj.gmall.service.AttrService;
import cn.wj.gmall.service.SearchService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class SearchController {
    @Reference
    private SearchService searchService;
    @Reference
    private AttrService attrService;

    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index() {
        return "index";
    }

    //    @RequestMapping("list.html")
//    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {
//        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.getAll(pmsSearchParam);
//        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);
//
//        //获取搜索结果中的valueId  set集合 无序不重复
//        Set<String> valueIdSet = new HashSet<>();
//        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
//            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
//            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
//                String valueId = pmsSkuAttrValue.getValueId();
//                valueIdSet.add(valueId);
//            }
//        }
//        //根据valueId获取平台属性集合
//        //将set集合转化为字符串 并用逗号 分割
//        String valueIdStr = StringUtils.join(valueIdSet, ',');
//        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdStr);
//        modelMap.put("attrList", pmsBaseAttrInfos);
//
//        //将输入的keyword 显示到页面 筛选那里
//        String keyword = pmsSearchParam.getKeyword();
//        String catalog3Id = pmsSearchParam.getCatalog3Id();
//        modelMap.put("keyword", keyword);
//        String[] valueIds = pmsSearchParam.getValueId();
//        String urlParam = getUrl(pmsSearchParam);
//        modelMap.put("urlParam", urlParam);
//        //点击某个属性值 ，就删除 该属性值所在的属性
//        if (valueIds != null) {  //如果没有valueId，也就不用执行下面的操作了
//            //迭代器，游标 （查找）  如果下面用几个删除的话  会出现数组下标越界， 应为删除一个的时候数组的长度就会变化
//            Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
//            while (iterator.hasNext()) {
//                PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
//                List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
//                for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
//                    //获取当前页面所有的属性值
//                    String valueIdDsl = pmsBaseAttrValue.getId();
//                    for (String valueId : valueIds) {
//                        if (valueIdDsl.contains(valueId)) {
//                            //删出传进来的属性值 所在的属性
//                            iterator.remove();
//                        }
//                    }
//                }
//            }
//        }
//        //开始面包屑功能
//        //参数封装对象
//        List<PmsSearchAttrValue> pmsSearchAttrValue = new ArrayList<>();
//        //里面的urlParam  就是当前的url -该valueId值
//        if (valueIds != null) {
//            if (StringUtils.isNotBlank(keyword)) {
//                urlParam = "keyword=" + keyword;
//
//                for (String valueId : valueIds) {
//                    PmsSearchAttrValue pmsSearch = new PmsSearchAttrValue();
//                    pmsSearch.setUrlParam(urlParam);
//                    pmsSearchAttrValue.add(pmsSearch);
//                    urlParam = urlParam + "&valueId=" + valueId;
//                }
//            }
//            if (catalog3Id != null) {
//                urlParam = "catalog3Id=" + catalog3Id;
//                for (String valueId : valueIds) {
//                    PmsSearchAttrValue pmsSearch = new PmsSearchAttrValue();
//                    pmsSearch.setUrlParam(urlParam);
//                    //对于 valueName 因为在上面的 PmsBaseAttrInfo中有 不想再写一遍代码 可以合并
//                        pmsSearchAttrValue.add(pmsSearch);
//                    urlParam = urlParam + "&valueId=" + valueId;
//                }
//            }
//            modelMap.put("attrValueSelectedList", pmsSearchAttrValue);
//        }
//
//        return "list";
//    }
    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.getAll(pmsSearchParam);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);

        //获取搜索结果中的valueId  set集合 无序不重复
        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }
        //根据valueId获取平台属性集合
        //将set集合转化为字符串 并用逗号 分割
        String valueIdStr = StringUtils.join(valueIdSet, ',');
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdStr);
        modelMap.put("attrList", pmsBaseAttrInfos);

        //将输入的keyword 显示到页面 筛选那里
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        modelMap.put("keyword", keyword);
        String[] valueIds = pmsSearchParam.getValueId();
        String urlParam = getUrl(pmsSearchParam);
        modelMap.put("urlParam", urlParam);
        //点击某个属性值 ，就删除 该属性值所在的属性
        if (valueIds != null) {  //如果没有valueId，也就不用执行下面的操作了
            //开始面包屑功能
            //参数封装对象
            List<PmsSearchAttrValue> pmsSearchAttrValue = new ArrayList<>();
            for (String valueId : valueIds) {  //这个循环和上面注释中的，， 在下面和在上面 对程序没影响  如3x4x2  和2x3x4循环次数不变
                PmsSearchAttrValue pmsSearch = new PmsSearchAttrValue();
                //里面的urlParam  就是当前的url -该valueId值
                    pmsSearch.setUrlParam(getUrlFrom(pmsSearchParam,valueId));

                //迭代器，游标 （查找）  如果下面用几个删除的话  会出现数组下标越界， 应为删除一个的时候数组的长度就会变化
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        //获取当前页面所有的属性值
                        String valueIdDsl = pmsBaseAttrValue.getId();
                        if (valueIdDsl.contains(valueId)) {
                            //删出传进来的属性值 所在的属性
                            String valueName = pmsBaseAttrValue.getValueName();
                            pmsSearch.setValueName(valueName);
                            pmsSearchAttrValue.add(pmsSearch);
                            iterator.remove();
                        }
                    }
                }
            }
            modelMap.put("attrValueSelectedList", pmsSearchAttrValue);
        }
        return "list";
    }

    public String getUrl(PmsSearchParam pmsSearchParam) {
        //属性值选择列表
        String urlParam = "";
        //每个属性值的url 应该为 当前的url +属性值
        //如果关键字不为空
        String[] valueIds = pmsSearchParam.getValueId();
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();

        if (StringUtils.isNotBlank(keyword)) {
            //如果urlParm为空 ，则keyword 为第一个参数 不需要 & 符号
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = "&";
            }
                urlParam = urlParam + "keyword=" + keyword;
        }
        //如果三级id不为空
        if (catalog3Id != null) {
            //如果urlParm为空 ，则keyword 为第一个参数 不需要 & 符号
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = "&";
            }
                urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if(valueIds!=null){
            for (String valueId : valueIds) {
                urlParam = urlParam + "&valueId=" + valueId;
            }
        }
            return urlParam;
        }

    public String getUrlFrom(PmsSearchParam pmsSearchParam,String valueId) {
        //属性值选择列表
        String urlParam = "";
        //每个属性值的url 应该为 当前的url +属性值
        //如果关键字不为空
        String[] valueIds = pmsSearchParam.getValueId();
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();

        if (StringUtils.isNotBlank(keyword)) {
            //如果urlParm为空 ，则keyword 为第一个参数 不需要 & 符号
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        //如果三级id不为空
        if (catalog3Id != null) {
            //如果urlParm为空 ，则keyword 为第一个参数 不需要 & 符号
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if(valueIds!=null){
            for (String valueId1 : valueIds) {
                //传进来的当前的valueId 和循环到id不相等时 加上。每个valueId上的url就是排除了自己，但得包含其他的valueId
                if(!valueId.equals(valueId1)){
                    urlParam = urlParam + "&valueId=" + valueId1;
                }
            }
        }
        return urlParam;
    }


}
