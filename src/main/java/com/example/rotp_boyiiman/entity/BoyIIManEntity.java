package com.example.rotp_boyiiman.entity;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;

import net.minecraft.world.World;

/** Сущность стенда Boy II Man. Вся особенность стенда - в действиях и в RpsMatchManager. */
public class BoyIIManEntity extends StandEntity {

    public BoyIIManEntity(StandEntityType<BoyIIManEntity> type, World world) {
        super(type, world);
    }
}
