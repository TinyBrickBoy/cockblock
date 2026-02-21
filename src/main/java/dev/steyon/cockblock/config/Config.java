package dev.steyon.cockblock.config;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private List<String> blockedDomains = new ArrayList<>();
    private List<String> allowedDomains = new ArrayList<>();
    private String kickMessage = "<red><bold>Zugriff verweigert</bold></red><newline><gray>Du darfst dich nicht mit dieser Domain verbinden.</gray>";
    private boolean whitelistMode = false;
    private int fakeOnlinePlayers = -1;
    private int fakeMaxPlayers = -1;

    private final Path configPath;

    public Config(Path dataDirectory) {
        this.configPath = dataDirectory.resolve("config.yml");
    }

    public void load() throws IOException {
        Files.createDirectories(configPath.getParent());

        if (!Files.exists(configPath)) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (in != null) {
                    Files.copy(in, configPath);
                }
            }
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(configPath)
                .build();

        ConfigurationNode root = loader.load();

        blockedDomains = root.node("blocked-domains").getList(String.class, new ArrayList<>());
        allowedDomains = root.node("allowed-domains").getList(String.class, new ArrayList<>());
        kickMessage = root.node("kick-message").getString(kickMessage);
        whitelistMode = root.node("whitelist-mode").getBoolean(false);
        fakeOnlinePlayers = root.node("fake-online-players").getInt(-1);
        fakeMaxPlayers = root.node("fake-max-players").getInt(-1);
    }

    /**
     * Returns true if the given hostname should be blocked.
     * In blocklist mode: blocked if the domain is in blocked-domains.
     * In whitelist mode: blocked if the domain is NOT in allowed-domains.
     */
    public boolean isBlocked(String hostname) {
        String host = hostname.toLowerCase();
        if (whitelistMode) {
            return !matchesList(host, allowedDomains);
        } else {
            return matchesList(host, blockedDomains);
        }
    }

    private boolean matchesList(String hostname, List<String> domains) {
        for (String domain : domains) {
            String d = domain.toLowerCase().trim();
            if (hostname.equals(d) || hostname.endsWith("." + d)) {
                return true;
            }
        }
        return false;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public List<String> getBlockedDomains() {
        return blockedDomains;
    }

    public List<String> getAllowedDomains() {
        return allowedDomains;
    }

    public boolean isWhitelistMode() {
        return whitelistMode;
    }

    public int getFakeOnlinePlayers() {
        return fakeOnlinePlayers;
    }

    public int getFakeMaxPlayers() {
        return fakeMaxPlayers;
    }
}
