package com.pinyougou.search.service.impl;

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;
import java.util.Arrays;

/**
 * 监听类：用于删除索引库的记录
 */
@Component
public class ItemDeleteListener implements MessageListener{

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
//        生产者发送消息是什么类型就强转成什么类型（AMQ的五种类型）
            ObjectMessage  objectMessage = (ObjectMessage)message;
//            获取消息
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            System.out.println("ItemDeleteListener监听接收到消息"+goodsIds);
//            调用ItemSearchService里删除方法
            itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
            System.out.println("成功删除索引库中的记录");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
