package hr.algebra.theloop.model;

import lombok.Data;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class GameResources implements Serializable {

    private Map<Era, Integer> rifts;
    private Map<Era, Integer> energy;
    private Set<Era> vortexes;
    private Map<Era, List<Duplicate>> duplicates;

    public GameResources() {
        this.rifts = new ConcurrentHashMap<>();
        this.energy = new ConcurrentHashMap<>();
        this.vortexes = ConcurrentHashMap.newKeySet();
        this.duplicates = new ConcurrentHashMap<>();

        for (Era era : Era.values()) {
            rifts.put(era, 0);
            energy.put(era, 1);
            duplicates.put(era, new ArrayList<>());
        }
    }

    public int getRifts(Era era) {
        return rifts.getOrDefault(era, 0);
    }

    public void addRifts(Era era, int amount) {
        int current = getRifts(era);
        rifts.put(era, current + amount);
    }

    public void removeRifts(Era era, int amount) {
        int current = getRifts(era);
        int newAmount = Math.max(0, current - amount);
        rifts.put(era, newAmount);
    }

    public int getEnergy(Era era) {
        return energy.getOrDefault(era, 0);
    }

    public void addEnergy(Era era, int amount) {
        int current = getEnergy(era);
        energy.put(era, current + amount);
    }

    public void removeEnergy(Era era, int amount) {
        int current = getEnergy(era);
        int newAmount = Math.max(0, current - amount);
        energy.put(era, newAmount);
    }

    public boolean hasVortex(Era era) {
        return vortexes.contains(era);
    }

    public void createVortex(Era era) {
        vortexes.add(era);
        rifts.put(era, 0);
    }

    public int getVortexCount() {
        return vortexes.size();
    }

    public List<Duplicate> getDuplicatesAt(Era era) {
        return new ArrayList<>(duplicates.getOrDefault(era, new ArrayList<>()));
    }

    public void addDuplicate(Era era, Duplicate duplicate) {
        duplicates.computeIfAbsent(era, k -> new ArrayList<>()).add(duplicate);
    }

    public boolean removeDuplicate(Era era, Duplicate duplicate) {
        List<Duplicate> eraDuplicates = duplicates.get(era);
        return eraDuplicates != null && eraDuplicates.remove(duplicate);
    }

    public int getDuplicateCount(Era era) {
        return duplicates.getOrDefault(era, new ArrayList<>()).size();
    }

    public int getTotalRifts() {
        return rifts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getTotalEnergy() {
        return energy.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Era getEraWithMostRifts() {
        return rifts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Era.DAWN_OF_TIME);
    }

    public Era getEraWithMostEnergy() {
        return energy.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Era.DAWN_OF_TIME);
    }

    public void clearVortexes() {
        vortexes.clear();
    }

    public void resetResources() {
        for (Era era : Era.values()) {
            rifts.put(era, 0);
            energy.put(era, 1);
            duplicates.put(era, new ArrayList<>());
        }
        vortexes.clear();
    }
}