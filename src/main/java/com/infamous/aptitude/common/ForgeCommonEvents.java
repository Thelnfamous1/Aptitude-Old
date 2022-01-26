package com.infamous.aptitude.common;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.behavior.BehaviorType;
import com.infamous.aptitude.common.behavior.BrainManager;
import com.infamous.aptitude.common.behavior.util.BehaviorHelper;
import com.infamous.aptitude.common.entity.ICanSpit;
import com.infamous.aptitude.common.entity.IDevourer;
import com.infamous.aptitude.common.entity.IPredator;
import com.infamous.aptitude.common.entity.IRearing;
import com.infamous.aptitude.common.util.AptitudePredicates;
import com.infamous.aptitude.server.goal.animal.AptitudeBreedGoal;
import com.infamous.aptitude.server.goal.animal.AptitudeFollowParentGoal;
import com.infamous.aptitude.server.goal.misc.AptitudeTemptGoal;
import com.infamous.aptitude.server.goal.misc.DevourerFindItemsGoal;
import com.infamous.aptitude.server.goal.target.AptitudeDefendTargetGoal;
import com.infamous.aptitude.server.goal.target.AptitudeHurtByTargetGoal;
import com.infamous.aptitude.server.goal.target.HuntGoal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Aptitude.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEvents {

    @SubscribeEvent
    public static void onReloadListener(AddReloadListenerEvent event){
        Aptitude.brainManager = new BrainManager();
        event.addListener(Aptitude.brainManager);
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event){
        if(event.getWorld().isClientSide){
            return;
        }

        Mob eventMob = event.getEntity() instanceof Mob ? ((Mob) event.getEntity()) : null;
        if(eventMob == null) return;

        if(eventMob instanceof Pig pig && eventMob.getType() == EntityType.PIG){
            eventMob.goalSelector.removeAllGoals();
            eventMob.targetSelector.removeAllGoals();
            ResourceLocation pigLocation = ForgeRegistries.ENTITIES.getKey(EntityType.PIG);
            Set<MemoryModuleType<?>> memoryTypes = Aptitude.brainManager.getMemoryTypes(pigLocation);
            Set<SensorType<? extends Sensor<? super Pig>>> sensorTypes = Aptitude.brainManager.getSensorTypesUnchecked(pigLocation);
            Map<Integer, Map<Activity, Set<BehaviorType<?>>>> activitiesByPriority = Aptitude.brainManager.getActivitiesByPriority(pigLocation);
            BehaviorHelper.refreshBrain(pig, (ServerLevel) event.getWorld(), memoryTypes, sensorTypes, activitiesByPriority);
        }

        if(eventMob instanceof Cat){
            Cat cat = (Cat) eventMob;
            addCatGoals(cat);
        } else if(eventMob instanceof Dolphin){
            Dolphin dolphin = (Dolphin) eventMob;
            addDolphinGoals(dolphin);
        } else if(eventMob instanceof Fox){
            Fox fox = (Fox) eventMob;
            addFoxGoals(fox);
        } else if(eventMob instanceof AbstractHorse){
            AbstractHorse horse = (AbstractHorse) eventMob;
            addHorseGoals(horse);
        } else if(eventMob instanceof Ocelot){
            Ocelot ocelot = (Ocelot) eventMob;
            addOcelotGoals(ocelot);
        } else if(eventMob instanceof Parrot){
            Parrot parrot = (Parrot) eventMob;
            addParrotGoals(parrot);
        } else if(eventMob instanceof PolarBear){
            PolarBear polarBear = (PolarBear) eventMob;
            addPolarBearGoals(polarBear);
        }
    }

    private static void addCatGoals(Cat cat) {
        cat.goalSelector.addGoal(8, new DevourerFindItemsGoal<>(cat, AptitudePredicates.ALLOWED_ITEMS, 10));

        cat.targetSelector.addGoal(1, new AptitudeDefendTargetGoal<>(cat, LivingEntity.class, 10, false, false, AptitudePredicates.CAT_DEFEND_PREDICATE).setFollowDistanceFactor(0.25D));

        cat.setCanPickUpLoot(true);
    }

    private static void addDolphinGoals(Dolphin dolphin) {
        dolphin.goalSelector.addGoal(1, new AptitudeBreedGoal<>(dolphin, 1.25D, 60 * 10, 8.0D * 4, 3.0D));
        dolphin.goalSelector.addGoal(3, new AptitudeTemptGoal(dolphin, 1.25D, AptitudePredicates.DOLPHIN_FOOD_PREDICATE, false));
        dolphin.goalSelector.addGoal(4, new AptitudeFollowParentGoal<>(dolphin, 1.25D, 8.0D, 3.0D));

        dolphin.targetSelector.addGoal(2, new HuntGoal<>(dolphin, LivingEntity.class, 10, true, false, AptitudePredicates.DOLPHIN_PREY_PREDICATE));

        dolphin.setCanPickUpLoot(true);
    }

    private static void addDonkeyGoals(Donkey donkey) {
        donkey.targetSelector.addGoal(2, new AptitudeDefendTargetGoal<>(donkey, LivingEntity.class, 16, false, true, AptitudePredicates.DONKEY_DEFEND_PREDICATE).setFollowDistanceFactor(0.25F));
    }

    private static void addFoxGoals(Fox fox){
        fox.goalSelector.addGoal(3, new AptitudeTemptGoal(fox, 1.25D, AptitudePredicates.FOX_FOOD_PREDICATE, false));
    }

    private static void addHorseGoals(AbstractHorse horse) {
        if(!(horse instanceof Llama)){
            horse.targetSelector.addGoal(1, new AptitudeHurtByTargetGoal<>(horse));
        }
        if(horse instanceof Donkey){
            Donkey donkey = (Donkey) horse;
            addDonkeyGoals(donkey);
        }
        if(horse instanceof Mule){
            Mule mule = (Mule) horse;
            addMuleGoals(mule);
        }
    }

    private static void addMuleGoals(Mule mule) {
        mule.targetSelector.addGoal(2, new AptitudeDefendTargetGoal<>(mule, LivingEntity.class, 16, false, true, AptitudePredicates.MULE_DEFEND_PREDICATE).setFollowDistanceFactor(0.25F));
    }

    private static void addOcelotGoals(Ocelot ocelot) {
        ocelot.goalSelector.addGoal(8, new DevourerFindItemsGoal<>(ocelot, AptitudePredicates.ALLOWED_ITEMS, 10));

        ocelot.targetSelector.addGoal(1, new AptitudeDefendTargetGoal<>(ocelot, LivingEntity.class, 10, false, false, AptitudePredicates.OCELOT_DEFEND_PREDICATE).setFollowDistanceFactor(0.25D));

        ocelot.setCanPickUpLoot(true);
    }

    private static void addParrotGoals(Parrot parrot) {
        parrot.goalSelector.addGoal(1, new BreedGoal(parrot, 1.0D));
        parrot.goalSelector.addGoal(1, new AptitudeTemptGoal(parrot, 1.0D, false, AptitudePredicates.PARROT_FOOD_PREDICATE));
        parrot.goalSelector.addGoal(2, new FollowParentGoal(parrot, 1.1D));
    }

    private static void addPolarBearGoals(PolarBear polarBear) {
        polarBear.goalSelector.addGoal(1, new BreedGoal(polarBear, 1.25D));
        polarBear.goalSelector.addGoal(3, new AptitudeTemptGoal(polarBear, 1.25D, AptitudePredicates.POLAR_BEAR_FOOD_PREDICATE, false));
        polarBear.goalSelector.addGoal(8, new DevourerFindItemsGoal<>(polarBear, AptitudePredicates.ALLOWED_ITEMS, 10));

        polarBear.setCanPickUpLoot(true);
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
        if(event.getEntityLiving() instanceof IRearing){
            IRearing rearable = (IRearing) event.getEntityLiving();
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
