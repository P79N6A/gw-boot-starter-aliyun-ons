package com.aliyun.openservices.ons.api.batch;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import java.util.List;

/**
 * 批量消息监听器，BatchConsumer注册消息监听器来批量地消费消息
 */
public interface BatchMessageListener {
    /**
     * 批量消费消息接口，由应用来实现<br>
     * 需要注意网络抖动等不稳定的情形可能会带来消息重复，对重复消息敏感的业务可对消息做幂等处理
     *
     * @param messages 一批消息
     * @param context 消费上下文
     * @return {@link Action} 消费结果，如果应用抛出异常或者返回Null等价于返回Action.ReconsumeLater
     * @see <a href="https://help.aliyun.com/document_detail/44397.html">如何做到消费幂等</a>
     */
    Action consume(final List<Message> messages, final ConsumeContext context);
}
