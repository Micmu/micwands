package net.micmu.mcmods.micwands.core;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

/**
 * 
 * @author Micmu
 */
final class AIMobSelfImmolation extends EntityAIBase {
    private final EntityLiving creature;
    private final EntityAIBase thePanicAI;
    private final int thePanicPriority;
    private int tick;

    /**
     * 
     * @param creature
     * @param thePanicAI
     * @param thePanicPriority
     */
    AIMobSelfImmolation(EntityLiving creature, EntityAIBase thePanicAI, int thePanicPriority) {
        this.creature = creature;
        this.thePanicAI = thePanicAI;
        this.thePanicPriority = thePanicPriority;
    }

    /**
     * 
     */
    @Override
    public boolean shouldExecute() {
        if (tick > 0) {
            tick--;
        } else {
            PotionEffect pef = creature.getActivePotionEffect(MobEffects.FIRE_RESISTANCE);
            if ((pef == null) || (pef.getDuration() < 15) || !creature.isBurning())
                tick = WandsCore.getInstance().applyBurnEffects(creature);
        }
        return false;
    }

    /**
     * 
     * @return
     */
    EntityAIBase getPanicAI() {
        return thePanicAI;
    }

    /**
     * 
     * @return
     */
    int getPanicPriority() {
        return thePanicPriority;
    }
}
