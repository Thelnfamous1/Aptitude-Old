package com.infamous.aptitude.client;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.client.renderer.layer.CatHeldItemLayer;
import com.infamous.aptitude.client.renderer.layer.OcelotHeldItemLayer;
import com.infamous.aptitude.client.renderer.layer.PolarBearHeldItemLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Aptitude.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event){
        event.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            EntityRenderDispatcher manager = minecraft.getEntityRenderDispatcher();
            Map<EntityType<?>, EntityRenderer<?>> renderers = manager.renderers;
            for(EntityRenderer<?> entityRenderer : renderers.values()){
                if(entityRenderer instanceof OcelotRenderer ocelotRenderer){
                    ocelotRenderer.addLayer(new OcelotHeldItemLayer<>(ocelotRenderer));
                }
                else if(entityRenderer instanceof CatRenderer ocelotRenderer){
                    ocelotRenderer.addLayer(new CatHeldItemLayer<>(ocelotRenderer));
                }
                else if(entityRenderer instanceof PolarBearRenderer polarBearRenderer){
                    polarBearRenderer.addLayer(new PolarBearHeldItemLayer<>(polarBearRenderer));
                }
            }
        });

    }
}
