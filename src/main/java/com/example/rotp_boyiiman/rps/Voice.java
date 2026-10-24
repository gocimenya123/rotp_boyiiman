package com.example.rotp_boyiiman.rps;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/** Реплики владельца стенда: проигрываются на сервере в точке, где стоит игрок. Только для игроков. */
public final class Voice {

    public static void say(LivingEntity owner, SoundEvent line) {
        if (!(owner instanceof PlayerEntity)) return;
        World w = owner.level;
        if (w.isClientSide()) return;
        w.playSound(null, owner.getX(), owner.getY(), owner.getZ(), line, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    private Voice() {}
}
