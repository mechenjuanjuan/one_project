package com.pinyougou.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全控制
 * 认证类
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    //      set注入
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    //    前台传来参数username
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//       构建角色列表
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));

//        得到商家对象,根据id查询（数据库商家用户名就id）
        TbSeller seller = sellerService.findOne(username);
        if (seller != null) {
//            获取数据库状态字段的状态值，是1就可以登录
            if(seller.getStatus().equals("1")){
                return  new User(username, seller.getPassword(),grantedAuthorities);
            }else {
//                返回null就代表不能登录
                return null;
            }
        }else {
            return null;
        }

    }
}
