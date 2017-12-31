package net.micmu.mcmods.micwands.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.model.ModelLoader;

import net.micmu.mcmods.micwands.MicWandsMod;

/**
 *
 * @author Micmu
 */
public abstract class ItemWand extends Item {

    /**
     *
     * @param name
     */
    public ItemWand(String name) {
        super();
        this.setFull3D();
        this.setRegistryName(MicWandsMod.MODID, name);
        this.setUnlocalizedName(MicWandsMod.MODID + '.' + name);
        this.setMaxStackSize(1);
        this.setMaxDamage(100);
        this.setNoRepair();
        this.setCreativeTab(CreativeTabs.TOOLS);
    }

    /**
     *
     * @param player
     * @param entity
     * @return
     */
    protected abstract int onWandInteract(EntityPlayer player, EntityLivingBase entity);

    /**
     *
     */
    protected void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }

    /**
     *
     */
    @Override
    public boolean itemInteractionForEntity(ItemStack itemstack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
        if (!entity.world.isRemote) {
            int r = onWandInteract(player, entity);
            if (r != 0) {
                // Consume wand durability
                int d;
                if (!player.isCreative()) {
                    d = Math.abs(r);
                    if (d < 10000)
                        itemstack.damageItem(d, entity);
                }
                d = getAnimationDuration(r > 0);
                if (d > 0) {
                    // Just play some spell animation on the (un)enchanted entity
                    if ((r > 0) && !entity.isPotionActive(MobEffects.WATER_BREATHING)) {
                        entity.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, d, 0));
                    } else if ((r < 0) && !entity.isPotionActive(MobEffects.NIGHT_VISION)) {
                        entity.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, d, 0));
                    }
                }
                // Produce successful use sound
                SoundEvent snd = getSoundSuccess(r > 0);
                if (snd != null)
                    entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, snd, SoundCategory.PLAYERS, 1.0F, getSoundSuccessPitch(r > 0));
            } else {
                // Produce failure sound
                entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
            return true;
        }
        return false;
    }

    /**
     * 
     * @param toggleOn
     * @return
     */
    protected int getAnimationDuration(boolean toggleOn) {
        return 60;
    }

    /**
     * 
     * @param toggleOn
     * @return
     */
    protected SoundEvent getSoundSuccess(boolean toggleOn) {
        return SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE;
    }

    /**
     * 
     * @param toggleOn
     * @return
     */
    protected float getSoundSuccessPitch(boolean toggleOn) {
        return toggleOn ? 1.0f : 1.2f;
    }

    /**
     *
     */
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    /**
     *
     */
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    /**
     *
     */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }
}
