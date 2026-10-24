package com.example.rotp_boyiiman.init;

import com.example.rotp_boyiiman.AddonMain;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Звуки. Звуки стенда в sounds.json пока ссылаются на ванильные события (плейсхолдеры);
 * фразы владельца - файлы sounds/user/*.ogg (заглушки-тишина, замени на свои записи с теми же именами).
 */
public class InitSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AddonMain.MOD_ID);

    public static final RegistryObject<SoundEvent> B2M_SUMMON = add("boy_ii_man_summon");
    public static final RegistryObject<SoundEvent> B2M_UNSUMMON = add("boy_ii_man_unsummon");
    public static final RegistryObject<SoundEvent> B2M_PUNCH = add("boy_ii_man_punch");
    public static final RegistryObject<SoundEvent> B2M_STEAL = add("boy_ii_man_steal");

    // --- Озвучка владельца стенда (Кэн Оянаги) ---
    public static final RegistryObject<SoundEvent> OWNER_SUMMON = add("owner_summon");
    public static final RegistryObject<SoundEvent> OWNER_CHALLENGE = add("owner_challenge");
    public static final RegistryObject<SoundEvent> OWNER_FORCED = add("owner_forced");
    public static final RegistryObject<SoundEvent> OWNER_ROUND_WIN = add("owner_round_win");
    public static final RegistryObject<SoundEvent> OWNER_ROUND_LOSE = add("owner_round_lose");
    public static final RegistryObject<SoundEvent> OWNER_STEAL = add("owner_steal");
    public static final RegistryObject<SoundEvent> OWNER_MATCH_WIN = add("owner_match_win");
    public static final RegistryObject<SoundEvent> OWNER_MATCH_LOSE = add("owner_match_lose");

    private static RegistryObject<SoundEvent> add(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(AddonMain.MOD_ID, name)));
    }
}
