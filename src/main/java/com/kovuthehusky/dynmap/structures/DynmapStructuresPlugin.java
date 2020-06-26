package com.kovuthehusky.dynmap.structures;

import java.io.*;
import java.util.*;

import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import static org.bukkit.StructureType.*;
import static org.bukkit.block.Biome.*;

@SuppressWarnings("unused")
public class DynmapStructuresPlugin extends JavaPlugin implements Listener {
    private static final StructureType[][] BIOMES = new StructureType[Biome.values().length][];
    private static final Map<StructureType, String> LABELS = new HashMap<StructureType, String>() {{
        put(BURIED_TREASURE, "Buried Treasure");
        put(DESERT_PYRAMID, "Desert Pyramid");
        put(END_CITY, "End City");
        put(NETHER_FORTRESS, "Nether Fortress");
        put(IGLOO, "Igloo");
        put(JUNGLE_PYRAMID, "Jungle Pyramid");
        put(WOODLAND_MANSION, "Woodland Mansion");
        put(MINESHAFT, "Abandoned Mineshaft");
        put(OCEAN_MONUMENT, "Ocean Monument");
        put(OCEAN_RUIN, "Underwater Ruins");
        if (StructureType.getStructureTypes().containsKey("pillager_outpost")) {
            put(PILLAGER_OUTPOST, "Pillager Outpost");
        }
        put(SHIPWRECK, "Shipwreck");
        put(STRONGHOLD, "Stronghold");
        put(SWAMP_HUT, "Witch Hut");
        put(VILLAGE, "Village");
        put(RUINED_PORTAL, "Ruined Portal");
        put(BASTION_REMNANT, "Bastion Remnant");
    }};
    private static final Map<StructureType, Boolean> STRUCTURES = new HashMap<>();

    private MarkerAPI api;
    private MarkerSet set;
    private boolean noLabels;
    private boolean includeCoordinates;

    @Override
    public void onEnable() {
        // Set up the metrics
        new Metrics(this);
        // Set up the configuration
        this.saveDefaultConfig();
        FileConfiguration configuration = this.getConfig();
        configuration.options().copyDefaults(true);
        this.saveConfig();
        // Fill in biome data structure
        BIOMES[OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD, RUINED_PORTAL};
        BIOMES[PLAINS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[DESERT.ordinal()] = new StructureType[]{DESERT_PYRAMID, MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[MOUNTAINS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[FOREST.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[TAIGA.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[SWAMP.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, SWAMP_HUT, RUINED_PORTAL};
        BIOMES[RIVER.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[NETHER_WASTES.ordinal()] = new StructureType[]{NETHER_FORTRESS, RUINED_PORTAL, BASTION_REMNANT};
        BIOMES[CRIMSON_FOREST.ordinal()] = new StructureType[]{NETHER_FORTRESS, RUINED_PORTAL, BASTION_REMNANT};
        BIOMES[WARPED_FOREST.ordinal()] = new StructureType[]{NETHER_FORTRESS, RUINED_PORTAL, BASTION_REMNANT};
        BIOMES[SOUL_SAND_VALLEY.ordinal()] = new StructureType[]{NETHER_FORTRESS, RUINED_PORTAL, BASTION_REMNANT};
        BIOMES[BASALT_DELTAS.ordinal()] = new StructureType[]{NETHER_FORTRESS, RUINED_PORTAL};
        BIOMES[THE_END.ordinal()] = new StructureType[]{END_CITY};
        BIOMES[FROZEN_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD, RUINED_PORTAL};
        BIOMES[FROZEN_RIVER.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[SNOWY_TUNDRA.ordinal()] = new StructureType[]{IGLOO, MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[SNOWY_MOUNTAINS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[MUSHROOM_FIELDS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[MUSHROOM_FIELD_SHORE.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[BEACH.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD, RUINED_PORTAL};
        BIOMES[DESERT_HILLS.ordinal()] = new StructureType[]{DESERT_PYRAMID, MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[WOODED_HILLS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[TAIGA_HILLS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[MOUNTAIN_EDGE.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[JUNGLE.ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[JUNGLE_HILLS.ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[JUNGLE_EDGE.ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[BAMBOO_JUNGLE.ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[BAMBOO_JUNGLE_HILLS.ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[DEEP_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_MONUMENT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD, RUINED_PORTAL};
        BIOMES[STONE_SHORE.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD, RUINED_PORTAL};
        BIOMES[SNOWY_BEACH.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD, RUINED_PORTAL};
        BIOMES[BIRCH_FOREST.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[BIRCH_FOREST_HILLS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[DARK_FOREST.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, WOODLAND_MANSION, RUINED_PORTAL};
        BIOMES[SNOWY_TAIGA.ordinal()] = new StructureType[]{IGLOO, MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[SNOWY_TAIGA_HILLS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[GIANT_TREE_TAIGA.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[GIANT_TREE_TAIGA_HILLS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[WOODED_MOUNTAINS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[SAVANNA.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[SAVANNA_PLATEAU.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[BADLANDS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[WOODED_BADLANDS_PLATEAU.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[BADLANDS_PLATEAU.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[SMALL_END_ISLANDS.ordinal()] = new StructureType[]{END_CITY};
        BIOMES[END_MIDLANDS.ordinal()] = new StructureType[]{END_CITY};
        BIOMES[END_HIGHLANDS.ordinal()] = new StructureType[]{END_CITY};
        BIOMES[END_BARRENS.ordinal()] = new StructureType[]{END_CITY};
        BIOMES[WARM_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD, RUINED_PORTAL};
        BIOMES[LUKEWARM_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD, RUINED_PORTAL};
        BIOMES[COLD_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD, RUINED_PORTAL};
        BIOMES[DEEP_WARM_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_MONUMENT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD, RUINED_PORTAL};
        BIOMES[DEEP_LUKEWARM_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_MONUMENT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD, RUINED_PORTAL};
        BIOMES[DEEP_COLD_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_MONUMENT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD, RUINED_PORTAL};
        BIOMES[DEEP_FROZEN_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_MONUMENT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD, RUINED_PORTAL};
        BIOMES[THE_VOID.ordinal()] = new StructureType[]{};
        BIOMES[SUNFLOWER_PLAINS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[DESERT_LAKES.ordinal()] = new StructureType[]{DESERT_PYRAMID, MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[GRAVELLY_MOUNTAINS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[FLOWER_FOREST.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[TAIGA_MOUNTAINS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[SWAMP_HILLS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, SWAMP_HUT, RUINED_PORTAL};
        BIOMES[ICE_SPIKES.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[MODIFIED_JUNGLE.ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[MODIFIED_JUNGLE_EDGE.ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[TALL_BIRCH_FOREST.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[TALL_BIRCH_HILLS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[DARK_FOREST_HILLS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, WOODLAND_MANSION, RUINED_PORTAL};
        BIOMES[SNOWY_TAIGA_MOUNTAINS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[GIANT_SPRUCE_TAIGA.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[GIANT_SPRUCE_TAIGA_HILLS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[MODIFIED_GRAVELLY_MOUNTAINS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[SHATTERED_SAVANNA.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[SHATTERED_SAVANNA_PLATEAU.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE, RUINED_PORTAL};
        BIOMES[ERODED_BADLANDS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[MODIFIED_WOODED_BADLANDS_PLATEAU.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        BIOMES[MODIFIED_BADLANDS_PLATEAU.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, RUINED_PORTAL};
        // Add pillager outposts if supported
        if (StructureType.getStructureTypes().containsKey("pillager_outpost")) {
            for (Biome biome : new Biome[]{PLAINS, DESERT, TAIGA, SNOWY_TUNDRA, SNOWY_MOUNTAINS, DESERT_HILLS, TAIGA_HILLS, SNOWY_TAIGA, SNOWY_TAIGA_HILLS, SAVANNA, SAVANNA_PLATEAU, SUNFLOWER_PLAINS, DESERT_LAKES, TAIGA_MOUNTAINS, ICE_SPIKES, SNOWY_TAIGA_MOUNTAINS, SHATTERED_SAVANNA, SHATTERED_SAVANNA_PLATEAU}) {
                StructureType[] temp = new StructureType[BIOMES[biome.ordinal()].length + 1];
                System.arraycopy(BIOMES[biome.ordinal()], 0, temp, 0, BIOMES[biome.ordinal()].length);
                temp[temp.length - 1] = PILLAGER_OUTPOST;
                BIOMES[biome.ordinal()] = temp;
            }
        }
        // Fill in id and label data structures
        for (StructureType type : StructureType.getStructureTypes().values()) {
            String id = type.getName().toLowerCase(Locale.ROOT).replace("_", "");
            STRUCTURES.put(type, configuration.getBoolean("structures." + id));
            String label = configuration.getString("labels." + id);
            if (label != null) {
                LABELS.put(type, label);
            }
        }
        // Register for events
        this.getServer().getPluginManager().registerEvents(this, this);
        // Check if Dynmap is even enabled
        if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
            // Set up our Dynmap api
            try {
                DynmapCommonAPI plugin = (DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap");
                if (plugin != null) {
                    api = plugin.getMarkerAPI();
                }
            } catch (NullPointerException e) {
                return;
            }
            // Set up our Dynmap layer
            String layer = configuration.getString("layer.name");
            if (layer == null) {
                layer = "Structures";
            }
            set = api.getMarkerSet(layer.toLowerCase(Locale.ROOT));
            if (set == null) {
                set = api.createMarkerSet(layer.toLowerCase(Locale.ROOT), layer, null, true);
            }
            set.setHideByDefault(configuration.getBoolean("layer.hidebydefault"));
            set.setLayerPriority(configuration.getInt("layer.layerprio"));
            noLabels = configuration.getBoolean("layer.noLabels");
            int minZoom = configuration.getInt("layer.minzoom");
            if (minZoom > 0) {
                set.setMinZoom(minZoom);
            }
            includeCoordinates = configuration.getBoolean("layer.inc-coord");
            // Create the marker icons
            for (StructureType type : StructureType.getStructureTypes().values()) {
                String str = type.getName().toLowerCase(Locale.ROOT).replaceAll("_", "");
                InputStream in = this.getClass().getResourceAsStream("/" + str + ".png");
                if (in != null) {
                    if (api.getMarkerIcon("structures." + str) == null) {
                        api.createMarkerIcon("structures." + str, str, in);
                    } else {
                        api.getMarkerIcon("structures." + str).setMarkerIconImage(in);
                    }
                }
            }
            // Remove any markers for disabled types
            List<String> disabled = new ArrayList<>();
            for (StructureType type : StructureType.getStructureTypes().values()) {
                String id = type.getName().toLowerCase(Locale.ROOT).replace("_", "");
                if (!configuration.getBoolean("structures." + id)) {
                    disabled.add(id);
                }
            }
            for (Marker marker : set.getMarkers()) {
                for (String id : disabled) {
                    if (marker.getMarkerID().startsWith(id)) {
                        marker.deleteMarker();
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.getWorld().canGenerateStructures()) {
            new Thread(new DynmapStructuresRunnable(event.getChunk())).start();
        }
    }

    private class DynmapStructuresRunnable implements Runnable {
        private Chunk chunk;

        private DynmapStructuresRunnable(Chunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public void run() {
            Location location = new Location(chunk.getWorld(), chunk.getX() << 4, 64, chunk.getZ() << 4);
            World world = location.getWorld();
            if (world != null) {
                Biome biome = world.getBiome(location.getBlockX(), location.getBlockZ());
                for (StructureType type : BIOMES[biome.ordinal()]) {
                    if (STRUCTURES.get(type)) {
                        Location structure;
                        try {
                            structure = location.getWorld().locateNearestStructure(location, type, 1, false);
                        } catch (NullPointerException e) {
                            getLogger().warning("Skipping locate at ([" + location.getWorld().getName() + "]," + location.getBlockX() + ", " + location.getBlockZ() + ") due to null pointer exception.");
                            return;
                        }
                        if (structure != null) {
                            String id = type.getName().toLowerCase(Locale.ROOT).replace("_", "");
                            int x = structure.getBlockX();
                            int z = structure.getBlockZ();
                            String label = "";
                            if (!noLabels) {
                                label = LABELS.get(type);
                                if (includeCoordinates) {
                                    label = label + " [" + x + "," + z + "]";
                                }
                            }
                            set.createMarker(id + "," + x + "," + z, label, world.getName(), x, 64, z, api.getMarkerIcon("structures." + id), true);
                        }
                    }
                }
            }
        }
    }
}
