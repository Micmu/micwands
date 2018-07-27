package net.micmu.mcmods.micwands.core;

import java.util.UUID;

import net.minecraft.block.BlockGrassPath;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 *
 * @author Micmu
 */
final class AIMobFollowPlayer extends EntityAIBase {
    private final EntityLiving creature;
    private final int moveType; // 0-walk, 1-fly, 2-swim
    private final double followSpeed;
    private final double distFarSq;
    private final double distCloseSq;
    private int tick;
    private String idCache_str;
    private UUID idCache_uid;
    private EntityPlayer owner;
    private float oldWaterPrio;
    private int recalcTick;

    /**
     *
     * @param creature
     */
    AIMobFollowPlayer(EntityLiving creature) {
        this.creature = creature;
        this.setMutexBits(3);
        // Detect creature movement type
        if (creature instanceof EntityWaterMob) {
            this.moveType = 2;
        } else if ((creature instanceof net.minecraft.entity.passive.EntityFlying) || (creature instanceof net.minecraft.entity.EntityFlying)) {
            this.moveType = 1;
        } else {
            this.moveType = 0;
        }
        // Detect creature speed and following distance
        if (creature instanceof EntityVillager) {
            this.followSpeed = 0.7D;
            this.distFarSq = 64.0D;
            this.distCloseSq = ((EntityVillager)creature).isChild() ? 4.0D : 9.0D;
        } else if (creature instanceof AbstractHorse) {
            this.followSpeed = 2.0D;
            this.distFarSq = 81.0D;
            this.distCloseSq = 16.0D;
        } else {
            if (creature instanceof EntityChicken) {
                this.followSpeed = 1.5D;
            } else if (creature instanceof EntityZombie) {
                this.followSpeed = 1.2D;
            } else {
                this.followSpeed = 1.3D;
            }
            this.distFarSq = 64.0D;
            this.distCloseSq = 9.0D;
        }
    }

    /**
     *
     */
    @Override
    public boolean shouldExecute() {
        if (tick > 0) {
            tick--;
            return false;
        } else {
            EntityPlayer own = getOwnerPlayer();
            if ((own != null) && !own.isDead && !own.isSpectator() && isCreatureReady() && (creature.getDistanceSq(own) > distFarSq)) {
                owner = own;
                return true;
            }
            tick = 2;
            return false;
        }
    }

    /**
     *
     */
    @Override
    public boolean shouldContinueExecuting() {
        EntityPlayer own = this.owner;
        return !creature.getNavigator().noPath() && (own != null) && (creature.getDistanceSq(own) > distCloseSq) && isCreatureReady() && !own.isDead && !own.isSpectator();
    }

    /**
     *
     */
    @Override
    public void startExecuting() {
        recalcTick = 0;
        if (moveType != 2)
            oldWaterPrio = creature.getPathPriority(PathNodeType.WATER);
        creature.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    /**
     *
     */
    @Override
    public void resetTask() {
        owner = null;
        creature.getNavigator().clearPath();
        if (moveType != 2)
            creature.setPathPriority(PathNodeType.WATER, oldWaterPrio);
        tick = 0;
    }

    /**
     *
     */
    @Override
    public void updateTask() {
        if (isCreatureReady()) {
            creature.getLookHelper().setLookPositionWithEntity(owner, 10.0F, (float)creature.getVerticalFaceSpeed());
            if (--recalcTick <= 0) {
                recalcTick = 10;
                if (!creature.getNavigator().tryMoveToEntityLiving(owner, followSpeed) && !creature.isRiding() && (creature.getDistanceSq(owner) > 200.0D)) {
                    int x = MathHelper.floor(owner.posX) - 2;
                    int y = MathHelper.floor(owner.getEntityBoundingBox().minY);
                    int z = MathHelper.floor(owner.posZ) - 2;
                    int dz;
                    for (int dx = 0; dx <= 4; ++dx) {
                        for (dz = 0; dz <= 4; ++dz) {
                            if (((dx < 1) || (dz < 1) || (dx > 3) || (dz > 3)) && canTeleportTo(x, y, z, dx, dz)) {
                                creature.setLocationAndAngles((double)((float)(x + dx) + 0.5F), (double)y, (double)((float)(z + dz) + 0.5F), creature.rotationYaw, creature.rotationPitch);
                                creature.getNavigator().clearPath();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @return
     */
    private boolean isCreatureReady() {
        if (creature.isDead || creature.getLeashed())
            return false;
        if (creature instanceof EntityTameable)
            return !((EntityTameable)creature).isSitting();
        return true;
    }

    /**
     *
     * @return
     */
    private EntityPlayer getOwnerPlayer() {
        String s = creature.getEntityData().getString(WandsCore.NBT_KEY_S_FOLLOW_PLAYER);
        if ((s == null) || s.isEmpty()) {
            idCache_uid = null;
            idCache_str = null;
        } else if (!s.equals(idCache_str)) {
            idCache_str = s;
            try {
                idCache_uid = UUID.fromString(s);
            } catch (Exception e) {
                idCache_uid = null;
            }
        }
        return (idCache_uid != null) ? creature.world.getPlayerEntityByUUID(idCache_uid) : null;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param dx
     * @param dz
     * @return
     */
    private boolean canTeleportTo(int x, int y, int z, int dx, int dz) {
        final World world = creature.getEntityWorld();
        final BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(x + dx, y - 1, z + dz);
        final IBlockState bs = world.getBlockState(p);
        if (moveType == 1) {
            // Flying
            if (!(bs.isSideSolid(world, p, EnumFacing.UP) || (bs.getMaterial() == Material.LEAVES)))
                return false;
        } else if (moveType == 2) {
            // Swimming in water
            if (bs.getMaterial() != Material.WATER)
                return false;
            p.setY(y);
            if (world.getBlockState(p).getMaterial() != Material.WATER)
                return false;
            p.setY(y + 1);
            if (world.getBlockState(p).getMaterial() != Material.WATER)
                return false;
            return true;
        } else if ((bs.getBlockFaceShape(world, p, EnumFacing.DOWN) != BlockFaceShape.SOLID) && !(bs.getBlock() instanceof BlockGrassPath)) {
            // Walking
            return false;
        }
        p.setY(y);
        if (isImpassable(world, p, true))
            return false;
        p.setY(y + 1);
        if (isImpassable(world, p, false))
            return false;
        return true;
    }

    /**
     *
     * @param world
     * @param p
     * @param bottom
     * @return
     */
    private boolean isImpassable(World world, BlockPos p, boolean bottom) {
        final IBlockState bs = world.getBlockState(p);
        if (bs.getBlock().isAir(bs, world, p))
            return false;
        if (bottom) {
            final Material m = bs.getMaterial();
            if (((m == Material.SNOW) || (m == Material.PLANTS) || (m == Material.CARPET)) && bs.getBlock().isPassable(world, p))
                return false;
        }
        return true;
    }
}
