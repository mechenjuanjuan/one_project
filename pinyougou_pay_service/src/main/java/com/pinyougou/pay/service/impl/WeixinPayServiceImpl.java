package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;

/**
 * 微信支付实现类
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {
    //    注入common配置文件的属性值
    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;


    /**
     * 生成微信支付二维码
     *
     * @param out_trade_no 订单号
     * @param total_fee    总金额（分）
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
//       1 创建参数
        Map<String, String> param = new HashMap<>();//创建容器
        //添加请求参数
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商家好
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "品优购");//商家描述
        param.put("out_trade_no", out_trade_no);//商家订单号
        param.put("total_fee", total_fee);//总金额(分)
        param.put("spbill_create_ip", "127.0.0.1");//用户端实际IP
        param.put("notify_url", "http://www.itcast.cn");
        param.put("trade_type", "NATIVE");//交易类型

//       2 生成要发送的xml
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求的参数：:" + xmlParam);
            //发送请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();

//       3 获得结果
            String result = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            System.out.println("微信返回结果" + resultMap);
            Map<String, String> map = new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//生成支付二维码链接
            map.put("out_trade_no", out_trade_no);//订单号
            map.put("total_fee", total_fee);//总金额
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    /**
     * 根据订单号查询支付状态
     *
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        //1 封装数据
        Map param = new HashMap();
        param.put("appid", appid);
        param.put("mch_id", partner);
        param.put("out_trade_no", out_trade_no);
        param.put("nonce_str", WXPayUtil.generateNonceStr());

        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //2 发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //3 获取结果
            String xmlResult = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("调动查询API返回结果：" + xmlResult);

            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 关闭支付
     * @param out_trade_no
     * @return
     */
    @Override
    public Map closePay(String out_trade_no) {

        //1 封装数据
        Map param = new HashMap();
        param.put("appid", appid);//公众号ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串

        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //2 发送请求"https://api.mch.weixin.qq.com/pay/closeorder";
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //3 获取结果
            String xmlResult = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            System.out.println(map);

            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
