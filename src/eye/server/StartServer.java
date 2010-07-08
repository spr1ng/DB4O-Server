package eye.server;

import com.db4o.*;
import com.db4o.cs.*;
import com.db4o.cs.config.*;
import com.db4o.messaging.*;
import java.util.Date;

/**
 * starts a db4o server with the settings from {@link ServerInfo}. <br>
 * <br>
 * This is a typical setup for a long running server. <br>
 * <br>
 * The StartServer may be stopped from a remote location by running StopServer. The
 * StartServer instance is used as a MessageRecipient and reacts to receiving an
 * instance of a StopServer object. <br>
 * <br>
 * Note that all user classes need to be present on the server side and that all
 * possible Db4o.configure() calls to alter the db4o configuration need to be
 * executed on the client and on the server.
 * @version $Id: StartServer.java 57 2010-07-08 03:35:18Z spr1ng $
 */
public class StartServer implements MessageRecipient {

    /** setting the value to true denotes that the server should be closed */
    private boolean stop = false;
    private static ConfigLoader conf = ConfigLoader.getInstance();
    
    /**
     * starts a db4o server using the configuration from {
     * @link www.db4o.com ServerInfo}.
     */
    public static void main(String[] arguments) {
        new StartServer().runServer();
    }

    /**
     * opens the ObjectServer, and waits forever until close() is called or a
     * StopServer message is being received.
     */
    public void runServer() {
        synchronized (this) {
            ServerConfiguration config =
                    Db4oClientServer.newServerConfiguration();
            // Using the messaging functionality to redirect all
            // messages to this.processMessage
            config.networking().messageRecipient(this);
            ObjectServer db4oServer =
                    Db4oClientServer.openServer(config, conf.getFile(), conf.getPort());
            db4oServer.grantAccess(conf.getUser(), conf.getPass());
            // to identify the thread in a debugger
            Thread.currentThread().setName(this.getClass().getName());
            // We only need low priority since the db4o server has
            // it's own thread.
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            try {
                System.out.println("[" + conf.getHost() + ":" + conf.getPort()
                                       + "] DB4o server started: " + new Date());
                if (!stop) {
                    // wait forever for notify() from close()
                    this.wait(Long.MAX_VALUE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("[" + conf.getHost() + ":" + conf.getPort()
                                       + "] DB4o server stopped: " + new Date());
            db4oServer.close();
        }
    }

    /**
     * messaging callback www.db4o.com
     * @see com.db4o.messaging.MessageRecipient#processMessage(MessageContext,
     * Object)
     */
    public void processMessage(MessageContext con, Object message) {
        if (message instanceof StopServer) {
            close();
        }
    }

    /** closes this server */
    public void close() {
        synchronized (this) {
            stop = true;
            this.notify();
        }
    }
}
