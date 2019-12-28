package cn.wj.gmall.search;

import cn.wj.gmall.bean.PmsSearchSkuInfo;
import cn.wj.gmall.bean.PmsSkuInfo;
import cn.wj.gmall.service.SkuService;
import com.alibaba.dubbo.config.annotation.Reference;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

    @Reference
    SkuService skuService;
    @Autowired
    JestClient jestClient;
    @Test
    public void contextLoads() throws IOException {
//        //dsl工具类
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        //from
//        searchSourceBuilder.from(0);
//        //size
//        searchSourceBuilder.size(20);
//        //highlight
//        searchSourceBuilder.highlight(null);
//
//        //bool
//        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
//        //filter
//        //term   属性名 和属性值
//        TermQueryBuilder term = new TermQueryBuilder("skuAttrValueList.valueId","48");
//        //TermQueryBuilder term1 = new TermQueryBuilder("","");
//      //  TermQueryBuilder term2 = new TermQueryBuilder("","");
//       // TermsQueryBuilder terms = new TermsQueryBuilder("","");
//        boolQueryBuilder.filter(term);
//        //must
//        MatchQueryBuilder match = new MatchQueryBuilder("skuName","超广角微距三摄");
//        boolQueryBuilder.must(match);
//        //query
//        searchSourceBuilder.query(boolQueryBuilder);
//        String s = searchSourceBuilder.toString();
//        Search build = new Search.Builder(s).addIndex("gmall101").addType("PmsSkuInfo").build();
//        //查询返回结果
//        SearchResult result = jestClient.execute(build);
//        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
//        //获取hits的值
//        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = result.getHits(PmsSearchSkuInfo.class);
//        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
//           //获取hits 中的source
//            PmsSearchSkuInfo source = hit.source;
//            pmsSearchSkuInfos.add(source);
//        }
//        System.out.println(pmsSearchSkuInfos);
        put();
    }

    public void put() throws IOException {
        //查询mysql
        List<PmsSkuInfo> skuAll = skuService.getSkuAll("287");
        //转换为es
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();
        for (PmsSkuInfo pmsSkuInfo : skuAll) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
            pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
        }
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
            Index put = new Index.Builder(pmsSearchSkuInfo).index("gmall101").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()).build();
            jestClient.execute(put);
        }
    }
}
