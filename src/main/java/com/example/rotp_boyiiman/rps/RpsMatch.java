package com.example.rotp_boyiiman.rps;

import java.util.UUID;

import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;

/**
 * Состояние одного матча. Сама игра (раунды, меню, пакеты) - это RockPaperScissorsGame
 * из основного мода; здесь хранится только то, что нужно аддону для кражи энергии.
 */
public class RpsMatch {
    public enum Phase { INVITED, PLAYING }

    public final UUID challenger;   // владелец Boy II Man
    public final UUID target;       // цель
    public Phase phase = Phase.INVITED;
    public long lastActivity;

    /** Игра основного мода (появляется после принятия вызова). */
    public RockPaperScissorsGame game;
    public int lastRound = 1;
    /** Счёт, уже учтённый аддоном (чтобы заметить выигранный раунд). */
    public int challengerWins;
    public int targetWins;

    public RpsMatch(UUID challenger, UUID target, long now) {
        this.challenger = challenger;
        this.target = target;
        this.lastActivity = now;
    }
}
