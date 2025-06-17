package hr.algebra.theloop.jndi;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

public class InitialDirContextCloseable extends InitialDirContext implements AutoCloseable {

    public InitialDirContextCloseable(Hashtable<?, ?> environment) throws NamingException {
        super(environment);
    }

    public InitialDirContextCloseable() throws NamingException {
        this(getDefaultEnvironment());
    }

    private static Hashtable<String, String> getDefaultEnvironment() {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        env.put(Context.PROVIDER_URL, "rmi://localhost:1099");
        return env;
    }

    @Override
    public void close() throws NamingException {
        super.close();
    }
}