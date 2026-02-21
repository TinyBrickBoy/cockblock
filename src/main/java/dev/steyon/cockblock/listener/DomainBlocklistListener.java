package dev.steyon.cockblock.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import dev.steyon.cockblock.config.Config;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.net.InetSocketAddress;
import java.util.Optional;

public class DomainBlocklistListener {

    private final Config config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DomainBlocklistListener(Config config) {
        this.config = config;
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        Optional<InetSocketAddress> virtualHostOpt = event.getConnection().getVirtualHost();
        if (virtualHostOpt.isEmpty()) return;

        String hostname = virtualHostOpt.get().getHostName();

        if (config.isBlocked(hostname)) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                    miniMessage.deserialize(config.getKickMessage())
            ));
        }
    }
}
