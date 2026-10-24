package com.example.rotp_boyiiman.client;

import com.example.rotp_boyiiman.AddonMain;
import com.example.rotp_boyiiman.entity.BoyIIManEntity;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class BoyIIManRenderer extends StandEntityRenderer<BoyIIManEntity, BoyIIManModel> {

    public BoyIIManRenderer(EntityRendererManager renderManager) {
        super(renderManager, new BoyIIManModel(),
                new ResourceLocation(AddonMain.MOD_ID, "textures/entity/stand/boy_ii_man.png"), 0);
    }
}
