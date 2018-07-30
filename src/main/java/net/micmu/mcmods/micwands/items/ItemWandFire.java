package net.micmu.mcmods.micwands.items;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;

import net.micmu.mcmods.micwands.core.WandsCore;

/**
 *
 * @author Micmu
 */
public class ItemWandFire extends ItemWand {

    /**
     *
     * @param name
     */
    public ItemWandFire(String name) {
        super(name);
    }

    /**
     *
     */
    @Override
    protected int onWandInteract(EntityPlayer player, EntityLivingBase entity) {
        final WandsCore wc = WandsCore.getInstance();
        if (wc.canFire(entity)) {
            int r = wc.wandFire(entity);
            player.sendStatusMessage(new TextComponentTranslation("msg.micwands.fire." + r, entity.getName()), true);
            return (r > 0) ? (6 + player.getRNG().nextInt(6)) : -(3 + player.getRNG().nextInt(3));
        }
        if (entity.isImmuneToFire()) {
            player.sendStatusMessage(new TextComponentTranslation("msg.micwands.err.fireimmune"), true);
        } else {
            player.sendStatusMessage(new TextComponentTranslation("msg.micwands.err.worksonly", new TextComponentTranslation(entity.isImmuneToFire() ? "msg.micwands.err.fireimmune" : "msg.micwands.err.neutral")), true);
        }
        return 0;
    }

    /**
     *
     */
    @Override
    protected SoundEvent getSoundSuccess(boolean toggleOn) {
        return toggleOn ? SoundEvents.ITEM_FIRECHARGE_USE : SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE;
    }

    /**
     *
     */
    @Override
    protected float getSoundSuccessPitch(boolean toggleOn) {
        return 1.0f;
    }
}
