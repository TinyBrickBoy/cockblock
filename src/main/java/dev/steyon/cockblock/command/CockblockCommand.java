package dev.steyon.cockblock.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.steyon.cockblock.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.io.IOException;

public class CockblockCommand implements SimpleCommand {

    private static final String PERMISSION = "cockblock.admin";

    private final Config config;
    private final Logger logger;

    public CockblockCommand(Config config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            try {
                config.load();
                invocation.source().sendMessage(
                        Component.text("[Cockblock] Config erfolgreich neu geladen.", NamedTextColor.GREEN)
                );
                logger.info("Config wurde neu geladen.");
            } catch (IOException e) {
                invocation.source().sendMessage(
                        Component.text("[Cockblock] Fehler beim Laden der Config: " + e.getMessage(), NamedTextColor.RED)
                );
                logger.error("Fehler beim Laden der Config.", e);
            }
            return;
        }

        invocation.source().sendMessage(
                Component.text("[Cockblock] Verwendung: /cockblock reload", NamedTextColor.YELLOW)
        );
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(PERMISSION);
    }
}
