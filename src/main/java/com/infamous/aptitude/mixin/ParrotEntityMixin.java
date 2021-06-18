package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.util.AptitudeHelper;
import com.infamous.aptitude.common.util.AptitudePredicates;
import com.infamous.aptitude.common.util.AptitudeResources;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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

@Mixin(ParrotEntity.class)
public abstract class ParrotEntityMixin extends ShoulderRidingEntity {

    @Shadow public abstract boolean isFood(ItemStack p_70877_1_);

    @Shadow public abstract int getVariant();

    protected ParrotEntityMixin(EntityType<? extends ShoulderRidingEntity> p_i48566_1_, World p_i48566_2_) {
        super(p_i48566_1_, p_i48566_2_);
    }

    @ModifyVariable(at = @At("STORE"), method = "finalizeSpawn")
    private ILivingEntityData allowBabySpawn(@Nullable ILivingEntityData livingEntityData){
        if (livingEntityData instanceof AgeableEntity.AgeableData
                && !((AgeableData) livingEntityData).isShouldSpawnBaby()) {
            livingEntityData = new AgeableEntity.AgeableData(true);
        }
        return livingEntityData;
    }

    @Inject(at = @At("RETURN"), method = "isBaby", cancellable = true)
    private void animalIsBaby(CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(super.isBaby());
    }

    @Inject(at = @At("RETURN"), method = "canMate", cancellable = true)
    private void animalCanMate(AnimalEntity animal, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(super.canMate(animal));
    }

    @Inject(at = @At("RETURN"), method = "getBreedOffspring", cancellable = true)
    private void getAnimalBreedOffspring(ServerWorld serverWorld, AgeableEntity ageable, CallbackInfoReturnable<AgeableEntity> cir){
        ParrotEntity babyParrot = EntityType.PARROT.create(serverWorld);

        if(babyParrot != null){
            if (this.random.nextBoolean()) {
                babyParrot.setVariant(this.getVariant());
            } else if(ageable instanceof ParrotEntity){
                babyParrot.setVariant(((ParrotEntity)ageable).getVariant());
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
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.isFood(itemstack) && !this.isPoison(itemstack)) {
            if(!this.isTame()){
                if (!player.abilities.instabuild) {
                    itemstack.shrink(1);
                }

                if (!this.isSilent()) {
                    this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PARROT_EAT, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
                }

                if (!this.level.isClientSide) {
                    if (this.random.nextInt(10) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                        this.tame(player);
                        this.level.broadcastEntityEvent(this, (byte)7);
                    } else {
                        this.level.broadcastEntityEvent(this, (byte)6);
                    }
                }

                return ActionResultType.sidedSuccess(this.level.isClientSide);
            } else{
                return super.mobInteract(player, hand);
            }
        } else if (this.isPoison(itemstack)) {
            if (!player.abilities.instabuild) {
                itemstack.shrink(1);
            }

            this.addEffect(new EffectInstance(Effects.POISON, 900));
            if (player.isCreative() || !this.isInvulnerable()) {
                this.hurt(DamageSource.playerAttack(player), Float.MAX_VALUE);
            }

            return ActionResultType.sidedSuccess(this.level.isClientSide);
        } else if (!this.isFlying() && this.isTame() && this.isOwnedBy(player)) {
            if (!this.level.isClientSide) {
                this.setOrderedToSit(!this.isOrderedToSit());
            }

            return ActionResultType.sidedSuccess(this.level.isClientSide);
        } else {
            return ActionResultType.PASS;
        }
    }

    private boolean isPoison(ItemStack itemstack) {
        return itemstack.getItem().is(AptitudeResources.PARROTS_CANNOT_EAT);
    }

    @Override
    public void usePlayerItem(PlayerEntity player, ItemStack stack) {
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
