package me.realized.duels.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaImpl;

public class ArenaData {

    private String name;
    private boolean disabled;
    private Set<String> kits = new HashSet<>();
    private Map<Integer, LocationData> positions = new HashMap<>();

    public ArenaData(final ArenaImpl arena) {
        this.name = arena.getName();
        this.disabled = arena.isDisabled();
        arena.getKits().forEach(kit -> this.kits.add(kit.getName()));
        arena.getPositions().entrySet().stream()
                .filter(entry -> entry.getValue().getWorld() != null)
                .forEach(entry -> positions.put(entry.getKey(), LocationData.fromLocation(entry.getValue())));
    }

    public ArenaImpl toArena(final DuelsPlugin plugin) {
        final ArenaImpl arena = new ArenaImpl(plugin, name, disabled);

        // Manually bind kits and add locations to prevent saveArenas being called
        kits.stream()
                .map(name -> plugin.getKitManager().get(name))
                .filter(Objects::nonNull)
                .forEach(kit -> arena.getKits().add(kit));
        positions.forEach((key, value) -> arena.getPositions().put(key, value.toLocation()));
        arena.refreshGui(arena.isAvailable());
        return arena;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Set<String> getKits() {
        return kits;
    }

    public void setKits(Set<String> kits) {
        this.kits = kits;
    }

    public Map<Integer, LocationData> getPositions() {
        return positions;
    }

    public void setPositions(Map<Integer, LocationData> positions) {
        this.positions = positions;
    }
}
