package com.infamous.aptitude.mixin;

import com.infamous.aptitude.common.util.AptitudePredicates;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(ParrotEntity.class)
public abstract class ParrotEntityMixin extends ShoulderRidingEntity {

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

    @Inject(at = @At(value = "HEAD"), method = "mobInteract", cancellable = true)
    private void bypassParrotInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResultType> cir){
        if (player.isSecondaryUseActive()) {
            cir.setReturnValue(super.mobInteract(player, hand));
        }
    }
}
