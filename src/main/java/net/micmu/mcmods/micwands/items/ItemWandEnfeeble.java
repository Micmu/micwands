package net.micmu.mcmods.micwands.items;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;

import net.micmu.mcmods.micwands.core.WandsCore;

/**
 *
 * @author Micmu
 */
public class ItemWandEnfeeble extends ItemWand {
    private final boolean pacify;

    /**
     *
     * @param name
     * @param pacify
     */
    public ItemWandEnfeeble(String name, boolean pacify) {
        super(name);
        this.pacify = pacify;
    }

    /**
     *
     */
    @Override
    protected int onWandInteract(EntityPlayer player, EntityLivingBase entity) {
        final WandsCore wc = WandsCore.getInstance();
        if (pacify ? wc.canPacify(entity) : wc.canEnfeeble(entity)) {
            int r = pacify ? wc.wandPacify(entity) : wc.wandEnfeeble(entity);
            if (r < 0) {
                player.sendStatusMessage(new TextComponentTranslation("msg.micwands.err.worksonly", new TextComponentTranslation("msg.micwands.err.named")), true);
                return 0;
            }
            player.sendStatusMessage(new TextComponentTranslation("msg.micwands." + (pacify ? "pacify." : "enfeeble.") + r, entity.getName()), true);
            if (r > 0) {
                if (!player.isCreative() && (player.getEntityWorld().getDifficulty() != EnumDifficulty.PEACEFUL)) {
                    if (player.isPotionActive(MobEffects.SLOWNESS))
                        player.removePotionEffect(MobEffects.SLOWNESS);
                    player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 400 + player.getRNG().nextInt(400), 2));
                    if (player.isPotionActive(MobEffects.WEAKNESS))
                        player.removePotionEffect(MobEffects.WEAKNESS);
                    player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 400 + player.getRNG().nextInt(400), 1));
                }
                return 10 + player.getRNG().nextInt(10);
            }
            return 0;
        }
        player.sendStatusMessage(new TextComponentTranslation("msg.micwands.err.worksonly", new TextComponentTranslation("msg.micwands.err.mob")), true);
        return 0;
    }
}
