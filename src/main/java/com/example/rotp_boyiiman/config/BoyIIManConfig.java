package com.example.rotp_boyiiman.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Общий конфиг (config/rotp_boyiiman-common.toml). */
public final class BoyIIManConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_THEFT;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PLAYERS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_MOBS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_BOSSES;
    public static final ForgeConfigSpec.DoubleValue STOLEN_POWER_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ABILITY_STEAL;
    public static final ForgeConfigSpec.DoubleValue CHALLENGE_RANGE;
    public static final ForgeConfigSpec.IntValue INVITE_TIMEOUT_SECONDS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("theft");
        ENABLE_THEFT = b.comment("Enable energy theft via Rock-Paper-Scissors")
                .define("enableTheft", true);
        STOLEN_POWER_MULTIPLIER = b.comment("Multiplier of the buff the thief gets per stolen third")
                .defineInRange("stolenPowerMultiplier", 1.0D, 0.0D, 10.0D);
        ENABLE_ABILITY_STEAL = b.comment("Every match you win takes one more random right-click ability of the victim's Stand (not Block); "
                        + "every match the victim wins returns one. You can play the same person any number of times. "
                        + "Theft lasts until the Stand's owner dies.")
                .define("enableAbilitySteal", true);
        ALLOW_PLAYERS = b.comment("Allow stealing from other players").define("allowPlayers", true);
        ALLOW_MOBS = b.comment("Allow stealing from mobs that have a Stand").define("allowMobs", true);
        ALLOW_BOSSES = b.comment("Allow stealing from bosses (Wither, Ender Dragon)").define("allowBosses", false);
        b.pop();

        b.push("match");
        CHALLENGE_RANGE = b.comment("Max challenge distance (blocks)")
                .defineInRange("challengeRange", 8.0D, 2.0D, 64.0D);
        INVITE_TIMEOUT_SECONDS = b.comment("Seconds of inactivity before an invite/match is cancelled")
                .defineInRange("inviteTimeoutSeconds", 30, 5, 300);
        b.pop();

        SPEC = b.build();
    }

    private BoyIIManConfig() {}
}
