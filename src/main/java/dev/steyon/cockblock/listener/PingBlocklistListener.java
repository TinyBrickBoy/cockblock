package dev.steyon.cockblock.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import dev.steyon.cockblock.config.Config;
import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;
import java.util.Optional;

public class PingBlocklistListener {

    private final Config config;

    public PingBlocklistListener(Config config) {
        this.config = config;
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        Optional<InetSocketAddress> virtualHostOpt = event.getConnection().getVirtualHost();
        if (virtualHostOpt.isEmpty()) return;

        String hostname = virtualHostOpt.get().getHostName();

        if (config.isBlocked(hostname)) {
            ServerPing modified = event.getPing().asBuilder()
                    .description(Component.empty())
                    .clearFavicon()
                    .nullPlayers()
                    .build();
            event.setPing(modified);
        }
    }
}
