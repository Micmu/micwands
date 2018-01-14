package net.micmu.mcmods.micwands.core;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;

import net.micmu.mcmods.micwands.MicWandsMod;

/**
 *
 * @author Micmu
 */
public class LootTableHandler {
    private static final LootTableHandler INSTANCE = new LootTableHandler();

    private final ResourceLocation INJECT_LVL_0 = new ResourceLocation(MicWandsMod.MODID, "inject/lvl0");
    private final ResourceLocation INJECT_LVL_1 = new ResourceLocation(MicWandsMod.MODID, "inject/lvl1");
    private final ResourceLocation INJECT_LVL_2 = new ResourceLocation(MicWandsMod.MODID, "inject/lvl2");

    private final LootCondition[] NO_LOOT_COND = new LootCondition[0];

    /**
     *
     * @return
     */
    public static LootTableHandler getInstance() {
        return INSTANCE;
    }

    /**
     *
     * @param res
     * @param table
     */
    public void initTable(ResourceLocation res, LootTable table) {
        ResourceLocation inject = getInjectionTable(res);
        if (inject != null) {
            if (MicWandsMod.LOG.isTraceEnabled())
                MicWandsMod.LOG.trace("Expanding loot table: " + res + " --> " + inject.getResourcePath());
            table.addPool(new LootPool(new LootEntry[] { new LootEntryTable(inject, 1, 0, NO_LOOT_COND, "micwands_inject_entry") }, NO_LOOT_COND, new RandomValueRange(1), new RandomValueRange(0, 1), "micwands_inject_pool"));
        }
    }

    /**
     *
     * @param res
     * @return
     */
    private ResourceLocation getInjectionTable(ResourceLocation res) {
        if ("minecraft".equals(res.getResourceDomain())) {
            // Vanilla minecraft integration
            if (matchName(res, "chests", "desert_pyramid")) {
                return INJECT_LVL_0;
            } else if (matchName(res, "chests", "jungle_temple")) {
                return INJECT_LVL_0;
            } else if (matchName(res, "chests", "igloo_chest")) {
                return INJECT_LVL_0;
            } else if (matchName(res, "chests", "abandoned_mineshaft")) {
                return INJECT_LVL_0;
            } else if (matchName(res, "chests", "simple_dungeon")) {
                return INJECT_LVL_0;
            } else if (matchName(res, "chests", "woodland_mansion")) {
                return INJECT_LVL_1;
            } else if (matchName(res, "chests", "nether_bridge")) {
                return INJECT_LVL_1;
            } else if (matchName(res, "chests", "stronghold_corridor")) {
                return INJECT_LVL_2;
            } else if (matchName(res, "chests", "stronghold_crossing")) {
                return INJECT_LVL_2;
            } else if (matchName(res, "chests", "stronghold_library")) {
                return INJECT_LVL_2;
            }
        }
        return null;
    }

    /**
     *
     * @param res
     * @param prefix
     * @param suffix
     * @return
     */
    private boolean matchName(ResourceLocation res, String prefix, String suffix) {
        String s = res.getResourcePath();
        if (s.length() != (prefix.length() + suffix.length() + 1))
            return false;
        if ((s.charAt(prefix.length()) != '/') || !s.startsWith(prefix, 0))
            return false;
        return s.regionMatches(prefix.length() + 1, suffix, 0, suffix.length());
    }

    /**
     *
     */
    private LootTableHandler() {
    }
}
