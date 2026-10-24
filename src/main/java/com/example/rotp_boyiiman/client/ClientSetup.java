package com.example.rotp_boyiiman.client;

import com.example.rotp_boyiiman.AddonMain;
import com.example.rotp_boyiiman.init.InitStands;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Регистрация рендера стенда (только клиент). */
@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // ВАЖНО: в 1.16.5 регистрировать рендерер нужно НАПРЯМУЮ, без event.enqueueWork():
        // отложенная задача выполняется уже после создания EntityRendererManager,
        // и тогда у сущности нет рендерера (NullPointerException в shouldRender).
        RenderingRegistry.registerEntityRenderingHandler(
                InitStands.STAND_BOY_II_MAN.getEntityType(), BoyIIManRenderer::new);
    }
}
