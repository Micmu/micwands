package net.micmu.mcmods.micwands.core;

import java.util.UUID;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntitySpellcasterIllager;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;

/**
 *
 * @author Micmu
 */
public class WandsCore {
    private static final WandsCore INSTANCE = new WandsCore();

    public static final String NBT_KEY_B_PACIFIED = "Pacified";
    public static final String NBT_KEY_B_PERMANENT_FIRE = "PermanentFire";
    public static final String NBT_KEY_I_ORIGINAL_AGE = "OriginalAge";
    public static final String NBT_KEY_S_FOLLOW_PLAYER = "FollowPlayer";
    public static final String NBT_KEY_S_FOLLOW_LAST_PLAYER = "FollowLastPlayer";
    public static final String NBT_KEY_L_FOLLOW_LAST_TIME = "FollowLastTime";

    private static final int PARMANENT_BABY_MAX_AGE = -500000000;

    /**
     *
     * @return
     */
    public static WandsCore getInstance() {
        return INSTANCE;
    }

    /**
     * 
     * @param entity
     * @return
     */
    public boolean canSilence(EntityLivingBase entity) {
        return (entity instanceof EntityLiving) && !(entity instanceof EntityDragon) && !(entity instanceof EntityWither);
    }

    /**
     * 
     * @param entity
     * @return
     */
    public int wandSilence(EntityLivingBase entity) {
        if (!canSilence(entity))
            return -1;
        boolean b = !entity.isSilent();
        entity.setSilent(b);
        return b ? 1 : 0;
    }

    /**
     * 
     * @param entity
     * @return
     */
    public boolean canAge(EntityLivingBase entity) {
        return (entity instanceof EntityAgeable) && ((EntityAgeable)entity).isChild();
    }

    /**
     * 
     * @param entity
     * @return
     */
    public int wandAge(EntityLivingBase entity) {
        if (!canAge(entity))
            return -1;
        EntityAgeable mob = (EntityAgeable)entity;
        int age = mob.getGrowingAge();
        if (age > PARMANENT_BABY_MAX_AGE) {
            // Not perm baby -> turn on
            entity.getEntityData().setInteger(NBT_KEY_I_ORIGINAL_AGE, age);
            mob.setGrowingAge(-2000000000);
            return 1;
        } else {
            // Is perm baby -> turn off
            age = entity.getEntityData().getInteger(NBT_KEY_I_ORIGINAL_AGE);
            if (age > -2)
                age = -24000;
            mob.setGrowingAge(age);
            return 0;
        }
    }

    /**
     * 
     * @param entity
     * @return
     */
    public boolean isEnfeebled(EntityLivingBase entity) {
        if ((entity instanceof EntityLiving) && ((EntityLiving)entity).isNoDespawnRequired() && (entity instanceof IMob)) {
            final IAttributeInstance dmg = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
            return (dmg != null) && (dmg.getBaseValue() == 0.0D) && entity.hasCustomName();
        }
        return false;
    }

    /**
     * 
     * @return
     */
    public boolean canEnfeeble(EntityLivingBase entity) {
        return (entity instanceof EntityLiving) && (entity instanceof IMob) && !entity.isDead;
    }

    /**
     * 
     * @param entity
     * @return
     */
    public int wandEnfeeble(EntityLivingBase entity) {
        return canEnfeeble(entity) ? doEnfebleOrPacify((EntityLiving)entity, false) : -1;
    }

    /**
     * 
     * @param entity
     * @return
     */
    public boolean isPacified(EntityLivingBase entity) {
        if ((entity instanceof EntityLiving) && ((EntityLiving)entity).isNoDespawnRequired() && (entity instanceof IMob)) {
            final IAttributeInstance dmg = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
            return (dmg != null) && (dmg.getBaseValue() == 0.0D) && entity.hasCustomName() && entity.getEntityData().getBoolean(NBT_KEY_B_PACIFIED);
        }
        return false;
    }

    /**
     * 
     * @param entity
     * @return
     */
    public boolean canPacify(EntityLivingBase entity) {
        return canEnfeeble(entity);
    }

    /**
     * 
     * @param entity
     * @return
     */
    public int wandPacify(EntityLivingBase entity) {
        return canPacify(entity) ? doEnfebleOrPacify((EntityLiving)entity, true) : -1;
    }

    /**
     * 
     * @param entity
     * @return
     */
    public boolean isFollowing(EntityLivingBase entity) {
        if (canFollowing(entity)) {
            String s = entity.getEntityData().getString(NBT_KEY_S_FOLLOW_PLAYER);
            return (s != null) && !s.isEmpty();
        }
        return false;
    }

    /**
     * 
     * @param entity
     * @return
     */
    public boolean canFollowing(EntityLivingBase entity) {
        if ((entity instanceof EntityLiving) && !entity.isDead) {
            if (entity instanceof EntityGolem)
                return false;
            return (entity instanceof EntityVillager) || ((entity instanceof IAnimals) && !(entity instanceof IMob)) || isPacified(entity);
        }
        return false;
    }

    /**
     * 
     * @param entity
     * @param player
     * @return
     */
    public int wandFollowing(EntityLivingBase entity, EntityPlayer player) {
        return canFollowing(entity) ? doFollowing((EntityLiving)entity, player, false) : -1;
    }

    /**
     * 
     * @param entity
     * @return
     */
    public boolean isFire(EntityLivingBase entity) {
        return canFire(entity) && entity.getEntityData().getBoolean(NBT_KEY_B_PERMANENT_FIRE);
    }

    /**
     * 
     * @param entity
     * @return
     */
    public boolean canFire(EntityLivingBase entity) {
        return !entity.isImmuneToFire() && canFollowing(entity);
    }

    /**
     * 
     * @param entity
     * @return
     */
    public int wandFire(EntityLivingBase entity) {
        if (!canFire(entity))
            return -1;
        if (entity.getEntityData().getBoolean(NBT_KEY_B_PERMANENT_FIRE)) {
            // On. Turn off!
            entity.getEntityData().removeTag(NBT_KEY_B_PERMANENT_FIRE);
            updateMobAI((EntityLiving)entity, false, -1, 0);
            entity.extinguish();
            entity.removePotionEffect(MobEffects.FIRE_RESISTANCE);
            return 0;
        } else {
            // Off. Turn on!
            entity.getEntityData().setBoolean(NBT_KEY_B_PERMANENT_FIRE, true);
            updateMobAI((EntityLiving)entity, false, -1, 1);
            return 1;
        }
    }

    /**
     * 
     * @param mob
     */
    public void initializeMob(EntityLiving mob) {
        // Update mob AI if needed.
        boolean p = isPacified(mob);
        boolean f = isFollowing(mob);
        boolean i = isFire(mob);
        if (p || f || i) {
            updateMobAI(mob, p, (f ? 1 : 0), (i ? 1 : 0));
            if (i)
                applyBurnEffects(mob);
        }
        // Check if this mobster needs to be kept as a baby.
        if (mob instanceof EntityAgeable) {
            int a = ((EntityAgeable)mob).getGrowingAge();
            if ((a <= PARMANENT_BABY_MAX_AGE) && (a > -1900000000))
                ((EntityAgeable)mob).setGrowingAge(-2000000000);
        }
    }

    /**
     * 
     * @param animal
     * @param player
     */
    public void initializeNewTamedAnimal(EntityAnimal animal, EntityPlayer player) {
        if (isFollowing(animal) && (!(animal instanceof AbstractHorse) || (player == null) || player.getCachedUniqueIdString().equals(animal.getEntityData().getString(NBT_KEY_S_FOLLOW_PLAYER))))
            doFollowing(animal, player, true);
    }

    /**
     * 
     * @param creature
     * @return
     */
    protected int applyBurnEffects(EntityLiving creature) {
        int tick = 18 + creature.getRNG().nextInt(6);
        creature.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, ((tick + 2) * 20), 0, false, false));
        creature.setFire(tick);
        return tick;
    }

    /**
     *
     * @param mob
     * @param pacify
     * @return
     */
    private int doEnfebleOrPacify(EntityLiving mob, boolean pacify) {
        if (!mob.isNoDespawnRequired() || !(mob instanceof IMob) || !mob.hasCustomName())
            return -1;
        IAttributeInstance atr = mob.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (atr == null)
            return -1;
        double v = atr.getBaseValue();
        if (pacify) {
            if ((v == 0.0D) && mob.getEntityData().getBoolean(NBT_KEY_B_PACIFIED))
                return 0;
            if (!isEnfeebled(mob))
                return -2;
        } else if (v == 0.0D) {
            return 0;
        }
        if (v != 0.0D)
            atr.setBaseValue(0.0D);
        atr = mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        v = atr.getBaseValue();
        if (v > 16.0D)
            atr.setBaseValue(16.0D);
        if (pacify) {
            mob.getEntityData().setBoolean(NBT_KEY_B_PACIFIED, true);
            updateMobAI(mob, true, -1, -1);
        }
        return 1;
    }

    /**
     * 
     * @param mob
     * @param player
     * @param forceUnfollow
     * @return
     */
    private int doFollowing(EntityLiving mob, EntityPlayer player, boolean forceUnfollow) {
        String s;
        if (forceUnfollow || isFollowing(mob)) {
            // FOLLOW OFF
            s = mob.getEntityData().getString(NBT_KEY_S_FOLLOW_PLAYER);
            if (!forceUnfollow && ((s == null) || (player == null) || !s.equals(player.getCachedUniqueIdString())))
                return -2; // Not an owner!!
            mob.getEntityData().removeTag(NBT_KEY_S_FOLLOW_PLAYER);
            if (s != null) {
                mob.getEntityData().setString(NBT_KEY_S_FOLLOW_LAST_PLAYER, s);
                mob.getEntityData().setLong(NBT_KEY_L_FOLLOW_LAST_TIME, mob.getEntityWorld().getTotalWorldTime());
            } else {
                mob.getEntityData().removeTag(NBT_KEY_S_FOLLOW_LAST_PLAYER);
                mob.getEntityData().removeTag(NBT_KEY_L_FOLLOW_LAST_TIME);
            }
            updateMobAI(mob, false, 0, -1);
            return 3;
        } else if (player != null) {
            // FOLLOW ON
            // Check if owner of a tamed animal!!
            UUID owner = null;
            boolean tamedNotHorse = false;
            if ((mob instanceof AbstractHorse) && ((AbstractHorse)mob).isTame()) {
                owner = ((AbstractHorse)mob).getOwnerUniqueId();
            } else if ((mob instanceof EntityTameable) && ((EntityTameable)mob).isTamed()) {
                owner = ((EntityTameable)mob).getOwnerId();
                tamedNotHorse = true;
            }
            if ((owner != null) && !owner.equals(player.getUniqueID()))
                return -2; // Not an owner!!
            if (tamedNotHorse)
                return -3; // Already tamed!!
            long now = mob.getEntityWorld().getTotalWorldTime();
            long ts = mob.getEntityData().getLong(NBT_KEY_L_FOLLOW_LAST_TIME);
            s = mob.getEntityData().getString(NBT_KEY_S_FOLLOW_LAST_PLAYER);
            if (s != null)
                mob.getEntityData().removeTag(NBT_KEY_S_FOLLOW_LAST_PLAYER);
            mob.getEntityData().setString(NBT_KEY_S_FOLLOW_PLAYER, player.getCachedUniqueIdString());
            mob.getEntityData().setLong(NBT_KEY_L_FOLLOW_LAST_TIME, now);
            updateMobAI(mob, false, 1, -1);
            if ((s != null) && s.equals(player.getCachedUniqueIdString()))
                return (Math.abs(now - ts) <= 6000L) ? 2 : 4;
            return 1;
        }
        return -3;
    }

    /**
     * 
     * @param creature
     * @param doPacify
     * @param doFollow
     * @param doFire
     */
    private void updateMobAI(EntityLiving creature, boolean doPacify, int doFollow, int doFire) {
        int i;
        EntityAITasks tasks;
        EntityAITasks.EntityAITaskEntry[] ar;
        if (doPacify) {
            if (creature.getRevengeTarget() != null)
                creature.setRevengeTarget(null);
            if (creature.getAttackTarget() != null)
                creature.setAttackTarget(null);
            PathNavigate navig = creature.getNavigator();
            if (navig instanceof PathNavigateGround) {
                ((PathNavigateGround)navig).setBreakDoors(false);
                ((PathNavigateGround)navig).setEnterDoors(true);
            }
            if (creature instanceof EntityZombie)
                ((EntityZombie)creature).setBreakDoorsAItask(false);
            tasks = creature.targetTasks;
            ar = tasks.taskEntries.toArray(new EntityAITasks.EntityAITaskEntry[tasks.taskEntries.size()]);
            for (i = ar.length - 1; i >= 0; i--) {
                tasks.removeTask(ar[i].action);
                ar[i] = null;
            }
        }
        tasks = creature.tasks;
        ar = tasks.taskEntries.toArray(new EntityAITasks.EntityAITaskEntry[tasks.taskEntries.size()]);
        EntityAIBase b;
        EntityAIBase thePanic = null;
        int doStayPrio = (creature instanceof EntityCreature) ? 2 : -123;
        int doFollowPrio = 1;
        int doFirePrio = 0;
        int thePanicPrio = 0;
        for (i = ar.length - 1; i >= 0; i--) {
            b = ar[i].action;
            if (doPacify) {
                if ((b instanceof EntityAIAttackRanged) || (b instanceof EntityAIAttackMelee) || (b instanceof EntitySpellcasterIllager.AIUseSpell) || (b instanceof EntitySpellcasterIllager.AICastingApell)
                        || (b instanceof EntityAICreeperSwell) || (b instanceof EntityAILeapAtTarget)) {
                    tasks.removeTask(b);
                } else if (b instanceof EntityAIMoveTowardsRestriction) {
                    tasks.removeTask(b);
                    if (doStayPrio != -123)
                        doStayPrio = ar[i].priority;
                } else if (b.getClass() == AIMobStayInVillage.class) {
                    doStayPrio = -123;
                }
            }
            if (doFollow >= 0) {
                if (b instanceof EntityAIFollowOwner) {
                    if (doFollowPrio != -123)
                        doFollowPrio = ar[i].priority;
                } else if (b.getClass() == AIMobFollowPlayer.class) {
                    if (doFollow > 0) {
                        doFollowPrio = -123;
                    } else {
                        tasks.removeTask(b);
                    }
                }
            }
            if (doFire >= 0) {
                if (b instanceof EntityAIPanic) {
                    if (doFire > 0) {
                        thePanic = b;
                        thePanicPrio = ar[i].priority;
                        tasks.removeTask(b);
                    } else {
                        thePanic = null;
                        thePanicPrio = -123;
                    }
                } else if (b.getClass() == AIMobSelfImmolation.class) {
                    if (doFire > 0) {
                        doFirePrio = -123;
                    } else {
                        if (thePanicPrio != -123) {
                            thePanic = ((AIMobSelfImmolation)b).getPanicAI();
                            thePanicPrio = ((AIMobSelfImmolation)b).getPanicPriority();
                        }
                        tasks.removeTask(b);
                    }
                }
            }
            ar[i] = null;
        }
        if (doPacify && (doStayPrio != -123))
            tasks.addTask(doStayPrio, new AIMobStayInVillage((EntityCreature)creature));
        if ((doFollow > 0) && (doFollowPrio != -123))
            tasks.addTask(doFollowPrio, new AIMobFollowPlayer(creature));
        if ((doFire > 0) && (doFirePrio != -123))
            tasks.addTask(doFirePrio, new AIMobSelfImmolation(creature, thePanic, thePanicPrio));
        if ((doFire == 0) && (thePanicPrio != -123) && (thePanic != null))
            tasks.addTask(thePanicPrio, thePanic);
    }

    /**
     *
     */
    private WandsCore() {
    }
}
