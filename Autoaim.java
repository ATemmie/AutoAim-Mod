package com.modid.autoaimmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class AutoAimMod implements ClientModInitializer {
    private static boolean autoAimEnabled = false;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoaim.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.autoaim"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                autoAimEnabled = !autoAimEnabled;
            }

            if (autoAimEnabled && client.player != null) {
                aimAtNearestEntity(client);
            }
        });
    }

    private void aimAtNearestEntity(MinecraftClient client) {
        PlayerEntity player = client.player;
        World world = client.world;
        if (player == null || world == null) return;

        List<Entity> entities = world.getEntities();
        Entity target = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            if (isValidTarget(entity, player)) {
                double distance = player.squaredDistanceTo(entity);
                if (distance < 30 * 30 && distance < closestDistance) {
                    closestDistance = distance;
                    target = entity;
                }
            }
        }

        if (target != null) {
            adjustPlayerLook(player, target);
        }
    }

    private boolean isValidTarget(Entity entity, PlayerEntity player) {
        return entity instanceof LivingEntity &&
                !(entity instanceof ItemFrameEntity) &&
                !(entity instanceof AbstractMinecartEntity) &&
                !(entity instanceof ArmorStandEntity) &&
                entity != player;
    }

    private void adjustPlayerLook(PlayerEntity player, Entity target) {
        Vec3d targetPos = target.getPos().add(0, target.getEyeHeight(target.getPose()), 0);
        Vec3d playerPos = player.getEyePos();

        Vec3d direction = targetPos.subtract(playerPos).normalize();
        double yaw = Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90;
        double pitch = -Math.toDegrees(Math.asin(direction.y));

        player.setYaw((float) (player.getYaw() + 0.3 * MathHelper.wrapDegrees(yaw - player.getYaw())));
        player.setPitch((float) (player.getPitch() + 0.3 * MathHelper.wrapDegrees(pitch - player.getPitch())));
    }
}
