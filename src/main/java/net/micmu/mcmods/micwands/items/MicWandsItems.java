package net.micmu.mcmods.micwands.items;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * 
 * @author Micmu
 */
public final class MicWandsItems {
    public static Item SILENCE;
    public static Item AGES;
    public static Item FOLLOW;
    public static Item ENFEEBLEMENT;
    public static Item PACIFICATION;
    public static Item FIRE;

    /**
     * 
     */
    private MicWandsItems() {
    }

    /**
     * 
     * @author Micmu
     */
    @EventBusSubscriber
    private static final class Register {

        /**
         * 
         * @param event
         */
        @SubscribeEvent
        public static void onRegisterItems(RegistryEvent.Register<Item> event) {
            final IForgeRegistry<Item> reg = event.getRegistry();
            reg.register(SILENCE = new ItemWandSilence("wandsilence"));
            reg.register(AGES = new ItemWandAge("wandage"));
            reg.register(FOLLOW = new ItemWandFollow("wandfollow"));
            reg.register(ENFEEBLEMENT = new ItemWandEnfeeble("wandenfeeble", false));
            reg.register(PACIFICATION = new ItemWandEnfeeble("wandpacify", true));
            reg.register(FIRE = new ItemWandFire("wandfire"));
        }

        /**
         * 
         * @param event
         */
        @SubscribeEvent
        public static void onRegisterModels(ModelRegistryEvent event) {
            ((ItemWand)SILENCE).registerModels();
            ((ItemWand)AGES).registerModels();
            ((ItemWand)FOLLOW).registerModels();
            ((ItemWand)ENFEEBLEMENT).registerModels();
            ((ItemWand)PACIFICATION).registerModels();
            ((ItemWand)FIRE).registerModels();
        }

        /**
         * 
         */
        private Register() {
        }
    }
}
