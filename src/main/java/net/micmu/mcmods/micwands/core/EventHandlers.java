package net.micmu.mcmods.micwands.core;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import net.micmu.mcmods.micwands.items.ItemWand;

/**
 *
 * @author Micmu
 */
@EventBusSubscriber
final class EventHandlers {

    /**
     *
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote && (event.getEntity() instanceof EntityLiving))
            WandsCore.getInstance().initializeMob((EntityLiving)event.getEntity());
    }

    /**
     *
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityTamed(AnimalTameEvent event) {
        final EntityAnimal entity = event.getAnimal();
        if (!entity.world.isRemote)
            WandsCore.getInstance().initializeNewTamedAnimal(entity, event.getTamer());
    }

    /**
     *
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if ((event.getSide() == Side.SERVER) && (event.getHand() == EnumHand.MAIN_HAND) && (event.getTarget() instanceof EntityLivingBase)) {
            final ItemStack stack = event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND);
            if (!stack.isEmpty()) {
                final Item item = stack.getItem();
                if (item instanceof ItemWand) {
                    if (item.itemInteractionForEntity(stack, event.getEntityPlayer(), (EntityLivingBase)event.getTarget(), EnumHand.MAIN_HAND)) {
                        event.setResult(Result.DENY);
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    /**
     *
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onGolemSetAttackTarget(LivingSetAttackTargetEvent event) {
        if ((event.getTarget() != null) && (event.getEntityLiving() instanceof EntityGolem)) {
            EntityGolem golem = (EntityGolem)event.getEntityLiving();
            if (((golem instanceof EntityIronGolem) || (golem instanceof EntitySnowman)) && WandsCore.getInstance().isEnfeebled(event.getTarget())) {
                if (golem.getAttackTarget() != null)
                    golem.setAttackTarget(null);
                if (golem.getRevengeTarget() != null)
                    golem.setRevengeTarget(null);
            }
        }
    }

    /**
     *
     * @param event
     */
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        LootTableHandler.getInstance().initTable(event.getName(), event.getTable());
    }

    /**
     *
     * @param event
     */
    @SuppressWarnings("deprecation")
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onClientChatReceived(ClientChatReceivedEvent event) {
        if (event.getType() == ChatType.GAME_INFO) {
            ITextComponent msg = event.getMessage();
            if (msg instanceof TextComponentTranslation) {
                String k = ((TextComponentTranslation)msg).getKey();
                if ((k != null) && k.startsWith("msg.micwands.", 0) && !net.minecraft.util.text.translation.I18n.canTranslate(k))
                    event.setCanceled(true);
            }
        }
    }

    /**
     *
     */
    private EventHandlers() {
    }
}
