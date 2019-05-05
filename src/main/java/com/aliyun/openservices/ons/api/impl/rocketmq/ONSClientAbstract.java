package com.aliyun.openservices.ons.api.impl.rocketmq;

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Generated;

import com.alibaba.ons.open.trace.core.dispatch.AsyncDispatcher;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.exception.MQClientException;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.UtilAll;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.namesrv.TopAddressing;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.logging.InternalLogger;

import com.aliyun.openservices.ons.api.Admin;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import com.aliyun.openservices.ons.api.impl.authority.SessionCredentials;
import com.aliyun.openservices.ons.api.impl.util.ClientLoggerUtil;
import com.aliyun.openservices.ons.api.impl.util.NameAddrUtils;
import com.aliyun.openservices.shade.org.apache.commons.lang3.StringUtils;

import static com.aliyun.openservices.shade.com.alibaba.rocketmq.common.UtilAll.getPid;

@Generated("ons-client")
public abstract class ONSClientAbstract implements Admin {
    /**
     * 内网地址服务器
     */
    protected static final String WSADDR_INTERNAL = System.getProperty("com.aliyun.openservices.ons.addr.internal",
        "http://onsaddr-internal.aliyun.com:8080/rocketmq/nsaddr4client-internal");
    /**
     * 公网地址服务器
     */
    protected static final String WSADDR_INTERNET = System.getProperty("com.aliyun.openservices.ons.addr.internet",
        "http://onsaddr-internet.aliyun.com/rocketmq/nsaddr4client-internet");
    protected static final long WSADDR_INTERNAL_TIMEOUTMILLS =
        Long.parseLong(System.getProperty("com.aliyun.openservices.ons.addr.internal.timeoutmills", "3000"));
    protected static final long WSADDR_INTERNET_TIMEOUTMILLS =
            Long.parseLong(System.getProperty("com.aliyun.openservices.ons.addr.internet.timeoutmills", "5000"));
    private final static InternalLogger LOGGER = ClientLoggerUtil.getClientLogger();
    protected final Properties properties;
    protected final SessionCredentials sessionCredentials = new SessionCredentials();
    protected String nameServerAddr = NameAddrUtils.getNameAdd();

    protected AsyncDispatcher traceDispatcher = null;

    protected final AtomicBoolean started = new AtomicBoolean(false);

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1,
        new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "ONSClient-UpdateNameServerThread");
            }
        });

    public ONSClientAbstract(Properties properties) {
        this.properties = properties;
        this.sessionCredentials.updateContent(properties);
        // 检测必须的参数
        if (null == this.sessionCredentials.getAccessKey() || "".equals(this.sessionCredentials.getAccessKey())) {
            throw new ONSClientException("please set access key");
        }

        if (null == this.sessionCredentials.getSecretKey() || "".equals(this.sessionCredentials.getSecretKey())) {
            throw new ONSClientException("please set secret key");
        }

        if (null == this.sessionCredentials.getOnsChannel()) {
            throw new ONSClientException("please set ons channel");
        }

        // 用户指定了Name Server
        // 私有云模式有可能需要
        this.nameServerAddr = getNameSrvAddrFromProperties();
        if (nameServerAddr != null) {
            return;
        }
        this.nameServerAddr = fetchNameServerAddr();
        if (null == nameServerAddr) {
            throw new ONSClientException(FAQ.errorMessage("Can not find name server, May be your network problem.", FAQ.FIND_NS_FAILED));
        }

        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    String nsAddrs = fetchNameServerAddr();
                    if (nsAddrs != null && !ONSClientAbstract.this.nameServerAddr.equals(nsAddrs)) {
                        ONSClientAbstract.this.nameServerAddr = nsAddrs;
                        if (isStarted()) {
                            updateNameServerAddr(nsAddrs);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("update name server periodically failed.", e);
                }
            }
        }, 10 * 1000L, 30 * 1000L, TimeUnit.MILLISECONDS);

    }

    protected abstract void updateNameServerAddr(String newAddrs);

    private String getNameSrvAddrFromProperties() {
        String nameserverAddrs = this.properties.getProperty(PropertyKeyConst.NAMESRV_ADDR);
        if (StringUtils.isNotEmpty(nameserverAddrs) && NameAddrUtils.NAMESRV_ENDPOINT_PATTERN.matcher(nameserverAddrs.trim()).matches()) {
            return nameserverAddrs.substring(NameAddrUtils.ENDPOINT_PREFIX.length());
        }

        return nameserverAddrs;
    }

    private String fetchNameServerAddr() {
        String nsAddrs;

        // 用户指定了地址服务器
        {
            String property = this.properties.getProperty(PropertyKeyConst.ONSAddr);
            if (property != null) {
                nsAddrs = new TopAddressing(property).fetchNSAddr();
                if (nsAddrs != null) {
                    LOGGER.info("connected to user-defined ons addr server, {} success, {}", property, nsAddrs);
                    return nsAddrs;
                } else {
                    throw new ONSClientException(FAQ.errorMessage("Can not find name server with onsAddr " + property, FAQ.FIND_NS_FAILED));
                }
            }
        }

        // 用户未指定，默认访问内网地址服务器
        {
            TopAddressing top = new TopAddressing(WSADDR_INTERNAL);
            nsAddrs = top.fetchNSAddr(false, WSADDR_INTERNAL_TIMEOUTMILLS);
            if (nsAddrs != null) {
                LOGGER.info("connected to internal server, {} success, {}", WSADDR_INTERNAL, nsAddrs);
                return nsAddrs;
            }
        }

        // 用户未指定，然后访问公网地址服务器
        {
            TopAddressing top = new TopAddressing(WSADDR_INTERNET);
            nsAddrs = top.fetchNSAddr(false, WSADDR_INTERNET_TIMEOUTMILLS);
            if (nsAddrs != null) {
                LOGGER.info("connected to internet server, {} success, {}", WSADDR_INTERNET, nsAddrs);
            }
        }

        return nsAddrs;
    }

    public String getNameServerAddr() {
        return this.nameServerAddr;
    }

    protected String buildIntanceName() {
        return Integer.toString(UtilAll.getPid())
            + "#" + this.nameServerAddr.hashCode()
            + "#" + this.sessionCredentials.getAccessKey().hashCode()
            + "#" + System.nanoTime();
    }

    protected String getNamespace() {
        String namespace = null;

        // 用户指定了Endpoint(Namesrv地址的泛域名)
        {
            String nameserverAddr = this.properties.getProperty(PropertyKeyConst.NAMESRV_ADDR);
            if (StringUtils.isNotEmpty(nameserverAddr)) {
                if (NameAddrUtils.validateInstanceEndpoint(nameserverAddr)) {
                    namespace = NameAddrUtils.parseInstanceIdFromEndpoint(nameserverAddr);
                    LOGGER.info("User specify namespace by endpoint: {}.", namespace);
                }
            }
        }

        // 用户通过Properties透传的namespace，如果同时指定，Properties中的配置优先级更高
        {
            String namespaceFromProperty = this.properties.getProperty(PropertyKeyConst.INSTANCE_ID, null);
            if (StringUtils.isNotEmpty(namespaceFromProperty)) {
                namespace = namespaceFromProperty;
                LOGGER.info("User specify namespace by property: {}.", namespace);
            }
        }

        return namespace;
    }

    protected void checkONSProducerServiceState(DefaultMQProducerImpl producer) {
        switch (producer.getServiceState()) {
            case CREATE_JUST:
                throw new ONSClientException(
                    FAQ.errorMessage(String.format("You do not have start the producer[" + getPid() + "], %s", producer.getServiceState()),
                        FAQ.SERVICE_STATE_WRONG));
            case SHUTDOWN_ALREADY:
                throw new ONSClientException(FAQ.errorMessage(String.format("Your producer has been shut down, %s", producer.getServiceState()),
                    FAQ.SERVICE_STATE_WRONG));
            case START_FAILED:
                throw new ONSClientException(FAQ.errorMessage(
                    String.format("When you start your service throws an exception, %s", producer.getServiceState()), FAQ.SERVICE_STATE_WRONG));
            case RUNNING:
                break;
            default:
                break;
        }
    }

    @Override
    public void start() {
        if (null != traceDispatcher) {
            try {
                traceDispatcher.start();
            } catch (MQClientException e) {
                LOGGER.warn("trace dispatcher start failed ", e);
            }
        }
    }

    @Override
    public void updateCredential(Properties credentialProperties) {
        if (null == credentialProperties.getProperty(SessionCredentials.AccessKey)
                || "".equals(credentialProperties.getProperty(SessionCredentials.AccessKey))) {
            throw new ONSClientException("update credential failed. please set access key.");
        }

        if (null == credentialProperties.getProperty(SessionCredentials.SecretKey)
                || "".equals(credentialProperties.getProperty(SessionCredentials.SecretKey))) {
            throw new ONSClientException("update credential failed. please set secret key");
        }
        this.sessionCredentials.updateContent(credentialProperties);
    }

    @Override
    public void shutdown() {
        if (null != traceDispatcher) {
            traceDispatcher.shutdown();
        }
        scheduledExecutorService.shutdown();
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }

    @Override
    public boolean isClosed() {
        return !isStarted();
    }
}
