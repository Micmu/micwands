package net.micmu.mcmods.micwands.items;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

import net.micmu.mcmods.micwands.core.WandsCore;

/**
 *
 * @author Micmu
 */
public class ItemWandSilence extends ItemWand {

    /**
     *
     * @param name
     */
    public ItemWandSilence(String name) {
        super(name);
    }

    /**
     *
     */
    @Override
    protected int onWandInteract(EntityPlayer player, EntityLivingBase entity) {
        final WandsCore wc = WandsCore.getInstance();
        if (wc.canSilence(entity)) {
            int r = wc.wandSilence(entity);
            player.sendStatusMessage(new TextComponentTranslation("msg.micwands.silence." + r, entity.getName()), true);
            return (r > 0) ? (3 + player.getRNG().nextInt(3)) : -(player.getRNG().nextBoolean() ? 1 : 2);
        } else {
            player.sendStatusMessage(new TextComponentTranslation("msg.micwands.err.worksonly", new TextComponentTranslation("msg.micwands.err.living")), true);
            return 0;
        }
    }

}
