package com.example.rotp_boyiiman.init;

import com.example.rotp_boyiiman.AddonMain;

import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/** Реестр сущностей. Сущность стенда регистрируется через EntityStandRegistryObject в InitStands. */
public class InitEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, AddonMain.MOD_ID);
}
