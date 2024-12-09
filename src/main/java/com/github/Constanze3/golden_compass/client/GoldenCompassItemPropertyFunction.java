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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;


public class GoldenCompassItemPropertyFunction implements ClampedItemPropertyFunction {
    private final CompassWobble wobble = new CompassWobble();
    private final CompassWobble wobbleRandom = new CompassWobble();

    public GoldenCompassItemPropertyFunction() {}

    @Override
    public float unclampedCall(
            @NotNull ItemStack itemStack,
            @Nullable ClientLevel clientLevel,
            @Nullable LivingEntity livingEntity,
            int i
    ) {
        assert(itemStack.getItem() instanceof GoldenCompassItem);

        Entity entity = livingEntity != null ? livingEntity : itemStack.getEntityRepresentation();
        if (entity == null) {
            return 0.0F;
        } else {
            ClientLevel level = tryFetchLevelIfMissing(entity, clientLevel);
            return level == null ? 0.0F : getCompassRotation(itemStack, level);
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

    private float getCompassRotation(ItemStack itemStack, ClientLevel level) {
        long tick = level.getGameTime();

        Optional<GlobalPos> targetPos =  GoldenCompassItem.getTargetPosition(itemStack.getOrCreateTag());
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null && targetPos.isPresent()) {
            if (targetPos.get().dimension() == player.level.dimension()) {
                Vec3 pos = Vec3.atCenterOf(targetPos.get().pos());
                double angle = Math.atan2(pos.z() - player.getZ(), pos.x() - player.getX());

                // 0 = -180, 1 = 180
                double wrappedAngle = Mth.positiveModulo(angle / 6.2831854820251465, 1.0);
                double wrappedVisualRotation = Mth.positiveModulo(
                        player.getVisualRotationYInDegrees() / 360.0F, 1.0
                );

                if (this.wobble.shouldUpdate(tick)) {
                    this.wobble.update(tick, 0.5 - (wrappedVisualRotation - 0.25));
                }

                return (float)Mth.positiveModulo(
                        wrappedAngle + this.wobble.rotation,
                        1.0F
                );
            }
        }

        return 0.0F;
    }

    static class CompassWobble {
        double rotation;
        private double deltaRotation;
        private long lastUpdateTick;

        CompassWobble() {}

        boolean shouldUpdate(long tick) {
            return this.lastUpdateTick != tick;
        }

        void update(long tick, double targetRotation) {
            this.lastUpdateTick = tick;
            double diff = Mth.positiveModulo(targetRotation - this.rotation, 1);
            diff = Mth.positiveModulo(diff + 0.5, 1.0) - 0.5;
            this.deltaRotation += diff * 0.1;
            this.deltaRotation *= 0.8;
            this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0);
        }
    }
}
