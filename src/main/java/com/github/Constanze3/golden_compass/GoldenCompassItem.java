package com.github.Constanze3.golden_compass;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Optional;

public class GoldenCompassItem extends Item implements Vanishable {
    public static final String TAG_TARGET = "Target";
    public static final String TAG_TARGET_POS = "TargetPos";
    public static final String TAG_TARGET_DIMENSION = "TargetDimension";

    public GoldenCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(
            @NotNull ItemStack itemStack,
            @NotNull Player player,
            @NotNull LivingEntity entity,
            @NotNull InteractionHand hand
    ) {
        if (entity instanceof Player otherPlayer) {
            setTarget(itemStack.getOrCreateTag(), otherPlayer.getName().getString());
            player.setItemInHand(hand, itemStack);
            return InteractionResult.SUCCESS;
        }

        return  InteractionResult.FAIL;
    }

    @Override
    public void inventoryTick(
            @NotNull ItemStack itemStack,
            Level level,
            @NotNull Entity entity,
            int slot,
            boolean selected
    ) {
        if(level.isClientSide() || entity.getServer() == null) {
            return;
        }

        CompoundTag tag = itemStack.getOrCreateTag();
        Optional<String> target = getTarget(tag);

        if (target.isPresent()) {
            ServerPlayer targetPlayer =  entity.getServer().getPlayerList().getPlayerByName(target.get());

            if (targetPlayer != null) {
                BlockPos targetPos = targetPlayer.blockPosition();
                ResourceKey<Level> targetDimension = targetPlayer.level.dimension();

                updateTags(tag, targetPos, targetDimension);
            } else {
                // removeTags(tag);
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        removeTags(item.getOrCreateTag());

        return true;
    }

    public static Optional<String> getTarget(CompoundTag tag) {
        boolean hasTarget = tag.contains(TAG_TARGET);

        if (hasTarget) {
            return Optional.of(tag.getString(TAG_TARGET));
        }

        return Optional.empty();
    }

    private static Optional<ResourceKey<Level>> getTargetDimension(CompoundTag tag) {
        return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, tag.get(TAG_TARGET_DIMENSION)).result();
    }

    public static Optional<GlobalPos> getTargetPosition(CompoundTag tag) {
        boolean hasTargetPos = tag.contains(TAG_TARGET_POS);
        boolean hasTargetDimension = tag.contains(TAG_TARGET_DIMENSION);

        if (hasTargetPos && hasTargetDimension) {
            BlockPos targetPos = NbtUtils.readBlockPos(tag.getCompound(TAG_TARGET_POS));
            Optional<ResourceKey<Level>> dimension = getTargetDimension(tag);

            if (dimension.isPresent()) {
                return Optional.of(GlobalPos.of(dimension.get(), targetPos));
            }
        }

        return Optional.empty();
    }

    public static void setTarget(CompoundTag tag, String target) {
        tag.putString(TAG_TARGET, target);
    }

    public static void updateTags(CompoundTag tag, BlockPos targetPos, ResourceKey<Level> targetDimension) {
        Optional<Tag> encodedDimension = Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, targetDimension).result();

        if (encodedDimension.isPresent()) {
            tag.put(TAG_TARGET_POS, NbtUtils.writeBlockPos(targetPos));
            tag.put(TAG_TARGET_DIMENSION, encodedDimension.get());
        }
    }

    public static void removeTags(CompoundTag tag) {
        tag.remove(TAG_TARGET);
        tag.remove(TAG_TARGET_POS);
        tag.remove(TAG_TARGET_DIMENSION);
    }
}
