package com.example.rotp_boyiiman.init;

import com.example.rotp_boyiiman.AddonMain;
import com.example.rotp_boyiiman.action.ChallengeRPSAction;
import com.example.rotp_boyiiman.action.ForcedChallengeRPSAction;
import com.example.rotp_boyiiman.action.PunchAction;
import com.example.rotp_boyiiman.action.StolenAbilityAction;
import com.example.rotp_boyiiman.entity.BoyIIManEntity;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.action.stand.StandEntityBlock;
import com.github.standobyte.jojo.action.stand.StandEntityLightAttack;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.init.power.stand.EntityStandRegistryObject;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/**
 * Регистрация действий (Action) и стенда (StandType) Boy II Man.
 * Стиль повторяет RotP-Addon-example.
 */
public class InitStands {
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<Action<?>> ACTIONS = DeferredRegister.create(
            (Class<Action<?>>) ((Class<?>) Action.class), AddonMain.MOD_ID);
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<StandType<?>> STANDS = DeferredRegister.create(
            (Class<StandType<?>>) ((Class<?>) StandType.class), AddonMain.MOD_ID);

    // ========================= Действия =========================

    /** Обычный удар (ЛКМ). */
    public static final RegistryObject<StandEntityAction> B2M_PUNCH = ACTIONS.register("boy_ii_man_punch",
            () -> new PunchAction(new StandEntityLightAttack.Builder()
                    .punchSound(InitSounds.B2M_PUNCH)));

    /** Блок (ПКМ). */
    public static final RegistryObject<StandEntityAction> B2M_BLOCK = ACTIONS.register("boy_ii_man_block",
            StandEntityBlock::new);

    /** Вызов на Камень-Ножницы-Бумага (ПКМ, второй слот). */
    public static final RegistryObject<StandEntityAction> B2M_CHALLENGE = ACTIONS.register("boy_ii_man_challenge",
            () -> new ChallengeRPSAction(new StandEntityAction.Builder()
                    .standPerformDuration(10)));

    /** Принудительный вызов (ПКМ, третий слот): появляется на максимальном уровне решимости (как у Heaven's Door). */
    public static final RegistryObject<StandEntityAction> B2M_FORCED_CHALLENGE = ACTIONS.register("boy_ii_man_forced_challenge",
            () -> new ForcedChallengeRPSAction(new StandEntityAction.Builder()
                    .standPerformDuration(10)));

    /** Слоты украденных способностей (ПКМ, 4-6 слоты): каждая выигранная партия кладёт в них ещё одну способность. */
    public static final RegistryObject<StolenAbilityAction> B2M_STOLEN_ABILITY = ACTIONS.register("boy_ii_man_stolen_ability",
            () -> new StolenAbilityAction(new StandAction.Builder(), 0));
    public static final RegistryObject<StolenAbilityAction> B2M_STOLEN_ABILITY_2 = ACTIONS.register("boy_ii_man_stolen_ability_2",
            () -> new StolenAbilityAction(new StandAction.Builder(), 1));
    public static final RegistryObject<StolenAbilityAction> B2M_STOLEN_ABILITY_3 = ACTIONS.register("boy_ii_man_stolen_ability_3",
            () -> new StolenAbilityAction(new StandAction.Builder(), 2));

    // ========================= Стенд =========================

    public static final EntityStandRegistryObject<EntityStandType<StandStats>, StandEntityType<BoyIIManEntity>> STAND_BOY_II_MAN =
            new EntityStandRegistryObject<>("boy_ii_man",
                    STANDS,
                    () -> new EntityStandType.Builder<StandStats>()
                            .color(0x5FA88A) // jade
                            .storyPartName(ModStandsInit.PART_4_NAME)
                            .leftClickHotbar(B2M_PUNCH.get())
                            .rightClickHotbar(B2M_BLOCK.get(), B2M_CHALLENGE.get(), B2M_FORCED_CHALLENGE.get(),
                                    B2M_STOLEN_ABILITY.get(), B2M_STOLEN_ABILITY_2.get(), B2M_STOLEN_ABILITY_3.get())
                            .defaultStats(StandStats.class, new StandStats.Builder()
                                    .tier(3)
                                    .power(12.0)
                                    .speed(12.0)
                                    .range(4.0, 8.0)
                                    .durability(12.0)
                                    .precision(8.0)
                                    .randomWeight(1))
                            .build(),

                    InitEntities.ENTITIES,
                    () -> new StandEntityType<BoyIIManEntity>(BoyIIManEntity::new, 0.7F, 1.75F)
                            .summonSound(InitSounds.B2M_SUMMON)
                            .unsummonSound(InitSounds.B2M_UNSUMMON))
            .withDefaultStandAttributes();
}
