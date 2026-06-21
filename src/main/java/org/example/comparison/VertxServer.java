package org.example.comparison;

/**
 * VERT.X SERVER (Event Loop — same as Node.js but on JVM)
 *
 * How it works under the hood:
 *   1. Uses Netty → which uses epoll/kqueue (SAME as our C++ server!)
 *   2. N event loop threads (N = CPU cores, e.g., 8)
 *   3. Each event loop handles THOUSANDS of connections
 *   4. NEVER blocks — all I/O is async with callbacks/futures
 *
 * ⚠️ THREAD SAFETY POINTS:
 *   - Each "Verticle" is SINGLE-THREADED (bound to one event loop)
 *     → NO synchronization needed within a verticle!
 *     → This is the genius of Vert.x — thread safety by isolation
 *   - Communication between verticles: EventBus (message passing)
 *     → No shared mutable state → no locks
 *   - If you MUST share state: use Vert.x SharedData (concurrent maps)
 *   - Worker verticles: run on worker thread pool (for blocking code)
 *     → SAME thread safety rules as Spring (shared state needs locks)
 *
 * WHEN TO USE VERT.X:
 *   ✅ Massive I/O (100K+ concurrent connections)
 *   ✅ WebSockets, real-time, chat
 *   ✅ API gateway / proxy (just forwarding requests)
 *   ❌ CPU-heavy in event loop (blocks all connections on that loop)
 *   ⚠️ CPU-heavy in worker thread: OK but then you're just Spring with extra steps
 */

// import io.vertx.core.AbstractVerticle;
// import io.vertx.core.Vertx;
// import io.vertx.ext.web.Router;

public class VertxServer /* extends AbstractVerticle */ {

    // ⚠️ THREAD SAFETY: this field is accessed ONLY by this verticle's
    //    event loop thread. NO lock needed. This is the Vert.x magic.
    private int counter = 0;  // plain int! not AtomicLong!

    // public void start() {
    //     Router router = Router.router(vertx);
    //
    //     // I/O HEAVY — perfect for Vert.x
    //     router.get("/api/io").handler(ctx -> {
    //         counter++;  // ⚠️ safe! single event loop thread
    //
    //         // Non-blocking DB call — thread is FREE immediately
    //         pgClient.query("SELECT * FROM users WHERE id=$1")
    //             .execute(Tuple.of(userId))
    //             .onSuccess(rows -> {
    //                 // This callback runs on the SAME event loop thread
    //                 // when the DB response arrives
    //                 ctx.json(rows);
    //                 // Thread immediately goes to handle next event
    //             })
    //             .onFailure(ctx::fail);
    //
    //         // Thread is HERE immediately — not waiting for DB!
    //         // Already handling the next request
    //     });
    //
    //     // CPU HEAVY — BAD on event loop, offload to worker
    //     router.get("/api/compute").handler(ctx -> {
    //
    //         // ❌ BAD — blocks event loop, all connections freeze:
    //         // long sum = 0;
    //         // for (long i = 0; i < 10_000_000; i++) sum += i;
    //
    //         // ✅ GOOD — offload to worker thread pool:
    //         vertx.executeBlocking(() -> {
    //             // ⚠️ THREAD SAFETY: this runs on a WORKER thread
    //             //    NOT on the event loop thread
    //             //    If accessing shared state here → need synchronization
    //             long sum = 0;
    //             for (long i = 0; i < 10_000_000; i++) sum += i;
    //             return sum;
    //         }).onSuccess(result -> {
    //             // Back on event loop thread — safe again
    //             ctx.json(Map.of("result", result));
    //         });
    //     });
    //
    //     vertx.createHttpServer()
    //         .requestHandler(router)
    //         .listen(8080);
    // }
    //
    // public static void main(String[] args) {
    //     Vertx vertx = Vertx.vertx();
    //     // Deploys N instances (N = CPU cores)
    //     // Each instance gets its own event loop thread
    //     // Each has its own `counter` — no sharing!
    //     vertx.deployVerticle(VertxServer.class.getName(),
    //         new DeploymentOptions().setInstances(
    //             Runtime.getRuntime().availableProcessors()
    //         ));
    // }

    /*
     * WHAT HAPPENS UNDER THE HOOD:
     *
     * Vert.x creates Netty EventLoopGroup (8 threads on 8-core machine)
     *
     * EventLoop-0: epoll_wait()  ← SAME as our C++ server!
     *   → connection on fd=42 has data
     *   → read HTTP request
     *   → call your handler
     *   → handler calls DB (non-blocking — returns Future)
     *   → register callback in Netty's task queue (= our C++ connections map)
     *   → go back to epoll_wait()
     *   → ... handle 5000 other requests ...
     *   → DB response arrives on fd=78
     *   → epoll_wait() returns
     *   → find callback → execute → write HTTP response → done
     *
     * Netty's epoll loop is literally our C++ code but more sophisticated:
     *   - edge-triggered vs level-triggered
     *   - zero-copy buffers
     *   - pipeline handlers (codec, SSL, compression)
     *   - memory pooling (PooledByteBufAllocator)
     */
}

