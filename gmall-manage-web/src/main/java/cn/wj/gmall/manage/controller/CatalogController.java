package cn.wj.gmall.manage.controller;

import cn.wj.gmall.bean.PmsBaseCatalog1;
import cn.wj.gmall.bean.PmsBaseCatalog2;
import cn.wj.gmall.bean.PmsBaseCatalog3;
import cn.wj.gmall.service.CatalogService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin   //这个注解 解决 前后端跨域问题
public class CatalogController {

    @Reference
    private CatalogService catalogService;

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1(){
        List<PmsBaseCatalog1> catalog1s = catalogService.getCatalog1();
        return catalog1s;
    }

    @RequestMapping("/getCatalog2")
    @ResponseBody                           //从前端传来json数据里面
    public List<PmsBaseCatalog2> getCatalog2(@RequestBody(required = false) String catalog1Id){
        List<PmsBaseCatalog2> catalog2s = catalogService.getCatalog2(catalog1Id);
        return catalog2s;
    }

    @RequestMapping("/getCatalog3")
    @ResponseBody                           //从前端传来json数据里面
    public List<PmsBaseCatalog3> getCatalog3(@RequestBody(required = false) String catalog2Id){
        List<PmsBaseCatalog3> catalog3s = catalogService.getCatalog3(catalog2Id);
        return catalog3s;
    }
}
