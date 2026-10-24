package com.example.rotp_boyiiman;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.rotp_boyiiman.config.BoyIIManConfig;
import com.example.rotp_boyiiman.init.InitEntities;
import com.example.rotp_boyiiman.init.InitSounds;
import com.example.rotp_boyiiman.init.InitStands;
import com.example.rotp_boyiiman.net.B2MNetwork;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Точка входа аддона. Регистрирует все DeferredRegister и конфиг. */
@Mod(AddonMain.MOD_ID)
public class AddonMain {
    public static final String MOD_ID = "rotp_boyiiman";
    public static final Logger LOGGER = LogManager.getLogger();

    public AddonMain() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        InitEntities.ENTITIES.register(bus);
        InitSounds.SOUNDS.register(bus);
        InitStands.ACTIONS.register(bus);
        InitStands.STANDS.register(bus);
        B2MNetwork.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BoyIIManConfig.SPEC);
    }
}
