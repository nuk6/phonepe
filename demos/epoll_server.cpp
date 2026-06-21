// Raw epoll server in C++ — THIS is what Vert.x/Node.js do under the hood
// Linux only (epoll is a Linux kernel feature)
// macOS equivalent = kqueue, Windows = IOCP

#include <sys/epoll.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <fcntl.h>
#include <cstring>
#include <cstdio>

// Make socket non-blocking — KEY to the entire model
// Without this, read()/write()/accept() would BLOCK the thread
void make_non_blocking(int fd) {
    int flags = fcntl(fd, F_GETFL, 0);
    fcntl(fd, F_SETFL, flags | O_NONBLOCK);
}

int main() {
    // 1. Create server socket
    int server_fd = socket(AF_INET, SOCK_STREAM, 0);
    int opt = 1;
    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
    make_non_blocking(server_fd);

    sockaddr_in addr{};
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = INADDR_ANY;
    addr.sin_port = htons(8080);
    bind(server_fd, (sockaddr*)&addr, sizeof(addr));
    listen(server_fd, 1024);  // backlog queue = 1024

    // 2. Create epoll instance — THIS IS THE MAGIC
    //    epoll = "hey kernel, watch these file descriptors for me"
    int epoll_fd = epoll_create1(0);

    // 3. Tell kernel: "notify me when server_fd has incoming connections"
    epoll_event event{};
    event.events = EPOLLIN;       // EPOLLIN = "data ready to READ"
    event.data.fd = server_fd;
    epoll_ctl(epoll_fd, EPOLL_CTL_ADD, server_fd, &event);

    epoll_event events[1024];     // buffer to receive ready events
    const char* response = "HTTP/1.1 200 OK\r\nContent-Length: 13\r\n\r\nHello, World!";

    printf("Server listening on :8080\n");

    // 4. THE EVENT LOOP — single thread handles everything
    while (true) {
        // BLOCK here until at least 1 fd is ready (or timeout)
        // This is the ONLY place the thread ever waits
        int n = epoll_wait(epoll_fd, events, 1024, -1);

        for (int i = 0; i < n; i++) {
            if (events[i].data.fd == server_fd) {
                // --- NEW CONNECTION ---
                // accept() returns immediately because socket is non-blocking
                while (true) {
                    int client_fd = accept(server_fd, nullptr, nullptr);
                    if (client_fd < 0) break;  // no more pending connections

                    make_non_blocking(client_fd);

                    // "REGISTER CALLBACK" = tell kernel to watch this client too
                    // This is what Node.js does when you call .on('data', callback)
                    epoll_event client_event{};
                    client_event.events = EPOLLIN;
                    client_event.data.fd = client_fd;
                    epoll_ctl(epoll_fd, EPOLL_CTL_ADD, client_fd, &client_event);
                }
            } else {
                // --- CLIENT SENT DATA ---
                char buf[4096];
                int bytes = read(events[i].data.fd, buf, sizeof(buf));

                if (bytes <= 0) {
                    // Client disconnected
                    close(events[i].data.fd);
                } else {
                    // Send response — in real server you'd also make this non-blocking
                    write(events[i].data.fd, response, strlen(response));
                    close(events[i].data.fd);
                }
            }
        }
    }
    return 0;
}

// THREAD SAFETY: None needed! Single thread, single event loop.
// But if you add a thread pool for CPU work:
//   - Event loop thread: NEVER do blocking/CPU work
//   - Worker threads: do heavy computation
//   - Communication between them: use a lock-free queue or pipe
//     (write to a pipe fd → epoll detects it → event loop picks up result)

