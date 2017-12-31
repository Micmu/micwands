package net.micmu.mcmods.micwands;

import net.minecraftforge.fml.common.Mod;

/**
 *
 * @author Micmu
 */
@Mod(modid = MicWandsMod.MODID, name = MicWandsMod.NAME, version = MicWandsMod.VERSION, acceptedMinecraftVersions = MicWandsMod.ACCEPTED_MINECRAFT_VERSIONS, acceptableRemoteVersions = MicWandsMod.ACCEPTED_REMOTE_VERSIONS)
public class MicWandsMod {
    public static final String MODID = "micwands";
    public static final String NAME = "Mob Control Wands";
    public static final String VERSION = "1.0.0";
    public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.12,1.13)";
    public static final String ACCEPTED_REMOTE_VERSIONS = "[1.0,1.1)";

    @Mod.Instance(MODID)
    public static MicWandsMod INSTANCE;
}
