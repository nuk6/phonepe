// Vert.x Server — built on Netty, which uses epoll (Linux) / kqueue (macOS)
// Netty is the Java equivalent of our C++ epoll code above
//
// Vert.x architecture:
//   Your code → Vert.x → Netty → epoll/kqueue → kernel
//   (high level)                                (low level)

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;

public class VertxServer extends AbstractVerticle {

    public static void main(String[] args) {
        // Creates N event loops (N = CPU cores)
        // Each event loop = 1 thread running epoll_wait() in a loop (like our C++ code)
        Vertx vertx = Vertx.vertx(new VertxOptions()
            .setEventLoopPoolSize(4)    // 4 event loop threads (like 4 copies of our C++ server)
            .setWorkerPoolSize(20)      // 20 worker threads for blocking operations
        );
        vertx.deployVerticle(new VertxServer());
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);

        // ═══════════════════════════════════════════════
        // IO-HEAVY API — perfect for Vert.x
        // Thread is NEVER blocked, handles next request immediately
        // ═══════════════════════════════════════════════
        router.get("/user/:id").handler(ctx -> {
            String id = ctx.pathParam("id");

            // This does NOT block — sends query to DB, returns immediately
            // Under the hood: Netty registers the DB socket with epoll
            // When DB responds → epoll_wait returns → callback fires
            dbClient.getConnection()
                .compose(conn -> conn.preparedQuery("SELECT * FROM users WHERE id = $1")
                    .execute(Tuple.of(id)))
                .onSuccess(rows -> {
                    // This callback runs ON THE EVENT LOOP THREAD
                    // when the DB response arrives
                    ctx.json(rows.iterator().next().toJson());
                    // THREAD SAFETY: no locks needed!
                    // Each request's callback runs sequentially on the event loop
                    // No two callbacks run simultaneously on the SAME event loop
                })
                .onFailure(err -> ctx.response().setStatusCode(500).end(err.getMessage()));

            // Thread is FREE here — already processing next request
            // The DB query is "in flight" — kernel is watching the socket via epoll
        });

        // ═══════════════════════════════════════════════
        // CPU-HEAVY API — MUST offload to worker thread pool
        // If you do this on the event loop, you block ALL other requests
        // ═══════════════════════════════════════════════
        router.get("/compute").handler(ctx -> {

            // executeBlocking = "run this on a WORKER thread, not event loop"
            vertx.executeBlocking(promise -> {
                // This runs on worker thread pool (those 20 threads from config)
                // THREAD SAFETY: if accessing shared state here, YOU need locks
                double result = heavyComputation();
                promise.complete(result);
            }, false, ar -> {
                // This callback runs back on the event loop thread
                if (ar.succeeded()) {
                    ctx.json(ar.result());
                }
            });
        });

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080)
            .onSuccess(s -> System.out.println("Vert.x listening on :8080"));
    }

    private double heavyComputation() {
        // Simulate CPU-heavy work
        double sum = 0;
        for (long i = 0; i < 1_000_000_000L; i++) sum += Math.sqrt(i);
        return sum;
    }
}

// THREAD SAFETY SUMMARY FOR VERT.X:
//
// 1. Event loop callbacks: NO locks needed
//    - Vert.x guarantees: one Verticle's handlers always run on the SAME event loop
//    - So your handlers are effectively single-threaded per Verticle
//
// 2. executeBlocking: YOU handle thread safety
//    - Multiple worker threads may run simultaneously
//    - Shared state (counters, caches) needs synchronization
//
// 3. SharedData / EventBus: Use these for cross-Verticle communication
//    - Vert.x handles the thread safety internally
//
// GOLDEN RULE: Never block the event loop. Ever.
//   - No Thread.sleep()
//   - No JDBC (use async DB drivers)
//   - No synchronized blocks
//   - No CPU-heavy loops
//   If you do → ALL requests on that event loop freeze

