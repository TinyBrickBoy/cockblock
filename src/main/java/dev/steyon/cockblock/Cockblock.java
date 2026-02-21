package dev.steyon.cockblock;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.steyon.cockblock.command.CockblockCommand;
import dev.steyon.cockblock.config.Config;
import dev.steyon.cockblock.listener.DomainBlocklistListener;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(
        id = "cockblock",
        name = "Cockblock",
        version = BuildConstants.VERSION,
        description = "Blocks or whitelists players based on the domain they connect with.",
        url = "https://mcwith.me/",
        authors = {"TinyBrickBoy"}
)
public class Cockblock {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;

    @Inject
    @DataDirectory
    private Path dataDirectory;

    private Config config;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        config = new Config(dataDirectory);

        try {
            config.load();
        } catch (IOException e) {
            logger.error("Konnte die Config nicht laden!", e);
            return;
        }

        // Register event listener
        server.getEventManager().register(this, new DomainBlocklistListener(config));

        // Register /cockblock command
        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("cockblock").plugin(this).build();
        commandManager.register(meta, new CockblockCommand(config, logger));

        if (config.isWhitelistMode()) {
            logger.info("Cockblock aktiv im WHITELIST-Modus mit {} erlaubten Domains.",
                    config.getAllowedDomains().size());
        } else {
            logger.info("Cockblock aktiv im BLOCKLIST-Modus mit {} blockierten Domains.",
                    config.getBlockedDomains().size());
        }
    }
}
