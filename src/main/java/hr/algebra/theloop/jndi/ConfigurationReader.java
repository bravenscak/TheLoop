package hr.algebra.theloop.jndi;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationReader {

    private ConfigurationReader() {}

    private static final ConcurrentHashMap<String, Object> jndiStore = new ConcurrentHashMap<>();
    private static boolean jndiEnabled = true;

    static {
        initializeConfiguration();
    }

    private static void initializeConfiguration() {
        try {
            jndiStore.put("hostname", "localhost");
            jndiStore.put("rmi.server.port", "1099");
            jndiStore.put("chat.server.port", "1099");
            jndiStore.put("player.one.server.port", "12345");
            jndiStore.put("player.two.server.port", "12346");
            jndiStore.put("connection.timeout", "5000");
        } catch (Exception e) {
            jndiEnabled = false;
        }
    }

    public static String getStringValueForKey(ConfigurationKey key) {
        if (jndiEnabled && jndiStore.containsKey(key.getKey())) {
            Object value = jndiStore.get(key.getKey());
            if (value != null) {
                return value.toString();
            }
        }
        return getDefaultStringValue(key);
    }

    public static Integer getIntegerValueForKey(ConfigurationKey key) {
        String value = getStringValueForKey(key);
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return getDefaultIntegerValue(key);
        }
    }

    public static Object lookup(String name) throws NamingException {
        if (!jndiEnabled) {
            throw new NamingException("JNDI not available");
        }

        Object value = jndiStore.get(name);
        if (value == null) {
            throw new NamingException("Name not found: " + name);
        }
        return value;
    }

    public static void bind(String name, Object value) throws NamingException {
        if (!jndiEnabled) {
            throw new NamingException("JNDI not available");
        }

        if (jndiStore.containsKey(name)) {
            throw new NamingException("Name already bound: " + name);
        }
        jndiStore.put(name, value);
    }

    public static void rebind(String name, Object value) throws NamingException {
        if (!jndiEnabled) {
            throw new NamingException("JNDI not available");
        }
        jndiStore.put(name, value);
    }

    public static void unbind(String name) throws NamingException {
        if (!jndiEnabled) {
            throw new NamingException("JNDI not available");
        }

        Object removed = jndiStore.remove(name);
        if (removed == null) {
            throw new NamingException("Name not found: " + name);
        }
    }

    public static void updateConfiguration(ConfigurationKey key, String value) {
        try {
            rebind(key.getKey(), value);
        } catch (Exception e) {
        }
    }

    private static String getDefaultStringValue(ConfigurationKey key) {
        return switch (key) {
            case HOSTNAME -> "localhost";
            default -> "unknown";
        };
    }

    private static Integer getDefaultIntegerValue(ConfigurationKey key) {
        return switch (key) {
            case PLAYER_ONE_SERVER_PORT -> 12345;
            case PLAYER_TWO_SERVER_PORT -> 12346;
            case CHAT_SERVER_PORT, RMI_PORT -> 1099;
            case CONNECTION_TIMEOUT -> 5000;
            default -> 0;
        };
    }
}
