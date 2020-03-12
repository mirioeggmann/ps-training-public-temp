package net.corda.training.webserver;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NodeRPCConnection {
    private static final String CORDA_USER_NAME = "config.rpc.username";
    private static final String CORDA_USER_PASSWORD = "config.rpc.password";
    private static final String CORDA_NODE_HOST = "config.rpc.host";
    private static final String CORDA_RPC_PORT = "config.rpc.port";

    @NotNull
    private CordaRPCConnection rpcConnection;
    @NotNull
    private CordaRPCOps proxy;
    private final String host;
    private final String username;
    private final String password;
    private final int rpcPort;

    @NotNull
    public final CordaRPCConnection getRpcConnection() {
        return this.rpcConnection;
    }

    @NotNull
    public final CordaRPCOps getProxy() {
        return this.proxy;
    }

    @PostConstruct
    public final void initialiseNodeRPCConnection() {
        NetworkHostAndPort rpcAddress = new NetworkHostAndPort(this.host, this.rpcPort);
        CordaRPCClient rpcClient = new CordaRPCClient(rpcAddress);
        CordaRPCConnection rpcConnection = rpcClient.start(this.username, this.password);
        this.proxy = rpcConnection.getProxy();
    }

    @PreDestroy
    public final void close() {
        this.rpcConnection.notifyServerAndClose();
    }

    public NodeRPCConnection(@Value("${config.rpc.host}") @NotNull String host,
                             @Value("${config.rpc.username}") @NotNull String username,
                             @Value("${config.rpc.password}") @NotNull String password,
                             @Value("${config.rpc.port}") int rpcPort) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.rpcPort = rpcPort;
    }
}
