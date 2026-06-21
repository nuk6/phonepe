package org.example.comparison;

/**
 * SPRING + TOMCAT SERVER (Thread-Per-Request)
 *
 * How it works under the hood:
 *   1. Tomcat uses NIO (epoll/kqueue) to ACCEPT connections efficiently
 *   2. But then hands each request to a DEDICATED thread from the pool
 *   3. That thread BLOCKS on every I/O call (DB, HTTP, file read)
 *   4. Thread sits idle during I/O — wastes memory
 *
 * Default: 200 threads → max 200 concurrent requests
 *
 * ⚠️ THREAD SAFETY POINTS:
 *   - Controller methods: called from DIFFERENT threads simultaneously
 *     → instance fields are SHARED → must be thread-safe
 *   - Spring beans are SINGLETONS by default
 *     → one instance, many threads → no mutable state!
 *   - @Autowired services: also singletons → same rule
 *
 * WHEN TO USE SPRING:
 *   ✅ CPU-heavy work (each thread gets its own CPU time slice)
 *   ✅ Simple blocking code (easy to read/debug)
 *   ✅ Moderate concurrency (< 10K concurrent)
 *   ✅ JDBC (blocking by nature — no async MySQL/Postgres driver in mainstream)
 *   ❌ 100K+ concurrent connections (too many threads)
 */

// import org.springframework.web.bind.annotation.*;
// import java.util.concurrent.atomic.AtomicLong;

// @RestController
public class SpringServer {

    // ⚠️ THREAD SAFETY: AtomicLong because 200 threads increment this
    // If this were a plain `long counter` → race condition → lost updates
    // private final AtomicLong counter = new AtomicLong(0);

    // ⚠️ THREAD SAFETY: This method runs on 200 DIFFERENT threads
    // Local variables (sum) are on the thread's stack → safe
    // Instance fields (counter) are shared → must be atomic/synchronized
    // @GetMapping("/api/hello")
    public String hello() {
        // counter.incrementAndGet();  // ⚠️ atomic — thread safe
        // long sum = 0;              // local variable — each thread has its own
        // for (long i = 0; i < 10_000_000; i++) sum += i;  // CPU work: fine in Spring!
        //                            // This thread is dedicated to this request
        //                            // Other 199 threads handle other requests
        // return "{\"result\":" + sum + "}";
        return "{}";
    }

    // @GetMapping("/api/io")
    public String ioHeavy() {
        // Thread 1 calls this:
        //   User user = userRepo.findById(id);  ← Thread 1 BLOCKS here for 50ms
        //   // Thread 1 is sleeping, doing NOTHING, using 1MB RAM
        //   // But Thread 2-200 can still serve other requests
        //
        // This is WHY Spring needs 200 threads:
        //   200 concurrent DB calls = 200 blocked threads = 200MB RAM
        //   With Vert.x: 200 concurrent DB calls = 200 callbacks = 100KB RAM
        return "{}";
    }

    /*
     * WHAT HAPPENS UNDER THE HOOD (Tomcat NIO):
     *
     * Tomcat Acceptor Thread (1 thread, uses epoll/kqueue):
     *   → epoll_wait() — same as our C++ server!
     *   → new connection arrives
     *   → Tomcat DOES NOT handle it on the event loop
     *   → Instead: hands it to a thread from the pool
     *
     * Thread Pool (200 threads):
     *   Thread-42 picks up the request
     *   → reads HTTP body (blocking)
     *   → calls your @GetMapping method
     *   → your method calls DB (blocking — thread sleeps)
     *   → DB responds → thread wakes up
     *   → your method returns
     *   → Thread-42 writes HTTP response
     *   → Thread-42 goes back to pool
     *
     * So Tomcat uses epoll for ACCEPTING, but blocking threads for PROCESSING
     * It's a hybrid — not fully event-driven like Vert.x/Node
     */
}

