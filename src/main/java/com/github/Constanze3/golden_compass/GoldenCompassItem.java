package com.github.Constanze3.golden_compass;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;

public class GoldenCompassItem extends Item implements Vanishable {
    public static final String TAG_TARGET = "Target";
    public static final String TAG_TARGET_POS = "TargetPos";
    public static final String TAG_TARGET_DIMENSION = "TargetDimension";

    public GoldenCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slot, boolean selected) {
        if(level.isClientSide() || !(entity instanceof ServerPlayer)) {
            return;
        }

        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag.contains("Target")) {
            String target = tag.getString(TAG_TARGET);
            ServerPlayer targetPlayer =  entity.getServer().getPlayerList().getPlayerByName(target);

            if (targetPlayer != null) {
                BlockPos targetPos = targetPlayer.blockPosition();
                ResourceKey<Level> targetDimension = targetPlayer.level.dimension();

                addTags(tag, targetPos, targetDimension);
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    private static Optional<ResourceKey<Level>> getTargetDimension(CompoundTag tag) {
        return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, tag.get(TAG_TARGET_DIMENSION)).result();
    }

    @Nullable
    public static GlobalPos getTargetPosition(CompoundTag tag) {
        boolean hasTargetPos = tag.contains(TAG_TARGET_POS);
        boolean hasTargetDimension = tag.contains(TAG_TARGET_DIMENSION);

        if (hasTargetPos && hasTargetDimension) {
            BlockPos targetPos = NbtUtils.readBlockPos(tag.getCompound(TAG_TARGET_POS));
            Optional<ResourceKey<Level>> dimension = getTargetDimension(tag);

            if (dimension.isPresent()) {
                return GlobalPos.of(dimension.get(), targetPos);
            }
        }

        return null;
    }

    @Nullable
    public static String getTarget(CompoundTag tag) {
        boolean hasTarget = tag.contains(TAG_TARGET);

        if (hasTarget) {
            return tag.getString(TAG_TARGET);
        }

        return null;
    }

    public static void addTags(CompoundTag tag, BlockPos targetPos, ResourceKey<Level> targetDimension) {
        Optional<Tag> encodedDimension = Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, targetDimension).result();

        if (encodedDimension.isPresent()) {
            tag.put(TAG_TARGET_POS, NbtUtils.writeBlockPos(targetPos));
            tag.put(TAG_TARGET_DIMENSION, encodedDimension.get());
        }
    }
}
