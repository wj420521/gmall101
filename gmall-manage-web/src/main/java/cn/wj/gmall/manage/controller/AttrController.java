package cn.wj.gmall.manage.controller;

import cn.wj.gmall.bean.PmsBaseAttrInfo;
import cn.wj.gmall.bean.PmsBaseAttrValue;
import cn.wj.gmall.bean.PmsBaseSaleAttr;
import cn.wj.gmall.service.AttrService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class AttrController {

    @Reference
    private AttrService attrService;

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<PmsBaseAttrInfo> attrInfoList(@RequestBody(required = false)String catalog3Id){
        List<PmsBaseAttrInfo> attrInfoList = attrService.attrInfoList(catalog3Id);
        return  attrInfoList;
    }
    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueList(@RequestBody(required = false)String attrId){
        List<PmsBaseAttrValue> pmsBaseAttrValues= attrService.getAttrValueList(attrId);
        return  pmsBaseAttrValues;
    }
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo) {

          String success=  attrService.saveAttrInfo(pmsBaseAttrInfo);

        return "success";
    }
    //销售属性
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> baseSaleAttrList(){
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs =  attrService.baseSaleAttrList();
        return pmsBaseSaleAttrs;
    }

}
