package com.example.rotp_boyiiman.client;

import com.example.rotp_boyiiman.entity.BoyIIManEntity;
import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;

import net.minecraft.client.renderer.model.ModelRenderer;

/**
 * Модель Boy II Man по референсу (автор: inkegg): стальной «рыцарь» с остроконечным капюшоном-шлемом,
 * светящимися глазами и маской-клювом, массивными наплечниками и грудной пластиной с заклёпками,
 * сегментированным животом, кожаными напульсниками и перчатками с шипами.
 * Вместо ног - жёлто-зелёное пламя (ноги базового скелета RotP не создаются).
 *
 * Позы и анимации - штатный гуманоидный скелет RotP. Все доп. боксы и их UV сгенерированы вместе с
 * текстурой 128x128 (textures/entity/stand/boy_ii_man.png), поэтому при правке UV правь и текстуру.
 * Пламя крепится к upperPart (его начало координат - таз, ось Y направлена вниз).
 */
public class BoyIIManModel extends HumanoidStandModel<BoyIIManEntity> {

    public BoyIIManModel() {
        super();
        // Базовые боксы RotP, но без ног - их заменяет пламя.
        addHumanoidBaseBoxes(part -> part != leftLeg && part != leftLegJoint && part != leftLowerLeg
                && part != rightLeg && part != rightLegJoint && part != rightLowerLeg);

        box(head, 32, 0, -4F, -8F, -4F, 8, 8, 8, 0.8F); // hood
        box(head, 56, 22, -3F, -10.8F, -3F, 6, 2, 6, 0F); // spike1
        box(head, 18, 26, -2F, -12.8F, -2F, 4, 2, 4, 0F); // spike2
        box(head, 118, 28, -1F, -15.8F, -1F, 2, 3, 2, 0F); // spike3
        box(head, 122, 15, -0.5F, -17.6F, -0.5F, 1, 2, 1, 0F); // spike4
        box(head, 0, 34, -4F, -6.6F, -5.3F, 8, 1, 1, 0F); // brow
        box(head, 82, 37, -3F, -3.2F, -5.3F, 6, 3, 1, 0F); // mask
        box(head, 122, 11, -1F, -5.2F, -5.9F, 2, 3, 1, 0F); // beak
        box(torso, 34, 26, -3F, -1.5F, -2F, 6, 2, 4, 0.9F); // collar
        box(torso, 98, 11, -4F, 0F, -2F, 8, 6, 4, 1F); // chest
        box(upperPart, 18, 32, -4F, -4.6F, -2F, 8, 1, 4, 0.45F); // rib1
        box(upperPart, 42, 36, -4F, -2.8F, -2F, 8, 1, 4, 0.45F); // rib2
        box(upperPart, 0, 37, -4F, -1F, -2F, 8, 1, 4, 0.45F); // rib3
        box(leftArm, 64, 12, -3F, -3.5F, -3F, 6, 4, 6, 0.5F); // pauldronL
        box(leftArm, 66, 36, -2F, -4.8F, -2F, 4, 1, 4, 0.5F); // pauldronCapL
        box(leftArm, 0, 16, -2F, 0F, -2F, 4, 6, 4, 0.3F); // upperArmorL
        box(leftForeArm, 54, 30, -2F, 2.4F, -2F, 4, 2, 4, 0.55F); // bracerL
        box(leftForeArm, 70, 30, -2F, 4.5F, -2F, 4, 2, 4, 0.35F); // gloveL
        box(leftForeArm, 122, 18, -1.5F, 4.7F, -3F, 1, 1, 1, 0F); // spikeF0L
        box(leftForeArm, 88, 19, -0.5F, 4.7F, -3F, 1, 1, 1, 0F); // spikeF1L
        box(leftForeArm, 92, 19, 0.5F, 4.7F, -3F, 1, 1, 1, 0F); // spikeF2L
        box(leftForeArm, 124, 20, 2.4F, 4.7F, -0.5F, 1, 1, 1, 0F); // spikeOL
        box(leftForeArm, 124, 22, -3.4F, 4.7F, -0.5F, 1, 1, 1, 0F); // spikeIL
        box(rightArm, 16, 16, -3F, -3.5F, -3F, 6, 4, 6, 0.5F); // pauldronR
        box(rightArm, 24, 37, -2F, -4.8F, -2F, 4, 1, 4, 0.5F); // pauldronCapR
        box(rightArm, 40, 16, -2F, 0F, -2F, 4, 6, 4, 0.3F); // upperArmorR
        box(rightForeArm, 86, 31, -2F, 2.4F, -2F, 4, 2, 4, 0.55F); // bracerR
        box(rightForeArm, 102, 31, -2F, 4.5F, -2F, 4, 2, 4, 0.35F); // gloveR
        box(rightForeArm, 124, 24, -1.5F, 4.7F, -3F, 1, 1, 1, 0F); // spikeF0R
        box(rightForeArm, 124, 26, -0.5F, 4.7F, -3F, 1, 1, 1, 0F); // spikeF1R
        box(rightForeArm, 80, 28, 0.5F, 4.7F, -3F, 1, 1, 1, 0F); // spikeF2R
        box(rightForeArm, 84, 28, 2.4F, 4.7F, -0.5F, 1, 1, 1, 0F); // spikeOR
        box(rightForeArm, 42, 32, -3.4F, 4.7F, -0.5F, 1, 1, 1, 0F); // spikeIR
        box(upperPart, 98, 0, -4.5F, -1F, -3F, 9, 5, 6, 0F); // flame1
        box(upperPart, 64, 0, -5F, 3F, -3.5F, 10, 5, 7, 0F); // flame2
        box(upperPart, 88, 21, -4F, 7F, -3F, 8, 4, 6, 0F); // flame3
        box(upperPart, 0, 26, -2.5F, 10F, -2F, 5, 4, 4, 0F); // flame4
        boxRot(upperPart, 88, 12, 5F, 1F, -1F, 2, 5, 2, 0F, 0F, 0F, 0F, 0F, 0F, -0.35F); // tuftL1
        boxRot(upperPart, 116, 21, -7F, 1F, -1F, 2, 5, 2, 0F, 0F, 0F, 0F, 0F, 0F, 0.35F); // tuftR1
        boxRot(upperPart, 56, 16, 4F, 6F, 0.5F, 2, 4, 2, 0F, 0F, 0F, 0F, 0F, 0F, -0.5F); // tuftL2
        boxRot(upperPart, 80, 22, -6F, 6F, 0.5F, 2, 4, 2, 0F, 0F, 0F, 0F, 0F, 0F, 0.5F); // tuftR2
    }

    /** Доп. бокс: родитель, UV, смещение/размер, расширение (inflate). Точка крепления - начало координат родителя. */
    private void box(ModelRenderer parent, int u, int v, float x, float y, float z, int w, int h, int d,
            float inflate) {
        parent.addChild(makePart(u, v, x, y, z, w, h, d, inflate, 0.0F, 0.0F, 0.0F));
    }

    /** То же, но с точкой крепления (px, py, pz) и поворотом (rx, ry, rz) в радианах - для языков пламени. */
    private void boxRot(ModelRenderer parent, int u, int v, float x, float y, float z, int w, int h, int d,
            float inflate, float px, float py, float pz, float rx, float ry, float rz) {
        ModelRenderer part = makePart(u, v, x, y, z, w, h, d, inflate, px, py, pz);
        part.xRot = rx;
        part.yRot = ry;
        part.zRot = rz;
        parent.addChild(part);
    }

    private ModelRenderer makePart(int u, int v, float x, float y, float z, int w, int h, int d,
            float inflate, float px, float py, float pz) {
        ModelRenderer part = new ModelRenderer(this);
        part.setPos(px, py, pz);
        part.texOffs(u, v).addBox(x, y, z, (float) w, (float) h, (float) d, inflate);
        return part;
    }
}
