package net.micmu.mcmods.micwands.core;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;

/**
 *
 * @author Micmu
 */
final class AIMobStayInVillage extends EntityAIBase {
    private final EntityCreature creature;
    private int tick;
    private Vec3d moveTo;

    /**
     *
     * @param creature
     */
    AIMobStayInVillage(EntityCreature creature) {
        this.creature = creature;
        this.setMutexBits(1);
    }

    /**
     *
     */
    @Override
    public boolean shouldExecute() {
        if (tick > 0) {
            tick--;
        } else {
            tick = 100 + creature.getRNG().nextInt(200);
            Village village = creature.world.getVillageCollection().getNearestVillage(creature.getPosition(), 128);
            if (village == null) {
                creature.detachHome();
            } else {
                creature.setHomePosAndDistance(village.getCenter(), village.getVillageRadius());
            }
        }
        if (creature.hasHome() && !creature.isWithinHomeDistanceCurrentPosition()) {
            BlockPos p = creature.getHomePosition();
            if ((p != null) && !WandsCore.getInstance().isFollowing(creature)) {
                moveTo = RandomPositionGenerator.findRandomTargetBlockTowards(creature, 16, 7, new Vec3d((double)p.getX(), (double)p.getY(), (double)p.getZ()));
                return (moveTo != null);
            }
        }
        return false;
    }

    /**
     *
     */
    @Override
    public boolean shouldContinueExecuting() {
        return !creature.getNavigator().noPath();
    }

    /**
     *
     */
    @Override
    public void startExecuting() {
        Vec3d v = moveTo;
        moveTo = null;
        creature.getNavigator().tryMoveToXYZ(v.x, v.y, v.z, 1.0D);
    }
}
