package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 */
public interface BrandService {
//    查询
    List<TbBrand> findAll();
//    分页
    PageResult findPage(int page,int rows);
//    添加
    void add(TbBrand brand);
//    根据id查询
    TbBrand findOne(Long id);
//    修改
    void update(TbBrand brand);
//    删除
    void delete(Long[] ids);
//    条件查询及分页
    PageResult findPage(TbBrand brand,int page,int rows);
//    品牌下拉框数据
    List<Map> selectOptionList();
}
