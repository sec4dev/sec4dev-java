package com.sec4dev;

import com.sec4dev.models.EmailCheckResult;
import com.sec4dev.models.IPCheckResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import static org.junit.jupiter.api.Assertions.*;

class Sec4DevClientTest {

    private HttpServer server;
    private int port;
    private Sec4DevClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());

        server.createContext("/api/v1/email/check", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String body = "{\"email\":\"user@tempmail.com\",\"domain\":\"tempmail.com\",\"is_disposable\":true}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        });

        server.createContext("/api/v1/ip/check", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String body = "{\"ip\":\"203.0.113.42\",\"classification\":\"hosting\",\"confidence\":0.95,"
                    + "\"signals\":{\"is_hosting\":true,\"is_residential\":false,\"is_mobile\":false,\"is_vpn\":false,\"is_tor\":false,\"is_proxy\":false},"
                    + "\"network\":{\"asn\":16509,\"org\":\"Amazon.com, Inc.\",\"provider\":\"AWS\"},"
                    + "\"geo\":{\"country\":\"US\",\"region\":null}}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        });

        server.start();
        port = server.getAddress().getPort();
        String baseUrl = "http://localhost:" + port + "/api/v1";
        client = Sec4DevClient.builder()
                .apiKey("sec4_test_key")
                .baseUrl(baseUrl)
                .retries(0)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void clientRejectsInvalidApiKey() {
        assertThrows(ValidationException.class, () ->
                Sec4DevClient.builder().apiKey("").build());
        assertThrows(ValidationException.class, () ->
                Sec4DevClient.builder().apiKey("invalid").build());
    }

    @Test
    void clientAcceptsValidApiKey() {
        Sec4DevClient c = Sec4DevClient.builder().apiKey("sec4_abc").baseUrl("https://api.example.com/v1").build();
        assertNotNull(c.getEmail());
        assertNotNull(c.getIp());
    }

    @Test
    void emailCheckReturnsResult() {
        EmailCheckResult result = client.getEmail().check("user@tempmail.com");
        assertEquals("user@tempmail.com", result.getEmail());
        assertEquals("tempmail.com", result.getDomain());
        assertTrue(result.isDisposable());
    }

    @Test
    void emailIsDisposable() {
        assertTrue(client.getEmail().isDisposable("user@tempmail.com"));
    }

    @Test
    void emailCheckValidatesInput() {
        assertThrows(ValidationException.class, () -> client.getEmail().check(""));
        assertThrows(ValidationException.class, () -> client.getEmail().check("not-an-email"));
    }

    @Test
    void ipCheckReturnsResult() {
        IPCheckResult result = client.getIp().check("203.0.113.42");
        assertEquals("203.0.113.42", result.getIp());
        assertEquals("hosting", result.getClassification());
        assertEquals(0.95, result.getConfidence(), 0.001);
        assertTrue(result.getSignals().isHosting());
        assertFalse(result.getSignals().isVpn());
        assertEquals("AWS", result.getNetwork().getProvider());
        assertEquals("US", result.getGeo().getCountry());
    }

    @Test
    void ipIsHosting() {
        assertTrue(client.getIp().isHosting("203.0.113.42"));
    }

    @Test
    void ipCheckValidatesInput() {
        assertThrows(ValidationException.class, () -> client.getIp().check(""));
        assertThrows(ValidationException.class, () -> client.getIp().check("not-an-ip"));
    }
}
