package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

//dubbo的service，不是本地的
@Service
public class BrandServiceImpl implements BrandService{
    @Autowired
    private TbBrandMapper tbBrandMapper;

//    查询
    @Override
    public List<TbBrand> findAll() {
        return tbBrandMapper.selectByExample(null);
    }

//      分页
    @Override
    public PageResult findPage(int page, int rows) {
        PageHelper.startPage(page, rows);
        Page<TbBrand> pages = (Page<TbBrand>) tbBrandMapper.selectByExample(null);
        return new PageResult(pages.getTotal(),pages.getResult());
    }

//    添加
    @Override
    public void add(TbBrand brand) {
        tbBrandMapper.insert(brand);
    }

//    根据id查询
    @Override
    public TbBrand findOne(Long id) {
        return tbBrandMapper.selectByPrimaryKey(id);
    }

//    修改
    @Override
    public void update(TbBrand brand) {
        tbBrandMapper.updateByPrimaryKey(brand);
    }

    //    删除
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            tbBrandMapper.deleteByPrimaryKey(id);
        }
    }
    //条件查询及分页
    @Override
    public PageResult findPage(TbBrand brand, int page, int rows) {
        PageHelper.startPage(page,rows);//分页

        TbBrandExample example = new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();
        if(brand.getName()!=null && brand.getName().length()>0){
            criteria.andNameLike("%"+brand.getName()+"%");
        }
        if(brand.getFirstChar()!=null && brand.getFirstChar().length()>0){
            criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");
        }

        Page<TbBrand> tbBrands = (Page<TbBrand>) tbBrandMapper.selectByExample(example);

        return new PageResult(tbBrands.getTotal(),tbBrands.getResult());
    }

//    列表数据
    @Override
    public List<Map> selectOptionList() {
        return tbBrandMapper.selectOptionList();
    }


}
