package com.github.Constanze3.golden_compass.client;

import com.github.Constanze3.golden_compass.GoldenCompassItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;


public class GoldenCompassItemPropertyFunction implements ClampedItemPropertyFunction {
    public GoldenCompassItemPropertyFunction() {

    }

    @Override
    public float unclampedCall(
            ItemStack goldenCompassStack,
            @Nullable ClientLevel clientLevel,
            @Nullable LivingEntity livingEntity,
            int i
    ) {
        Entity entity = livingEntity != null ? livingEntity : goldenCompassStack.getEntityRepresentation();
        if (entity == null) {
            return 0.0F;
        } else {
            ClientLevel level = tryFetchLevelIfMissing(entity, clientLevel);
            return level == null ? 0.0F : getCompassRotation(goldenCompassStack);
        }
    }

    @Nullable
    private ClientLevel tryFetchLevelIfMissing(Entity entity, @Nullable ClientLevel clientLevel) {
        if (clientLevel == null && entity.getLevel() instanceof ClientLevel) {
            return (ClientLevel) entity.getLevel();
        } else {
            return clientLevel;
        }
    }

    private float getCompassRotation(ItemStack goldenCompassStack) {
        GlobalPos targetPos =  GoldenCompassItem.getTargetPosition(goldenCompassStack.getOrCreateTag());
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null && targetPos != null && targetPos.dimension() == player.level.dimension()) {
            Vec3 pos = Vec3.atCenterOf(targetPos.pos());
            double angle = Math.atan2(pos.z() - player.getZ(), pos.x() - player.getX());

            // -1 = -180, 1 = 180
            double wrappedAngle = Mth.positiveModulo((float)(angle / 6.2831854820251465), 1.0F);
            double wrappedVisualRotation = Mth.positiveModulo(
                    player.getVisualRotationYInDegrees() / 360.0F, 1.0F
            );

            return (float)Mth.positiveModulo(wrappedAngle - wrappedVisualRotation - 0.25, 1.0F);
        }

        return 0.0F;
    }
}
