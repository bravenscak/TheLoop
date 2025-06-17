package hr.algebra.theloop.jndi;

public enum ConfigurationKey {

    PLAYER_ONE_SERVER_PORT("player.one.server.port"),
    PLAYER_TWO_SERVER_PORT("player.two.server.port"),
    CHAT_SERVER_PORT("chat.server.port"),
    HOSTNAME("hostname"),
    RMI_PORT("rmi.server.port"),
    CONNECTION_TIMEOUT("connection.timeout");

    private final String key;

    ConfigurationKey(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}