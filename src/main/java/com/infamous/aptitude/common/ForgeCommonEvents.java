package com.infamous.aptitude.common;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.entity.IAptitudeLlama;
import com.infamous.aptitude.common.entity.IRearable;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Aptitude.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEvents {

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event){
        if(event.getEntityLiving() instanceof IRearable){
            IRearable rearable = (IRearable) event.getEntityLiving();
            if(rearable.getAngrySoundCooldown() > 0){
                rearable.setAngrySoundCooldown(rearable.getAngrySoundCooldown() - 1);
            } else{
                rearable.setAngrySoundCooldown(0);
            }
        }
        if(event.getEntityLiving() instanceof IAptitudeLlama){
            IAptitudeLlama llama = (IAptitudeLlama) event.getEntityLiving();
            if(llama.getSpitCooldown() > 0){
                llama.setSpitCooldown(llama.getSpitCooldown() - 1);
            } else{
                llama.setSpitCooldown(0);
            }
        }
    }
}
