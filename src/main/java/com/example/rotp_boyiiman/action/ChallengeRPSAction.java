package com.example.rotp_boyiiman.action;

import com.example.rotp_boyiiman.init.InitSounds;
import com.example.rotp_boyiiman.rps.RpsMatchManager;
import com.example.rotp_boyiiman.rps.Voice;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;

/** Вызов цели (в небольшом радиусе) на Камень-Ножницы-Бумагу. Логика матча - в RpsMatchManager. */
public class ChallengeRPSAction extends StandEntityAction {
    private final boolean forced;

    public ChallengeRPSAction(StandEntityAction.AbstractBuilder<?> builder) {
        this(builder, false);
    }

    protected ChallengeRPSAction(StandEntityAction.AbstractBuilder<?> builder, boolean forced) {
        super(builder);
        this.forced = forced;
    }

    /** Нужна цель-сущность (на неё наводится курсор). */
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) return;
        ActionTarget target = task.getTarget();
        LivingEntity user = userPower.getUser();
        if (target.getType() == ActionTarget.TargetType.ENTITY
                && target.getEntity() instanceof LivingEntity
                && user instanceof ServerPlayerEntity) {
            Voice.say(user, (forced ? InitSounds.OWNER_FORCED : InitSounds.OWNER_CHALLENGE).get());
            RpsMatchManager.challenge((ServerPlayerEntity) user, (LivingEntity) target.getEntity(), forced);
        }
    }
}
