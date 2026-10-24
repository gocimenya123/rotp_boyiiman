package com.example.rotp_boyiiman.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;

/**
 * Принудительный вызов: игра Камень-Ножницы-Бумага начинается сразу, без согласия цели.
 * Есть в слотах стенда с самого начала, но применить её можно только пока действует эффект Resolve
 * на максимальном (5-м) уровне решимости; иначе слот недоступен (серый), с подсказкой в сообщении.
 */
public class ForcedChallengeRPSAction extends ChallengeRPSAction {

    public ForcedChallengeRPSAction(StandEntityAction.AbstractBuilder<?> builder) {
        super(builder, true);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (!ResolveUtil.isMaxResolveActive(power)) {
            return conditionMessage("need_max_resolve");
        }
        return super.checkSpecificConditions(user, power, target);
    }
}
