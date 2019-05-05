/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.aliyun.openservices.shade.io.netty.channel.udt;

import com.aliyun.openservices.shade.io.netty.channel.Channel;
import com.aliyun.openservices.shade.io.netty.channel.udt.nio.NioUdtProvider;

import java.net.InetSocketAddress;

/**
 * UDT {@link Channel}.
 * <p>
 * Supported UDT {@link UdtChannel} are available via {@link NioUdtProvider}.
 */
public interface UdtChannel extends Channel {

    /**
     * Returns the {@link UdtChannelConfig} of the channel.
     */
    @Override
    UdtChannelConfig config();

    @Override
    InetSocketAddress localAddress();
    @Override
    InetSocketAddress remoteAddress();

}
