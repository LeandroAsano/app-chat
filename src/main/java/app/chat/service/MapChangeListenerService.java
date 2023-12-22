package app.chat.service;

import app.chat.model.User;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class MapChangeListenerService {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> future;

    private Map<String, User> snapshotMap = new ConcurrentHashMap<>();
    @Autowired
    private UserService userService;
    @Getter
    private boolean mapChanged = false;

    @PostConstruct
    public void startMapChangeListener() {
        try {
            future = scheduledExecutorService.scheduleAtFixedRate(
                    this::checkForChangesAndBroadcast, 0, 1, TimeUnit.SECONDS);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
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

        Map<String, User> currentMapState = userService.getOnlineUsers();

        log.info("Current Map: ");
        if (!currentMapState.isEmpty()){
            userService.getOnlineUsers().values().forEach(u -> log.info(u.getUsername() + " " + u.getStatus().toString()));
        }

        log.info("Stored Map: ");
        if (!snapshotMap.isEmpty()) {
            snapshotMap.values().forEach(l -> log.info(l.getUsername() + " " + l.getStatus().toString() + " "));
        }

        mapChanged = isMapChanged(snapshotMap);

        if (mapChanged){
            waitForReCheck();

            mapChanged = isMapChanged(snapshotMap);
        }

        if (mapChanged) {
            this.snapshotMap = new ConcurrentHashMap<>();

            currentMapState = userService.getOnlineUsers();
            currentMapState.forEach((key, user) -> {
                try {
                    snapshotMap.put(key, user.clone());
                } catch (CloneNotSupportedException e) {
                    log.error(Arrays.toString(e.getStackTrace()));
                }
            });

            userService.broadcastUserList();
            mapChanged = false;
        }
    }

    private static void waitForReCheck() {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isMapChanged(Map<String, User> usersMap){
        Map<String, User> currentMapState = userService.getOnlineUsers();

        for (Map.Entry<String, User> entry : currentMapState.entrySet()) {
            String key = entry.getKey();
            User currentUser = entry.getValue();
            User storedUser = usersMap.get(key);

            if (storedUser == null) {
                // Key is missing in modified map (removed)
                log.info("Key '" + key + "' removed.");
                return true;
            } else if (!currentUser.equalsValue(storedUser)) {
                // Value has changed
                log.info("Key '" + key + "' changed from '"
                        + currentUser + "' to '" + storedUser + "'.");
                return true;
            }
        }

        for (Map.Entry<String, User> entry : currentMapState.entrySet()) {
            String key = entry.getKey();
            User storedUser = entry.getValue();

            if (!currentMapState.containsKey(key)) {
                // Key is missing in original map (added)
                log.info("Key '" + key + "' added with value '" + storedUser + "'.");
                return true;
            }
        }
        return false;
    }

}
