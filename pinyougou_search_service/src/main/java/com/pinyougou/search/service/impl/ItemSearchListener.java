package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

/**
 * 监听类：用于监听导入索引库
 */
@Component
public class ItemSearchListener implements MessageListener{

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        System.out.println("监听接收到的消息");
        try {
//        生产者发送消息是什么类型就强转成什么类型（AMQ的五种类型）
            TextMessage textMessage = (TextMessage)message;
//            获取生产者发送的消息
            String text = textMessage.getText();
//            转换成原来的类型
            List<TbItem> list = JSON.parseArray(text, TbItem.class);
//            遍历
            for (TbItem item : list) {
                System.out.println(item.getId()+" "+item.getTitle());
//                将 spec 字段中的 json字符串转换为 map
                Map specMap = JSON.parseObject(item.getSpec());
                item.setSpecMap(specMap);
//                调用ItemSearchService里导入方法
                itemSearchService.importList(list);
                System.out.println("成功导入到索引库");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
