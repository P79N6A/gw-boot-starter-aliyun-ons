package com.aliyun.openservices.ons.api.impl;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.LocalTransactionState;
import com.aliyun.openservices.ons.api.Constants;
import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactoryAPI;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.batch.BatchConsumer;
import com.aliyun.openservices.ons.api.impl.rocketmq.BatchConsumerImpl;
import com.aliyun.openservices.ons.api.impl.rocketmq.ConsumerImpl;
import com.aliyun.openservices.ons.api.impl.rocketmq.ONSUtil;
import com.aliyun.openservices.ons.api.impl.rocketmq.OrderConsumerImpl;
import com.aliyun.openservices.ons.api.impl.rocketmq.OrderProducerImpl;
import com.aliyun.openservices.ons.api.impl.rocketmq.ProducerImpl;
import com.aliyun.openservices.ons.api.impl.rocketmq.TransactionProducerImpl;
import com.aliyun.openservices.ons.api.order.OrderConsumer;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.aliyun.openservices.ons.api.transaction.TransactionProducer;
import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
import java.util.Properties;


public class ONSFactoryImpl implements ONSFactoryAPI {
    @Override
    public Producer createProducer(final Properties properties) {
        return new ProducerImpl(ONSUtil.extractProperties(properties));
    }


    @Override
    public Consumer createConsumer(final Properties properties) {
        return new ConsumerImpl(ONSUtil.extractProperties(properties));
    }

    @Override
    public BatchConsumer createBatchConsumer(final Properties properties) {
        return new BatchConsumerImpl(ONSUtil.extractProperties(properties));
    }

    @Override
    public OrderProducer createOrderProducer(final Properties properties) {
        return new OrderProducerImpl(ONSUtil.extractProperties(properties));
    }


    @Override
    public OrderConsumer createOrderedConsumer(final Properties properties) {
        return new OrderConsumerImpl(ONSUtil.extractProperties(properties));
    }


    @Override
    public TransactionProducer createTransactionProducer(Properties properties,
                                                         final LocalTransactionChecker checker) {
        return new TransactionProducerImpl(ONSUtil.extractProperties(properties), msg -> {
            String msgId = msg.getProperty(Constants.TRANSACTION_ID);
            Message message = ONSUtil.msgConvert(msg);
            message.setMsgID(msgId);
            TransactionStatus check = checker.check(message);
            if (TransactionStatus.CommitTransaction == check) {
                return LocalTransactionState.COMMIT_MESSAGE;
            } else if (TransactionStatus.RollbackTransaction == check) {
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
            return LocalTransactionState.UNKNOW;
        });
    }
}