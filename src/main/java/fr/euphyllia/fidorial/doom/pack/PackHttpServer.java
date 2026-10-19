package fr.euphyllia.fidorial.doom.pack;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A tiny HTTP server handing the resource pack to clients, which only download packs from a URL.
 */
public final class PackHttpServer implements AutoCloseable {

    private final HttpServer server;
    private final ExecutorService executor;
    private volatile byte[] zip = new byte[0];
    private volatile String path = "/";

    /**
     * Starts listening.
     *
     * @param bind the address and port to listen on
     * @throws IOException if the port cannot be bound
     */
    public PackHttpServer(final InetSocketAddress bind) throws IOException {
        server = HttpServer.create(bind, 16);
        executor = Executors.newFixedThreadPool(2, r -> {
            final Thread t = new Thread(r, "doom-pack-http");
            t.setDaemon(true);
            return t;
        });
        server.setExecutor(executor);
        server.createContext("/", this::handle);
        server.start();
    }

    /**
     * Serves a pack.
     *
     * @param pack the pack
     * @return the path it is served at, such as {@code /doom-<sha1>.zip}
     */
    public String serve(final ResourcePackBuilder.Pack pack) {
        zip = pack.zip();
        path = "/doom-" + pack.sha1() + ".zip";
        return path;
    }

    private void handle(final HttpExchange exchange) throws IOException {
        try (exchange) {
            final byte[] body = zip;
            if (!exchange.getRequestURI().getPath().equals(path) || body.length == 0) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            if (exchange.getRequestMethod().equalsIgnoreCase("HEAD")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            exchange.sendResponseHeaders(200, body.length);
            try (final OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
    }

    @Override
    public void close() {
        server.stop(0);
        executor.shutdownNow();
    }
}
