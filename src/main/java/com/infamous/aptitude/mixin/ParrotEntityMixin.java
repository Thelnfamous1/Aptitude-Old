package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.util.AptitudeHelper;
import com.infamous.aptitude.common.util.AptitudePredicates;
import com.infamous.aptitude.common.util.AptitudeResources;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.UUID;

import net.minecraft.world.entity.AgableMob.AgableMobGroupData;

@Mixin(Parrot.class)
public abstract class ParrotEntityMixin extends ShoulderRidingEntity {

    @Shadow public abstract boolean isFood(ItemStack p_70877_1_);

    @Shadow public abstract int getVariant();

    protected ParrotEntityMixin(EntityType<? extends ShoulderRidingEntity> p_i48566_1_, Level p_i48566_2_) {
        super(p_i48566_1_, p_i48566_2_);
    }

    @ModifyVariable(at = @At("STORE"), method = "finalizeSpawn")
    private SpawnGroupData allowBabySpawn(@Nullable SpawnGroupData livingEntityData){
        if (livingEntityData instanceof AgableMob.AgableMobGroupData
                && !((AgableMobGroupData) livingEntityData).isShouldSpawnBaby()) {
            livingEntityData = new AgableMob.AgableMobGroupData(true);
        }
        return livingEntityData;
    }

    @Inject(at = @At("RETURN"), method = "isBaby", cancellable = true)
    private void animalIsBaby(CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(super.isBaby());
    }

    @Inject(at = @At("RETURN"), method = "canMate", cancellable = true)
    private void animalCanMate(Animal animal, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(super.canMate(animal));
    }

    @Inject(at = @At("RETURN"), method = "getBreedOffspring", cancellable = true)
    private void getAnimalBreedOffspring(ServerLevel serverWorld, AgableMob ageable, CallbackInfoReturnable<AgableMob> cir){
        Parrot babyParrot = EntityType.PARROT.create(serverWorld);

        if(babyParrot != null){
            if (this.random.nextBoolean()) {
                babyParrot.setVariant(this.getVariant());
            } else if(ageable instanceof Parrot){
                babyParrot.setVariant(((Parrot)ageable).getVariant());
            }

            UUID ownerUUID = this.getOwnerUUID();
            if (ownerUUID != null) {
                babyParrot.setOwnerUUID(ownerUUID);
                babyParrot.setTame(true);
            } else if(this.isTame()){
                babyParrot.setTame(true);
            }
        }
        cir.setReturnValue(babyParrot);
    }

    @Inject(at = @At("RETURN"), method = "isFood", cancellable = true)
    private void checkFoodTag(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(AptitudePredicates.PARROT_FOOD_PREDICATE.test(stack));
    }

    /**
     * @author Thelnfamous1
     * @reason Replacing hardcoded food checks with tags
     */
    @Overwrite
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.isFood(itemstack) && !this.isPoison(itemstack)) {
            if(!this.isTame()){
                if (!player.abilities.instabuild) {
                    itemstack.shrink(1);
                }

                if (!this.isSilent()) {
                    this.level.playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PARROT_EAT, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
                }

                if (!this.level.isClientSide) {
                    if (this.random.nextInt(10) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                        this.tame(player);
                        this.level.broadcastEntityEvent(this, (byte)7);
                    } else {
                        this.level.broadcastEntityEvent(this, (byte)6);
                    }
                }

                return InteractionResult.sidedSuccess(this.level.isClientSide);
            } else{
                return super.mobInteract(player, hand);
            }
        } else if (this.isPoison(itemstack)) {
            if (!player.abilities.instabuild) {
                itemstack.shrink(1);
            }

            this.addEffect(new MobEffectInstance(MobEffects.POISON, 900));
            if (player.isCreative() || !this.isInvulnerable()) {
                this.hurt(DamageSource.playerAttack(player), Float.MAX_VALUE);
            }

            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else if (!this.isFlying() && this.isTame() && this.isOwnedBy(player)) {
            if (!this.level.isClientSide) {
                this.setOrderedToSit(!this.isOrderedToSit());
            }

            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    private boolean isPoison(ItemStack itemstack) {
        return itemstack.getItem().is(AptitudeResources.PARROTS_CANNOT_EAT);
    }

    @Override
    public void usePlayerItem(Player player, ItemStack stack) {
        if(this.isFood(stack)){
            this.playSound(this.getEatingSound(stack), 1.0F, 1.0F);
            if(stack.isEdible()) {
                this.heal(stack.getItem().getFoodProperties().getNutrition());
                AptitudeHelper.addEatEffect(stack, this.level, this);
            }
        }
        super.usePlayerItem(player, stack);
    }

    @Shadow
    public abstract boolean isFlying();
}
