package com.eleth.hotbarshuffler.client;

import com.eleth.hotbarshuffler.HotBarShuffler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HotbarShufflerClient implements ClientModInitializer {
    private static final Random RANDOM = new Random();
    private static final KeyMapping.Category HOTBAR_SHUFFLER_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(HotBarShuffler.MOD_ID, "controls"));

    private static KeyMapping toggleKey;
    private static boolean enabled;
    private static boolean pendingCheck;
    private static BlockPos pendingPrimaryPos;
    private static BlockState pendingPrimaryState;
    private static BlockPos pendingSecondaryPos;
    private static BlockState pendingSecondaryState;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.hotbarshuffler.toggle",
                GLFW.GLFW_KEY_R,
                HOTBAR_SHUFFLER_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                enabled = !enabled;

                if (client.player != null) {
                    client.player.displayClientMessage(Component.literal("Hotbar Shuffler: " + (enabled ? "ON" : "OFF")), true);
                }
            }

            if (pendingCheck && client.player != null && client.level != null) {
                Inventory inventory = client.player.getInventory();

                if (placementSucceeded(client.level)) {
                    inventory.setSelectedSlot(getRandomValidBlockSlot(inventory));
                }

                clearPendingCheck();
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!enabled || !world.isClientSide()) {
                return InteractionResult.PASS;
            }

            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof BlockItem) {
                pendingPrimaryPos = hitResult.getBlockPos();
                pendingPrimaryState = world.getBlockState(pendingPrimaryPos);
                pendingSecondaryPos = pendingPrimaryPos.relative(hitResult.getDirection());
                pendingSecondaryState = world.getBlockState(pendingSecondaryPos);
                pendingCheck = true;
            }

            return InteractionResult.PASS;
        });
    }

    private static int getRandomValidBlockSlot(Inventory inventory) {
        List<Integer> validSlots = new ArrayList<>();

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                validSlots.add(slot);
            }
        }

        if (validSlots.isEmpty()) {
            return inventory.getSelectedSlot();
        }

        return validSlots.get(RANDOM.nextInt(validSlots.size()));
    }

    private static boolean placementSucceeded(net.minecraft.world.level.Level level) {
        return stateChanged(level, pendingPrimaryPos, pendingPrimaryState)
                || stateChanged(level, pendingSecondaryPos, pendingSecondaryState);
    }

    private static boolean stateChanged(net.minecraft.world.level.Level level, BlockPos pos, BlockState previousState) {
        return pos != null && previousState != null && !level.getBlockState(pos).equals(previousState);
    }

    private static void clearPendingCheck() {
        pendingCheck = false;
        pendingPrimaryPos = null;
        pendingPrimaryState = null;
        pendingSecondaryPos = null;
        pendingSecondaryState = null;
    }
}
