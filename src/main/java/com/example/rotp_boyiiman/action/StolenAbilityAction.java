package com.example.rotp_boyiiman.action;

import com.example.rotp_boyiiman.rps.StolenEnergy;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

/**
 * Слот украденной способности (ПКМ). Слотов {@link StolenEnergy#MAX_STOLEN}: каждая выигранная партия
 * отнимает у жертвы ещё одну способность и кладёт её в следующий свободный слот.
 * Пока слот пуст - это пустышка; когда в нём лежит способность, слот подменяется (replaceAction) на неё:
 * в HUD показывается её иконка, а клик выполняет её. Проигранная партия возвращает жертве одну способность.
 */
public class StolenAbilityAction extends StandAction {
    private final int slot;

    public StolenAbilityAction(StandAction.AbstractBuilder<?> builder, int slot) {
        super(builder);
        this.slot = slot;
    }

    @Override
    protected Action<IStandPower> replaceAction(IStandPower power, ActionTarget target) {
        StandAction stolen = StolenEnergy.getStolenAction(power.getUser(), slot);
        return stolen != null ? stolen : this;
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        return StolenEnergy.getStolenAction(user, slot) != null
                ? ActionConditionResult.POSITIVE : conditionMessage("no_stolen_ability");
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        // Сам слот ничего не делает: выполняется подставленная (украденная) способность.
    }
}
