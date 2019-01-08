package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map search(Map searchMap) {
        Map map = new HashMap();
        //空格处理
        String keywords= (String)searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));//关键字去掉空格

        //1.查询列表
        map.putAll(searchList(searchMap));
        //2.分组查询 商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);

//        查询品牌和规格列表
        String category = (String) searchMap.get("category");
        if (!category.equals("")) {
            map.putAll(searchBrandAndSpecList(category));
        } else {
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }

        return map;
    }

    /**
     * 导入数据
     * @param list
     */
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**
     * 删除数据
     * @param goodsIdList
     */
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    /**
     * 根据关键字搜索列表
     *
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        HighlightQuery query = new SimpleHighlightQuery();
//        高亮选项初始化
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");//        高亮的前缀
        highlightOptions.setSimplePostfix("</em>");//        设置后缀
        query.setHighlightOptions(highlightOptions);//设置高亮选项


//       1. 按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

//        2.按商品分类过滤
        if (!"".equals(searchMap.get("category"))) {//如果用户选择了分类
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
//        3.按品牌管理
        if (!"".equals(searchMap.get("brand"))) {//如果用户选择了品牌
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
//      4.按规格过滤
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
//        5.价格过滤
        if (!"".equals(searchMap.get("price"))) {
            String[] price = ((String) searchMap.get("price")).split("-");
            if (!price[0].equals("0")) {//如果区间起点不等于 0
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")) {//如果最高价格不等于*
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

//        6.分页
        Integer pageNo = (Integer) searchMap.get("pageNo");//获取页码
        if (pageNo == null) {
            pageNo = 1;//设置第一页
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//每页记录数
        if (pageSize == null) {
            pageSize = 20;
        }
        query.setOffset((pageNo - 1) * pageSize);//起始索引
        query.setRows(pageSize);//每页记录数

//        7.排序
        String sortValue = (String) searchMap.get("sort");//ASC DESC
        String sortField = (String) searchMap.get("sortField");//排序字段
        if(sortValue!=null&&!sortValue.equals("")){
            if (sortValue.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if(sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }

//  *****  获取高亮结果集
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
//        循环高亮入口集合
        for (HighlightEntry<TbItem> h : page.getHighlighted()) {
            TbItem item = h.getEntity();//获取原实体类
            if (h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0) {
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));//设置高亮的结果
            }
        }
        //添加到map，前台resultMap里
        map.put("rows", page.getContent());
        map.put("totalPages",page.getTotalPages());//总页数
        map.put("total",page.getTotalElements());//总记录数
        return map;
    }

    /**
     * 查询分类列表
     *
     * @param searchMap
     * @return
     */
    private List searchCategoryList(Map searchMap) {
        List<String> list = new ArrayList();
        Query query = new SimpleQuery();
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

//        设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
//      得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
//        得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
        }
        return list;
    }


    /**
     * 根据商品分类名称查询品牌和规格列表
     *
     * @param category 商品分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        //1.根据商品分类名称得到模板ID  如手机和电脑
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (templateId != null) {
            //2.根据模板ID获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);
            System.out.println("品牌列表条数：" + brandList.size());

//            3.根据模板ID获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);
            System.out.println("规格列表条数：" + specList.size());
        }
        return map;
    }

}
