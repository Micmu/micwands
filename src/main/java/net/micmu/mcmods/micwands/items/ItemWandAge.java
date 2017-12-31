package net.micmu.mcmods.micwands.items;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

import net.micmu.mcmods.micwands.core.WandsCore;

/**
 *
 * @author Micmu
 */
public class ItemWandAge extends ItemWand {

    /**
     *
     * @param name
     */
    public ItemWandAge(String name) {
        super(name);
    }

    /**
     *
     */
    @Override
    protected int onWandInteract(EntityPlayer player, EntityLivingBase entity) {
        final WandsCore wc = WandsCore.getInstance();
        if (wc.canAge(entity)) {
            int r = wc.wandAge(entity);
            player.sendStatusMessage(new TextComponentTranslation("msg.micwands.age." + r, entity.getName()), true);
            return (r > 0) ? (6 + player.getRNG().nextInt(6)) : -(3 + player.getRNG().nextInt(3));
        }
        player.sendStatusMessage(new TextComponentTranslation("msg.micwands.err.worksonly", new TextComponentTranslation("msg.micwands.err.baby")), true);
        return 0;
    }

}
