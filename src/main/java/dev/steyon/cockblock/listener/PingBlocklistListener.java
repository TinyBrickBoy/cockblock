package dev.steyon.cockblock.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import dev.steyon.cockblock.config.Config;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Optional;

public class PingBlocklistListener {

    private final Config config;
    private final Logger logger;

    public PingBlocklistListener(Config config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        Optional<InetSocketAddress> virtualHostOpt = event.getConnection().getVirtualHost();
        if (virtualHostOpt.isEmpty()) return;

        String hostname = virtualHostOpt.get().getHostName();

        if (config.isBlocked(hostname)) {
            Channel channel = findChannel(event.getConnection(), 0);
            if (channel == null) {
                logger.warn("Couldn't find Netty channel for ping from {} — Velocity internals may have changed.", hostname);
                return;
            }

            // Inject an outbound handler that drops the status response and closes
            // the connection, so the client sees "no server" instead of a ping reply.
            channel.pipeline().addFirst("cockblock-ping-block", new ChannelOutboundHandlerAdapter() {
                @Override
                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                    ReferenceCountUtil.release(msg);
                    ctx.close();
                    promise.setSuccess();
                }
            });
        }
    }

    /**
     * Recursively searches the fields of obj for a Netty Channel instance.
     * Walks up to 3 levels deep to handle Velocity's internal wrapper chain:
     * InitialInboundConnection → MinecraftConnection → Channel
     */
    private Channel findChannel(Object obj, int depth) {
        if (obj == null || depth > 3) return null;
        for (Field field : obj.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value instanceof Channel) return (Channel) value;
                if (value != null && !value.getClass().getName().startsWith("java")
                        && !value.getClass().isEnum()) {
                    Channel ch = findChannel(value, depth + 1);
                    if (ch != null) return ch;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }
}
