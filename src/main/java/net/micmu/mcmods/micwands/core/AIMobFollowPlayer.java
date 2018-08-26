package net.micmu.mcmods.micwands.core;

import java.util.Random;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockGrassPath;
import net.minecraft.block.BlockMagma;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityIronGolem;
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
    private static final double DIST_TELEPORT = 200.0D; // 14+ blocks
    private static final double DIST_RIDE_MOVE = 256.0D; // 16 blocks
    private static final double DIST_LOOK = 576.0D; // 24 blocks
    private static final double DIST_CRITICAL = 9216.0D; // 96 blocks

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
    private int shortTick;
    private int longTick;

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
        // Fine tune for certain vanilla mobs
        if (creature instanceof EntityVillager) {
            this.followSpeed = 0.7D;
            this.distFarSq = 64.0D;
            // Helpless little villagers like to stay close
            this.distCloseSq = ((EntityVillager)creature).isChild() ? 4.0D : 9.0D;
        } else if (creature instanceof AbstractHorse) {
            this.followSpeed = 2.0D;
            this.distFarSq = 81.0D;
            this.distCloseSq = 16.0D;
        } else {
            if (creature instanceof EntityChicken) {
                this.followSpeed = 1.5D;
            } else if ((creature instanceof EntityZombie) || (creature instanceof EntityIronGolem)) {
                this.followSpeed = 1.2D;
            } else {
                this.followSpeed = 1.3D;
            }
            if (creature.width > 1.0F) {
                // Fat mofo. Stay a bit further away.
                this.distFarSq = 81.0D;
                this.distCloseSq = 16.0D;
            } else {
                this.distFarSq = 64.0D;
                this.distCloseSq = 9.0D;
            }
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
        shortTick = 0;
        longTick = 0;
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
        if (isCreatureReady() && (owner != null)) {
            double d = creature.getDistanceSq(owner);
            if (d > DIST_CRITICAL) {
                // Attempt a long range teleport!
                if (--longTick <= 0) {
                    longTick = 9 + creature.getRNG().nextInt(3);
                    if (!creature.isRiding())
                        if (!attemptTeleport())
                            attemptEmergencyTeleport();
                }
            } else {
                // Look at player
                if (d < DIST_LOOK)
                    creature.getLookHelper().setLookPositionWithEntity(owner, 10.0F, (float)creature.getVerticalFaceSpeed());
                if (--shortTick <= 0) {
                    shortTick = 9 + creature.getRNG().nextInt(3);
                    longTick = 0;
                    // Attempt to walk towards the player
                    if (((d < DIST_RIDE_MOVE) || !creature.isRiding()) && !creature.getNavigator().tryMoveToEntityLiving(owner, followSpeed)) {
                        // Failed.
                        if ((d > DIST_TELEPORT) && !creature.isRiding())
                            attemptTeleport(); // Attempt short range teleport
                    }
                }
            }
        }
    }

    /**
     *
     * @return
     */
    private boolean attemptTeleport() {
        final BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        boolean isWide = (creature.width > 1.0F);
        int dx = 0;
        int dz = isWide ? 3 : 2;
        int x = MathHelper.floor(owner.posX) - dz;
        int y = MathHelper.floor(owner.getEntityBoundingBox().minY);
        int z = MathHelper.floor(owner.posZ) - dz;
        if (isWide) {
            for (; dx <= 6; dx += 2)
                for (dz = 0; dz <= 6; dz += 2)
                    if (((dx == 0) || (dz == 0) || (dx == 6) || (dz == 6)) && canTeleportTo(m.setPos(x + dx, y, z + dz)))
                        if (canTeleportTo(m.setPos(x + dx - 1, y, z + dz - 1)))
                            if (canTeleportTo(m.setPos(x + dx - 1, y, z + dz + 1)))
                                if (canTeleportTo(m.setPos(x + dx + 1, y, z + dz + 1)))
                                    return teleportTo(x + dx, y, z + dz, 0.95F);
        } else {
            for (; dx <= 4; ++dx)
                for (dz = 0; dz <= 4; ++dz)
                    if (((dx == 0) || (dz == 0) || (dx == 4) || (dz == 4)) && canTeleportTo(m.setPos(x + dx, y, z + dz)))
                        return teleportTo(x + dx, y, z + dz, 0.45F);
        }
        return false;
    }

    /**
     *
     * @return
     */
    private boolean attemptEmergencyTeleport() {
        final World world = creature.world;
        BlockPos o = new BlockPos(MathHelper.floor(owner.posX), MathHelper.floor(owner.getEntityBoundingBox().minY), MathHelper.floor(owner.posZ));
        BlockPos p = world.getTopSolidOrLiquidBlock(o);
        if (p.getY() < o.getY()) {
            IBlockState bs = world.getBlockState(p);
            if (!avoidBlock(world, bs, p) || (bs.getMaterial() == Material.WATER))
                return teleportTo(p.getX(), p.getY() + 1, p.getZ(), 0.45F);
        }
        return false;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param add
     * @return
     */
    private boolean teleportTo(int x, int y, int z, float add) {
        final Random rnd = creature.getRNG();
        creature.setLocationAndAngles((double)((float)x + add + (0.1F * rnd.nextFloat())), (double)y, (double)((float)z + add + (0.1F * rnd.nextFloat())), creature.rotationYaw, creature.rotationPitch);
        creature.getNavigator().clearPath();
        return true;
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
     * @param m
     * @return
     */
    private boolean canTeleportTo(BlockPos.MutableBlockPos m) {
        final World world = creature.getEntityWorld();
        int y = m.getY();
        IBlockState bs;
        m.setY(y - 1);
        bs = world.getBlockState(m);
        if (moveType == 1) {
            // Flying
            if (!(bs.isSideSolid(world, m, EnumFacing.UP) || (bs.getMaterial() == Material.LEAVES)))
                return false;
        } else if (moveType == 2) {
            // Swimming in water
            if (bs.getMaterial() != Material.WATER)
                return false;
            m.setY(y);
            if (world.getBlockState(m).getMaterial() != Material.WATER)
                return false;
            m.setY(y + 1);
            if (world.getBlockState(m).getMaterial() != Material.WATER)
                return false;
            return true;
        } else if (avoidBlock(world, bs, m)) {
            // Walking
            return false;
        }
        m.setY(y);
        if (isNotPassable(world, m, true))
            return false;
        if (y < 255) {
            m.setY(y + 1);
            if (isNotPassable(world, m, false))
                return false;
            if ((creature.height > 2.0F) && (y < 254)) {
                m.setY(y + 2);
                if (isNotPassable(world, m, false))
                    return false;
            }
        }
        return true;
    }

    /**
     *
     * @param bs
     * @return
     */
    private boolean avoidBlock(World world, IBlockState bs, BlockPos p) {
        if (!creature.isImmuneToFire()) {
            Material m = bs.getMaterial();
            if ((m == Material.LAVA) || (m == Material.FIRE) || (bs.getBlock() instanceof BlockMagma))
                return true;
        }
        return ((bs.getBlockFaceShape(world, p, EnumFacing.DOWN) != BlockFaceShape.SOLID) && !bs.isSideSolid(world, p, EnumFacing.UP) && !solidSpecial(bs.getBlock()));
    }

    /**
     *
     * @param b
     * @return
     */
    private boolean solidSpecial(Block b) {
        return (b instanceof BlockGrassPath) || (b instanceof BlockFarmland) || (b instanceof BlockSlab);
    }

    /**
     *
     * @param world
     * @param p
     * @param bottom
     * @return
     */
    private boolean isNotPassable(World world, BlockPos p, boolean bottom) {
        final IBlockState bs = world.getBlockState(p);
        if (bs.getBlock().isAir(bs, world, p))
            return false;
        if (bottom) {
            final Material m = bs.getMaterial();
            if (((m == Material.SNOW) || (m == Material.PLANTS) || (m == Material.CARPET) || (m == Material.VINE)) && bs.getBlock().isPassable(world, p))
                return false;
        }
        return true;
    }
}
