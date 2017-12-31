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
public class ItemWandFollow extends ItemWand {

    /**
     *
     * @param name
     */
    public ItemWandFollow(String name) {
        super(name);
    }

    /**
     *
     */
    @Override
    protected int onWandInteract(EntityPlayer player, EntityLivingBase entity) {
        final WandsCore wc = WandsCore.getInstance();
        if (wc.canFollowing(entity)) {
            int r = wc.wandFollowing(entity, player);
            if (r >= 0) {
                int cost = 0;
                if (r >= 2) {
                    if (r == 4) {
                        r = 1;
                        cost = 1;
                    } else {
                        r = (r == 2) ? 1 : 0;
                        cost = 10000;
                    }
                }
                player.sendStatusMessage(new TextComponentTranslation("msg.micwands.follow." + r, entity.getName()), true);
                int d = (cost != 0) ? cost : (6 + player.getRNG().nextInt(6));
                return (r == 0) ? -d : d;
            } else if (r == -2) {
                player.sendStatusMessage(new TextComponentTranslation("msg.micwands.err.notowner"), true);
                return 0;
            } else if (r == -3) {
                player.sendStatusMessage(new TextComponentTranslation("msg.micwands.err.tamed"), true);
                return 0;
            }
        }
        player.sendStatusMessage(new TextComponentTranslation("msg.micwands.err.worksonly", new TextComponentTranslation("msg.micwands.err.neutral")), true);
        return 0;
    }

    /**
     * 
     */
    @Override
    protected SoundEvent getSoundSuccess(boolean toggleOn) {
        return toggleOn ? SoundEvents.BLOCK_NOTE_BELL : SoundEvents.BLOCK_NOTE_BASS;
    }

    /**
     * 
     */
    @Override
    protected float getSoundSuccessPitch(boolean toggleOn) {
        return 1.0f;
    }

    /**
     * 
     */
    @Override
    protected int getAnimationDuration(boolean toggleOff) {
        return 20;
    }
}