package com.example.rotp_boyiiman.action;

import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;

/** Условия по решимости (Resolve) для способностей аддона. */
public final class ResolveUtil {
    public static final int MAX_RESOLVE_LEVEL = 4;

    /** Достигнут максимальный уровень решимости (так же открываются способности высокого уровня у Heaven's Door). */
    public static boolean isMaxResolveLevel(IStandPower power) {
        // Как в Heaven's Door (BookGui): 5-я решимость = уровень 4 (уровни считаются с нуля).
        // getMaxResolveLevel() не используем - он может отличаться и тогда способность не откроется никогда.
        return power != null && power.getResolveLevel() >= MAX_RESOLVE_LEVEL;
    }

    /** Максимальный уровень решимости И эффект Resolve сейчас действует на владельце. */
    public static boolean isMaxResolveActive(IStandPower power) {
        if (!isMaxResolveLevel(power)) return false;
        LivingEntity user = power.getUser();
        return user != null && user.hasEffect(ModStatusEffects.RESOLVE.get());
    }

    private ResolveUtil() {}
}
