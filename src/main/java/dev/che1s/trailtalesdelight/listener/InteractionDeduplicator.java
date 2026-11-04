package dev.che1s.trailtalesdelight.listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 每个玩家只保留最近一次交互。玩家退出时移除对应缓存，
 * 避免缓存随服务器历史玩家数量持续增长。
 */
final class InteractionDeduplicator {
    private final Map<UUID, InteractionStamp> lastInteractions = new ConcurrentHashMap<>();

    boolean isDuplicate(UUID playerId, String blockId,
                        int x, int y, int z, int tick) {
        InteractionStamp current = new InteractionStamp(blockId, x, y, z, tick);
        InteractionStamp previous = lastInteractions.put(playerId, current);
        return previous != null
                && previous.tick() == current.tick()
                && previous.blockId().equals(current.blockId())
                && previous.x() == current.x()
                && previous.y() == current.y()
                && previous.z() == current.z();
    }

    void forget(UUID playerId) {
        if (playerId != null) {
            lastInteractions.remove(playerId);
        }
    }

    private record InteractionStamp(String blockId, int x, int y, int z, int tick) {
    }
}
