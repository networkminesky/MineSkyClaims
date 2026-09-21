/*
 * This file is part of HuskClaims, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskclaims.listener;

import lombok.Getter;
import net.william278.cloplib.listener.BukkitOperationListener;
import net.william278.cloplib.operation.OperationPosition;
import net.william278.cloplib.operation.OperationUser;
import net.william278.huskclaims.BukkitHuskClaims;
import net.william278.huskclaims.moderation.SignListener;
import net.william278.huskclaims.position.Position;
import net.william278.huskclaims.position.World;
import net.william278.huskclaims.user.OnlineUser;
import net.william278.huskclaims.user.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Getter
public class BukkitListener extends BukkitOperationListener implements BukkitPetListener, BukkitDropsListener,
        ClaimsListener, UserListener, SignListener {

    protected final BukkitHuskClaims plugin;

    public BukkitListener(@NotNull BukkitHuskClaims plugin) {
        super(plugin, plugin);
        this.plugin = plugin;
    }

    @Override
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setInspectorCallbacks();
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        this.onUserJoin(plugin.getOnlineUser(e.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        this.onUserQuit(plugin.getOnlineUser(e.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerSwitchHeldItem(@NotNull PlayerItemHeldEvent e) {
        final ItemStack mainHand = e.getPlayer().getInventory().getItem(e.getNewSlot());
        final ItemStack offHand = e.getPlayer().getInventory().getItemInOffHand();
        this.onUserSwitchHeldItem(
                plugin.getOnlineUser(e.getPlayer()),
                (mainHand != null ? mainHand.getType() : Material.AIR).getKey().toString(),
                offHand.getType().getKey().toString()
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onUserSwapHands(@NotNull PlayerSwapHandItemsEvent e) {
        final ItemStack mainHand = e.getMainHandItem();
        final ItemStack offHand = e.getOffHandItem();
        this.onUserSwitchHeldItem(
                plugin.getOnlineUser(e.getPlayer()),
                (mainHand != null ? mainHand.getType() : Material.AIR).getKey().toString(),
                (offHand != null ? offHand.getType() : Material.AIR).getKey().toString()
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUserTeleport(@NotNull PlayerTeleportEvent e) {
        final Location to = e.getTo();
        if (to == null || to.getWorld() == null) {
            return;
        }

        final Player player = e.getPlayer();
        final OnlineUser user = plugin.getOnlineUser(player);

        if (plugin.isIgnoringClaims(user) && plugin.hasIgnoreClaimsBanPermission(user)) {
            return;
        }

        final Position toPos = BukkitHuskClaims.Adapter.adapt(to);

        plugin.getClaimWorld(toPos.getWorld()).ifPresent(claimWorld -> {
            claimWorld.getClaimAt(toPos).ifPresent(claim -> {
                boolean isBanned = claimWorld.isBannedFromClaim(user, claim, plugin)
                        || (claim.isChildClaim() && claim.getParent()
                        .map(parent -> claimWorld.isBannedFromClaim(user, parent, plugin)).orElse(false));

                if (isBanned) {
                    e.setCancelled(true);

                    final Location fromLoc = e.getFrom();
                    plugin.runSync(() -> {
                        if (!player.isOnline()) return;

                        Position currentPos = BukkitHuskClaims.Adapter.adapt(player.getLocation());
                        if (claim.getRegion().contains(currentPos)) {
                            if (fromLoc.getWorld() != null && !claim.getRegion().contains(BukkitHuskClaims.Adapter.adapt(fromLoc))) {
                                player.teleport(fromLoc);
                            } else {
                                ejectPlayerOutsideClaim(player, claim);
                            }
                        }
                    });
                }
            });
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUserMove(@NotNull PlayerMoveEvent e) {
        if (e instanceof PlayerTeleportEvent) {
            return;
        }

        final Location to = e.getTo();
        final Location from = e.getFrom();
        if (to == null || (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        if (getPlugin().cancelMovement(
                plugin.getOnlineUser(e.getPlayer()),
                BukkitHuskClaims.Adapter.adapt(from),
                BukkitHuskClaims.Adapter.adapt(to)
        )) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHit(@NotNull ProjectileHitEvent e) {
        final Projectile projectile = e.getEntity();
        if (!(projectile.getShooter() instanceof Player player)) {
            return;
        }

        boolean isTeleportProjectile = projectile instanceof EnderPearl;

        if (!isTeleportProjectile && projectile instanceof AbstractArrow) {
            for (Entity passenger : projectile.getPassengers()) {
                if (passenger instanceof EnderPearl) {
                    isTeleportProjectile = true;
                    break;
                }
            }
        }

        if (!isTeleportProjectile) return;

        final Location hitLocation;
        if (e.getHitBlock() != null) {
            hitLocation = e.getHitBlock().getLocation().add(0.5, 1.0, 0.5);
        } else if (e.getHitEntity() != null) {
            hitLocation = e.getHitEntity().getLocation();
        } else {
            hitLocation = projectile.getLocation();
        }

        if (getPlugin().cancelMovement(
                plugin.getOnlineUser(player),
                BukkitHuskClaims.Adapter.adapt(player.getLocation()),
                BukkitHuskClaims.Adapter.adapt(hitLocation)
        )) {
            e.setCancelled(true);
            projectile.getPassengers().forEach(Entity::remove);
            projectile.remove();
        }
    }

    // Fix: End crystals are not treated as explosion in wilderness by cloplib, so their
    // block damage bypasses explosion_damage_terrain. Override here to handle them correctly.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEndCrystalExplode(@NotNull EntityExplodeEvent e) {
        if (!e.getEntity().getType().getKey().getKey().equals("end_crystal")) {
            return;
        }
        e.blockList().removeIf(block -> plugin.cancelOperation(
                net.william278.cloplib.operation.Operation.of(
                        net.william278.cloplib.operation.OperationType.EXPLOSION_DAMAGE_TERRAIN,
                        getPosition(block.getLocation())
                )
        ));
    }
        
    @EventHandler(ignoreCancelled = true)
    public void onWorldLoad(@NotNull WorldLoadEvent e) {
        plugin.runAsync(() -> {
            final World world = BukkitHuskClaims.Adapter.adapt(e.getWorld());
            plugin.loadClaimWorld(world);
            plugin.getClaimWorld(world).ifPresent(loaded -> plugin.getMapHooks().forEach(
                    hook -> hook.markClaims(loaded.getClaims(), loaded))
            );
        });
    }

    @Override
    public void onUserTamedEntityAction(@NotNull Cancellable event, @Nullable Entity player, @NotNull Entity entity) {
        // If pets are enabled, check if the entity is tamed
        if (player == null || !getPlugin().getSettings().getPets().isEnabled() || !(entity instanceof Tameable tamed)) {
            return;
        }

        // Check it was damaged by a player
        final Optional<Player> source = getPlayerSource(player);
        final Optional<User> owner = getPlugin().getPetOwner(tamed);
        if (source.isEmpty() || owner.isEmpty()) {
            return;
        }

        // Don't cancel the event if there's no mismatch
        if (getPlugin().cancelPetOperation(plugin.getOnlineUser(source.get()), owner.get())) {
            event.setCancelled(true);
        }
    }

    @Override
    @NotNull
    public OperationPosition getPosition(@NotNull Location location) {
        return BukkitHuskClaims.Adapter.adapt(location);
    }

    @Override
    @NotNull
    public OperationUser getUser(@NotNull Player player) {
        return plugin.getOnlineUser(player);
    }

    @Override
    public void setInspectionDistance(int i) {
        throw new UnsupportedOperationException("Cannot change inspection distance");
    }

    private void ejectPlayerOutsideClaim(@NotNull Player player, @NotNull net.william278.huskclaims.claim.Claim claim) {
        final org.bukkit.World world = player.getWorld();
        final net.william278.huskclaims.claim.Region region = claim.getRegion();

        int targetX = player.getLocation().getBlockX();
        int targetZ = player.getLocation().getBlockZ();

        int nearX = Math.max(region.getNearCorner().getBlockX(), Math.min(targetX, region.getFarCorner().getBlockX()));
        int nearZ = Math.max(region.getNearCorner().getBlockZ(), Math.min(targetZ, region.getFarCorner().getBlockZ()));

        if (Math.abs(targetX - region.getNearCorner().getBlockX()) < Math.abs(targetX - region.getFarCorner().getBlockX())) {
            nearX = region.getNearCorner().getBlockX() - 2;
        } else {
            nearX = region.getFarCorner().getBlockX() + 2;
        }

        int targetY = world.getHighestBlockYAt(nearX, nearZ) + 1;
        final Location safeLocation = new Location(world, nearX + 0.5, targetY, nearZ + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());

        player.teleport(safeLocation);
    }

}
