package com.infamous.aptitude.client;

import com.infamous.aptitude.Aptitude;
import com.infamous.aptitude.common.entity.IAnimal;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Aptitude.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeClientEvents {
    private static final Map<EntityType<?>, Float> CACHED_ADULT_SHADOW_RADII = new HashMap<>();
    private static Field shadowRadiusField;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderLiving(final RenderLivingEvent.Pre event){
        LivingEntity renderedEntity = event.getEntity();
        if(shouldScaleForBaby(renderedEntity)){
            LivingRenderer renderer = event.getRenderer();
            float adultShadowRadius;

            if(CACHED_ADULT_SHADOW_RADII.containsKey(renderedEntity.getType())){
                adultShadowRadius = CACHED_ADULT_SHADOW_RADII.get(renderedEntity.getType());
            } else{
                adultShadowRadius = getShadowRadius(renderer);
                if(adultShadowRadius < 0){
                    return;
                }
                CACHED_ADULT_SHADOW_RADII.put(renderedEntity.getType(), adultShadowRadius);
            }

            if(renderedEntity.isBaby()){
                float babyScale = 0.5F;
                event.getMatrixStack().scale(babyScale, babyScale, babyScale);
                setShadowRadius(renderer, adultShadowRadius / 2);
            } else{
                setShadowRadius(renderer, adultShadowRadius);
            }
        }
    }

    private static boolean shouldScaleForBaby(LivingEntity living){
        return living instanceof IAnimal
                || living instanceof ParrotEntity;
    }

    private static float getShadowRadius(LivingRenderer renderer){
        if(shadowRadiusField == null){
            shadowRadiusField = ObfuscationReflectionHelper.findField(EntityRenderer.class, "field_76989_e");
        }
        try {
            return shadowRadiusField.getFloat(renderer);
        } catch (IllegalAccessException e) {
            Aptitude.LOGGER.error("Reflection error for getting LivingRender#shadowRadius on {}", renderer);
            return -1.0F;
        }
    }

    private static void setShadowRadius(LivingRenderer renderer, float shadowRadiusIn){
        if(shadowRadiusField == null){
            shadowRadiusField = ObfuscationReflectionHelper.findField(LivingRenderer.class, "field_76989_e");
        }
        try {
            shadowRadiusField.set(renderer, shadowRadiusIn);
        } catch (IllegalAccessException e) {
            Aptitude.LOGGER.error("Reflection error for setting LivingRender#shadowRadius on {}", renderer);
        }
    }
}
