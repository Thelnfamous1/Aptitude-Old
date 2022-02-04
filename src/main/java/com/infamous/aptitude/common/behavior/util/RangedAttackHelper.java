package com.infamous.aptitude.common.behavior.util;

import com.google.common.collect.Lists;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

public class RangedAttackHelper {
    
    public static void peformCrossbowShooting(Level level, LivingEntity shooter, LivingEntity target, InteractionHand handWithCrossbow, ItemStack crossbowStack, float shootingPower, float inaccuracy) {
        List<ItemStack> chargedProjectiles = getChargedProjectiles(crossbowStack);
        float[] shotPitches = getShotPitches(chargedProjectiles.size(), shooter.getRandom());

        for(int i = 0; i < chargedProjectiles.size(); ++i) {
            ItemStack chargedProjectile = chargedProjectiles.get(i);
            boolean infiniteAmmo = shooter instanceof Player && ((Player)shooter).getAbilities().instabuild;
            if (!chargedProjectile.isEmpty()) {
                float offset = getProjectileOffsetForIndex(i);
                shootCrossbowProjectile(level, shooter, target, handWithCrossbow, crossbowStack, chargedProjectile, shotPitches[i], infiniteAmmo, shootingPower, inaccuracy, offset);
            }
        }

        onCrossbowShot(level, shooter, crossbowStack);
    }

    public static float getProjectileOffsetForIndex(int index) {
        boolean odd = index % 2 != 0;
        int scale = odd ? (index + 1) / -2 : index / 2;
        return 10.0F * scale;
    }

    public static List<ItemStack> getChargedProjectiles(ItemStack crossbowStack) {
        List<ItemStack> list = Lists.newArrayList();
        CompoundTag compoundtag = crossbowStack.getTag();
        if (compoundtag != null && compoundtag.contains("ChargedProjectiles", 9)) {
            ListTag chargedProjectiles = compoundtag.getList("ChargedProjectiles", 10);
            if (chargedProjectiles != null) {
                for(int i = 0; i < chargedProjectiles.size(); ++i) {
                    CompoundTag compoundtag1 = chargedProjectiles.getCompound(i);
                    list.add(ItemStack.of(compoundtag1));
                }
            }
        }

        return list;
    }

    public static float[] getShotPitches(int projectileAmount, Random random) {
        boolean high = random.nextBoolean();
        float[] shotPitches = new float[projectileAmount];
        for(int i = 0; i < shotPitches.length; i++){
            if(i == 0) shotPitches[i] = 1.0F;
            else if(i % 2 != 0) shotPitches[i] = getRandomShotPitch(high, random);
            else shotPitches[i] = getRandomShotPitch(!high, random);
        }
        return shotPitches;
    }

    public static float getRandomShotPitch(boolean high, Random random) {
        float basePitch = high ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + basePitch;
    }

    public static void onCrossbowShot(Level level, LivingEntity shooter, ItemStack crossbowStack) {
        if (shooter instanceof ServerPlayer serverplayer) {
            if (!level.isClientSide) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger(serverplayer, crossbowStack);
            }

            serverplayer.awardStat(Stats.ITEM_USED.get(crossbowStack.getItem()));
        }

        clearChargedProjectiles(crossbowStack);
    }

    public static void clearChargedProjectiles(ItemStack crossbowStack) {
        CompoundTag compoundtag = crossbowStack.getTag();
        if (compoundtag != null) {
            ListTag chargedProjectiles = compoundtag.getList("ChargedProjectiles", 9);
            chargedProjectiles.clear();
            compoundtag.put("ChargedProjectiles", chargedProjectiles);
        }

    }

    public static void shootCrossbowProjectile(Level level, LivingEntity shooter, LivingEntity target, InteractionHand handWithCrossbow, ItemStack crossbowStack, ItemStack ammoStack, float soundPitch, boolean infiniteAmmo, float shootingPower, float inaccuracy, float offset) {
        if (!level.isClientSide) {
            boolean fireworkRocket = ammoStack.is(Items.FIREWORK_ROCKET);
            Projectile projectile;
            if (fireworkRocket) {
                projectile = new FireworkRocketEntity(level, ammoStack, shooter, shooter.getX(), shooter.getEyeY() - (double)0.15F, shooter.getZ(), true);
            } else {
                projectile = getCrossbowArrow(level, shooter, crossbowStack, ammoStack);
                boolean isExtraProjectile = offset != 0.0F;
                if (infiniteAmmo || isExtraProjectile) {
                    ((AbstractArrow)projectile).pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
            }

            shootCrossbowProjectile(shooter, target, projectile, shootingPower, inaccuracy, offset);

            crossbowStack.hurtAndBreak(fireworkRocket ? 3 : 1, shooter, (le) -> {
                le.broadcastBreakEvent(handWithCrossbow);
            });
            level.addFreshEntity(projectile);
            level.playSound((Player)null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, soundPitch);
        }
    }

    private static AbstractArrow getCrossbowArrow(Level level, LivingEntity shooter, ItemStack crossbowStack, ItemStack arrowStack) {
        ArrowItem arrowItem = (ArrowItem)(arrowStack.getItem() instanceof ArrowItem ? arrowStack.getItem() : Items.ARROW);
        AbstractArrow arrow = arrowItem.createArrow(level, arrowStack, shooter);
        if (shooter instanceof Player) {
            arrow.setCritArrow(true);
        }

        arrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        arrow.setShotFromCrossbow(true);
        int piercingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, crossbowStack);
        if (piercingLevel > 0) {
            arrow.setPierceLevel((byte)piercingLevel);
        }

        return arrow;
    }

    public static void shootCrossbowProjectile(LivingEntity shooter, LivingEntity target, Projectile projectile, float shootingPower, float inaccuracy, float offset) {
        double xDist = target.getX() - shooter.getX();
        double zDist = target.getZ() - shooter.getZ();
        double horDistSq = Math.sqrt(xDist * xDist + zDist * zDist);
        double yDist = target.getY(0.3333333333333333D) - projectile.getY() + horDistSq * (double)0.2F;
        Vector3f projectileShotVector = getProjectileShotVector(shooter, new Vec3(xDist, yDist, zDist), offset);
        projectile.shoot((double)projectileShotVector.x(), (double)projectileShotVector.y(), (double)projectileShotVector.z(), shootingPower, inaccuracy);
        shooter.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (shooter.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    public static Vector3f getProjectileShotVector(LivingEntity shooter, Vec3 targetDistVec, float offset) {
        Vec3 normTargetDistVec = targetDistVec.normalize();
        Vec3 crossTargetDistVec = normTargetDistVec.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (crossTargetDistVec.lengthSqr() <= 1.0E-7D) {
            crossTargetDistVec = normTargetDistVec.cross(shooter.getUpVector(1.0F));
        }

        Quaternion targetDistQuat = new Quaternion(new Vector3f(crossTargetDistVec), 90.0F, true);
        Vector3f normTargetDistVecf = new Vector3f(normTargetDistVec);
        normTargetDistVecf.transform(targetDistQuat);
        Quaternion targetDistQuadOffset = new Quaternion(normTargetDistVecf, offset, true);
        Vector3f normTargetDistVec1 = new Vector3f(normTargetDistVec);
        normTargetDistVec1.transform(targetDistQuadOffset);
        return normTargetDistVec1;
    }
}
