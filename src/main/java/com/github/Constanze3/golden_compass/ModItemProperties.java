package com.github.Constanze3.golden_compass;

import com.github.Constanze3.golden_compass.client.GoldenCompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import static com.github.Constanze3.golden_compass.Main.GOLDEN_COMPASS;

public class ModItemProperties {
    public static void register() {
        ItemProperties.register(
                GOLDEN_COMPASS.get(),
                new ResourceLocation("tracking"),
                (ClampedItemPropertyFunction) (itemStack, clientLevel, livingEntity, i) -> {
                    CompoundTag tag = itemStack.getOrCreateTag();

                    if (GoldenCompassItem.getTargetOffline(tag)) {
                        return 0.5F;
                    } else if (GoldenCompassItem.getTarget(tag).isPresent()) {
                        return 1.0F;
                    } else {
                        return 0.0F;
                    }
                }
        );

        ItemProperties.register(
                GOLDEN_COMPASS.get(),
                new ResourceLocation("angle"),
                new GoldenCompassItemPropertyFunction()
        );
    }
}
