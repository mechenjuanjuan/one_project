package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojigroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    //    避免调用服务超时
    @Reference(timeout = 6000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
//        得到登录人账号，判断当前是否有人登录
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户是：" + username);

        // 读取本地cookie的购物车
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);

//        当用户未登陆时，username 的值为 anonymousUser
        if (username.equals("anonymousUser")) {//如果未登录
            //从cookie中提取购物车
            System.out.println("从cookie中提取购物车");
            return cartList_cookie;

        } else {//如果已登录
//           获取redis购物车
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            if (cartList_cookie.size() > 0) {//如果本地存在购物车
//                合并购物车
                cartList_redis = cartService.mergeCartList(cartList_redis,cartList_cookie);
//                消除本地cookie的数据
                CookieUtil.deleteCookie(request,response,"cartList");
//                将合并的数据存入redis
                cartService.saveCartListToRedis(username,cartList_redis);
            }
            return cartList_redis;
        }


    }


    /**
     * 添加商品到购物车
     *
     * @param itemId
     * @param num
     * @return
     */

    @RequestMapping("/addGoodsToCartList")
//    @CrossOrigin(origins = "http://localhost:9105",allowCredentials="true")//注解解决跨域
    public Result addGoodsToCartList(Long itemId, Integer num) {
//        解决跨域访问,响应请求头给前端预请求
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
//         允许携带cookie
        response.setHeader("Access-Control-Allow-Credentials", "true");


        //        得到登录人账号，判断当前是否有人登录
//        当用户未登陆时，username 的值为 anonymousUser
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户是：" + username);


        try {
            List<Cart> cartList = findCartList();//获取购物车列表
            //调用服务方法操作购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")) {//如果未登录，保存到cookie
                //将新的购物车存入cookie
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
                System.out.println("向cookie存储购物车");
            } else {//如果是以登录，保存到redis
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "存入购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "存入购物车失败");
        }
    }
}
