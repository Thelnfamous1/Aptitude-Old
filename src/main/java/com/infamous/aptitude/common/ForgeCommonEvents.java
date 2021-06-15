package com.infamous.aptitude.common;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.entity.ICanSpit;
import com.infamous.aptitude.common.entity.IDevourer;
import com.infamous.aptitude.common.entity.IPredator;
import com.infamous.aptitude.common.entity.IRearable;
import com.infamous.aptitude.common.util.AptitudePredicates;
import com.infamous.aptitude.server.goal.animal.AptitudeBreedGoal;
import com.infamous.aptitude.server.goal.animal.AptitudeFollowParentGoal;
import com.infamous.aptitude.server.goal.misc.AptitudeTemptGoal;
import com.infamous.aptitude.server.goal.misc.DevourerFindItemsGoal;
import com.infamous.aptitude.server.goal.target.AptitudeDefendTargetGoal;
import com.infamous.aptitude.server.goal.target.AptitudeHurtByTargetGoal;
import com.infamous.aptitude.server.goal.target.HuntGoal;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = Aptitude.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEvents {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event){
        MobEntity eventMob = event.getEntity() instanceof MobEntity ? ((MobEntity) event.getEntity()) : null;
        if(eventMob instanceof AbstractHorseEntity && !(eventMob instanceof LlamaEntity)){
            eventMob.targetSelector.addGoal(1, new AptitudeHurtByTargetGoal((CreatureEntity) eventMob));
        } else if(eventMob instanceof DolphinEntity){
            /*
        Dolphins move around really fast, so we have to quadruple the parent/partner search distances to reduce search failures
         */
            eventMob.goalSelector.addGoal(1, new AptitudeBreedGoal<>(eventMob, 1.25D, 60 * 10, 8.0D * 4, 3.0D));
            eventMob.goalSelector.addGoal(3, new AptitudeTemptGoal((CreatureEntity) eventMob, 1.25D, AptitudePredicates.DOLPHIN_FOOD_PREDICATE, false));
            eventMob.goalSelector.addGoal(4, new AptitudeFollowParentGoal<>(eventMob, 1.25D, 8.0D, 3.0D));
            eventMob.targetSelector.addGoal(2, new HuntGoal<>(eventMob, LivingEntity.class, 10, true, false, AptitudePredicates.DOLPHIN_PREY_PREDICATE));
            eventMob.setCanPickUpLoot(true);
        } else if(eventMob instanceof OcelotEntity){
            eventMob.goalSelector.addGoal(8, new DevourerFindItemsGoal<>(eventMob, AptitudePredicates.OCELOT_ALLOWED_ITEMS, 10));
            eventMob.targetSelector.addGoal(1, new AptitudeDefendTargetGoal<>(eventMob, CreeperEntity.class, 10, false, false, (Predicate<LivingEntity>)null));
            eventMob.setCanPickUpLoot(true);
        } else if(eventMob instanceof PolarBearEntity){
            eventMob.goalSelector.addGoal(1, new BreedGoal((AnimalEntity) eventMob, 1.25D));
            eventMob.goalSelector.addGoal(3, new AptitudeTemptGoal((CreatureEntity) eventMob, 1.25D, AptitudePredicates.POLAR_BEAR_FOOD_PREDICATE, false));
            eventMob.goalSelector.addGoal(8, new DevourerFindItemsGoal<>(eventMob, AptitudePredicates.POLAR_BEAR_ALLOWED_ITEMS, 10));
            eventMob.setCanPickUpLoot(true);
        } else if(eventMob instanceof ParrotEntity){
            eventMob.goalSelector.addGoal(1, new BreedGoal((AnimalEntity) eventMob, 1.0D));
            eventMob.goalSelector.addGoal(1, new AptitudeTemptGoal((CreatureEntity) eventMob, 1.0D, false, AptitudePredicates.PARROT_FOOD_PREDICATE));
            eventMob.goalSelector.addGoal(2, new FollowParentGoal((AnimalEntity) eventMob, 1.1D));
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event){
        if(event.getEntityLiving() instanceof IDevourer){
            IDevourer eatsFood = (IDevourer) event.getEntityLiving();
            if(eatsFood.getEatCooldown() > 0){
                eatsFood.setEatCooldown(eatsFood.getEatCooldown() - 1);
            } else{
                eatsFood.setEatCooldown(0);
            }
        }
        if(event.getEntityLiving() instanceof IPredator){
            IPredator predator = (IPredator) event.getEntityLiving();
            if(predator.getHuntCooldown() > 0){
                predator.setHuntCooldown(predator.getHuntCooldown() - 1);
            } else{
                predator.setHuntCooldown(0);
            }
        }
        if(event.getEntityLiving() instanceof IRearable){
            IRearable rearable = (IRearable) event.getEntityLiving();
            if(rearable.getAngrySoundCooldown() > 0){
                rearable.setAngrySoundCooldown(rearable.getAngrySoundCooldown() - 1);
            } else{
                rearable.setAngrySoundCooldown(0);
            }
        }
        if(event.getEntityLiving() instanceof ICanSpit){
            ICanSpit llama = (ICanSpit) event.getEntityLiving();
            if(llama.getSpitCooldown() > 0){
                llama.setSpitCooldown(llama.getSpitCooldown() - 1);
            } else{
                llama.setSpitCooldown(0);
            }
        }
    }
}
