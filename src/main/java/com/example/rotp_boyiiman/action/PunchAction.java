package com.example.rotp_boyiiman.action;

import com.github.standobyte.jojo.action.stand.StandEntityLightAttack;

/**
 * Базовый удар Boy II Man (средняя сила - задаётся StandStats).
 * Тонкая обёртка: сюда удобно добавлять особенности удара (например, бонус за удерживаемую энергию).
 */
public class PunchAction extends StandEntityLightAttack {

    public PunchAction(StandEntityLightAttack.Builder builder) {
        super(builder);
    }
}
