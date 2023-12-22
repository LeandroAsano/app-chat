package app.chat.service;

import app.chat.model.User;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MapChangeListenerService {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private Map<String, User> usersMap = new HashMap<>();
    private ScheduledFuture<?> future;
    @Autowired
    private UserService userService;
    @Getter
    private boolean mapChanged = false;

    @PostConstruct
    public void startMapChangeListener() {
        // Schedule a task to run periodically
        log.info("STARTING CHANGE MAP LISTENER");
        try {
            future = scheduledExecutorService.scheduleAtFixedRate(
                    this::checkForChangesAndBroadcast, 0, 1, TimeUnit.SECONDS); // Adjust the time interval as needed

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    log.info("Shutdown Listener");

                    if (future.cancel(true)) {
                        log.info("Task completely cancelled");
                    } else {
                        log.info("Task allowed to complete");
                    }

                    scheduledExecutorService.shutdown();
                    if (!scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS))
                        log.warn("Executor did not terminate in the specified time.");
                } catch (InterruptedException e) {
                    log.error("Error during shutdown", e);
                    // Restore interrupted status
                    Thread.currentThread().interrupt();
                }
            }));
        } catch (Exception e) {
            log.error("Error during startup", e);
        }
    }

    private void checkForChangesAndBroadcast() {
        log.info("Checking for changes...");

        Map<String, User> currentMapState = new HashMap<>(userService.getOnlineUsers());

        mapChanged = !currentMapState.equals(usersMap);

        log.info("Current Map: " + currentMapState);
        log.info("Stored Map: " + usersMap);

        if (mapChanged) {
            userService.broadcastUserList();
            usersMap = currentMapState;
            mapChanged = false;
        }
    }

}
