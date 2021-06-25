package com.infamous.aptitude.common;

import com.infamous.aptitude.Aptitude;
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
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.horse.*;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Aptitude.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEvents {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event){
        if(event.getWorld().isClientSide){
            return;
        }

        MobEntity eventMob = event.getEntity() instanceof MobEntity ? ((MobEntity) event.getEntity()) : null;
        if(eventMob == null) return;

        if(eventMob instanceof CatEntity){
            CatEntity cat = (CatEntity) eventMob;
            addCatGoals(cat);
        } else if(eventMob instanceof DolphinEntity){
            DolphinEntity dolphin = (DolphinEntity) eventMob;
            addDolphinGoals(dolphin);
        } else if(eventMob instanceof FoxEntity){
            FoxEntity fox = (FoxEntity) eventMob;
            addFoxGoals(fox);
        } else if(eventMob instanceof AbstractHorseEntity){
            AbstractHorseEntity horse = (AbstractHorseEntity) eventMob;
            addHorseGoals(horse);
        } else if(eventMob instanceof OcelotEntity){
            OcelotEntity ocelot = (OcelotEntity) eventMob;
            addOcelotGoals(ocelot);
        } else if(eventMob instanceof ParrotEntity){
            ParrotEntity parrot = (ParrotEntity) eventMob;
            addParrotGoals(parrot);
        } else if(eventMob instanceof PolarBearEntity){
            PolarBearEntity polarBear = (PolarBearEntity) eventMob;
            addPolarBearGoals(polarBear);
        }
    }

    private static void addCatGoals(CatEntity cat) {
        cat.goalSelector.addGoal(8, new DevourerFindItemsGoal<>(cat, AptitudePredicates.ALLOWED_ITEMS, 10));

        cat.targetSelector.addGoal(1, new AptitudeDefendTargetGoal<>(cat, LivingEntity.class, 10, false, false, AptitudePredicates.CAT_DEFEND_PREDICATE).setFollowDistanceFactor(0.25D));

        cat.setCanPickUpLoot(true);
    }

    private static void addDolphinGoals(DolphinEntity dolphin) {
        dolphin.goalSelector.addGoal(1, new AptitudeBreedGoal<>(dolphin, 1.25D, 60 * 10, 8.0D * 4, 3.0D));
        dolphin.goalSelector.addGoal(3, new AptitudeTemptGoal(dolphin, 1.25D, AptitudePredicates.DOLPHIN_FOOD_PREDICATE, false));
        dolphin.goalSelector.addGoal(4, new AptitudeFollowParentGoal<>(dolphin, 1.25D, 8.0D, 3.0D));

        dolphin.targetSelector.addGoal(2, new HuntGoal<>(dolphin, LivingEntity.class, 10, true, false, AptitudePredicates.DOLPHIN_PREY_PREDICATE));

        dolphin.setCanPickUpLoot(true);
    }

    private static void addDonkeyGoals(DonkeyEntity donkey) {
        donkey.targetSelector.addGoal(2, new AptitudeDefendTargetGoal<>(donkey, LivingEntity.class, 16, false, true, AptitudePredicates.DONKEY_DEFEND_PREDICATE).setFollowDistanceFactor(0.25F));
    }

    private static void addFoxGoals(FoxEntity fox){
        fox.goalSelector.addGoal(3, new AptitudeTemptGoal(fox, 1.25D, AptitudePredicates.FOX_FOOD_PREDICATE, false));
    }

    private static void addHorseGoals(AbstractHorseEntity horse) {
        if(!(horse instanceof LlamaEntity)){
            horse.targetSelector.addGoal(1, new AptitudeHurtByTargetGoal<>(horse));
        }
        if(horse instanceof DonkeyEntity){
            DonkeyEntity donkey = (DonkeyEntity) horse;
            addDonkeyGoals(donkey);
        }
        if(horse instanceof MuleEntity){
            MuleEntity mule = (MuleEntity) horse;
            addMuleGoals(mule);
        }
    }

    private static void addMuleGoals(MuleEntity mule) {
        mule.targetSelector.addGoal(2, new AptitudeDefendTargetGoal<>(mule, LivingEntity.class, 16, false, true, AptitudePredicates.MULE_DEFEND_PREDICATE).setFollowDistanceFactor(0.25F));
    }

    private static void addOcelotGoals(OcelotEntity ocelot) {
        ocelot.goalSelector.addGoal(8, new DevourerFindItemsGoal<>(ocelot, AptitudePredicates.ALLOWED_ITEMS, 10));

        ocelot.targetSelector.addGoal(1, new AptitudeDefendTargetGoal<>(ocelot, LivingEntity.class, 10, false, false, AptitudePredicates.OCELOT_DEFEND_PREDICATE).setFollowDistanceFactor(0.25D));

        ocelot.setCanPickUpLoot(true);
    }

    private static void addParrotGoals(ParrotEntity parrot) {
        parrot.goalSelector.addGoal(1, new BreedGoal(parrot, 1.0D));
        parrot.goalSelector.addGoal(1, new AptitudeTemptGoal(parrot, 1.0D, false, AptitudePredicates.PARROT_FOOD_PREDICATE));
        parrot.goalSelector.addGoal(2, new FollowParentGoal(parrot, 1.1D));
    }

    private static void addPolarBearGoals(PolarBearEntity polarBear) {
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
