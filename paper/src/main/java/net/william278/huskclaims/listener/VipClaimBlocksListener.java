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

import net.william278.huskclaims.BukkitHuskClaims;
import net.william278.huskclaims.user.ClaimBlocksManager;
import net.william278.huskclaims.user.User;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.Bukkit;

import java.util.UUID;

public class VipClaimBlocksListener {

    private final BukkitHuskClaims plugin;

    public VipClaimBlocksListener(BukkitHuskClaims plugin) {
        this.plugin = plugin;
    }

    public void register() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            plugin.getLogger().warning("LuckPerms not found! VIP claim blocks delivery system will not work.");
            return;
        }

        LuckPerms luckPerms = LuckPermsProvider.get();
        EventBus eventBus = luckPerms.getEventBus();
        eventBus.subscribe(plugin, NodeAddEvent.class, this::onNodeAdd);
    }

    private void onNodeAdd(NodeAddEvent event) {
        if (!event.isUser()) {
            return;
        }

        net.luckperms.api.model.user.User lpUser = (net.luckperms.api.model.user.User) event.getTarget();
        net.luckperms.api.node.Node addedNode = event.getNode();
        String addedKey = addedNode.getKey().toLowerCase();

        LuckPerms luckPerms = LuckPermsProvider.get();

        boolean isGold = false;
        boolean isDiamond = false;
        boolean isNetherite = false;

        if (addedKey.equals("mineskyclaims.vip.ouro")) {
            isGold = true;
        } else if (addedKey.equals("mineskyclaims.vip.diamante")) {
            isDiamond = true;
        } else if (addedKey.equals("mineskyclaims.vip.netherite")) {
            isNetherite = true;
        }
        else if (addedKey.startsWith("group.")) {
            String groupName = addedKey.substring("group.".length());
            net.luckperms.api.model.group.Group group = luckPerms.getGroupManager().getGroup(groupName);
            if (group != null) {
                isGold = group.getNodes().stream().anyMatch(n -> n.getKey().equalsIgnoreCase("mineskyclaims.vip.ouro"));
                isDiamond = group.getNodes().stream().anyMatch(n -> n.getKey().equalsIgnoreCase("mineskyclaims.vip.diamante"));
                isNetherite = group.getNodes().stream().anyMatch(n -> n.getKey().equalsIgnoreCase("mineskyclaims.vip.netherite"));
            }
        }

        if (!isGold && !isDiamond && !isNetherite) {
            return;
        }

        boolean goldReceived = lpUser.getNodes().stream()
                .filter(n -> n.getType() == NodeType.META)
                .map(n -> (MetaNode) n)
                .anyMatch(n -> n.getMetaKey().equalsIgnoreCase("claims_gold_received"));

        boolean diamondReceived = lpUser.getNodes().stream()
                .filter(n -> n.getType() == NodeType.META)
                .map(n -> (MetaNode) n)
                .anyMatch(n -> n.getMetaKey().equalsIgnoreCase("claims_diamond_received"));

        boolean netheriteReceived = lpUser.getNodes().stream()
                .filter(n -> n.getType() == NodeType.META)
                .map(n -> (MetaNode) n)
                .anyMatch(n -> n.getMetaKey().equalsIgnoreCase("claims_netherite_received"));

        long blocksToAdd = 0;
        boolean setGoldMeta = false;
        boolean setDiamondMeta = false;
        boolean setNetheriteMeta = false;

        if (isNetherite && !netheriteReceived) {
            if (diamondReceived) {
                blocksToAdd = 1500;
            } else if (goldReceived) {
                blocksToAdd = 2500;
            } else {
                blocksToAdd = 3000;
            }
            setGoldMeta = true;
            setDiamondMeta = true;
            setNetheriteMeta = true;
        }
        else if (isDiamond && !diamondReceived) {
            if (goldReceived) {
                blocksToAdd = 1000;
            } else {
                blocksToAdd = 1500;
            }
            setGoldMeta = true;
            setDiamondMeta = true;
        }
        else if (isGold && !goldReceived) {
            blocksToAdd = 500;
            setGoldMeta = true;
        }

        if (blocksToAdd <= 0) {
            return;
        }

        final long amount = blocksToAdd;
        final UUID uuid = lpUser.getUniqueId();
        final String username = lpUser.getUsername() != null ? lpUser.getUsername() : "Player";

        final boolean saveGold = setGoldMeta;
        final boolean saveDiamond = setDiamondMeta;
        final boolean saveNetherite = setNetheriteMeta;

        runTaskOnMainThread(() -> {
            User user = User.of(uuid, username);

            plugin.editClaimBlocks(
                    user,
                    ClaimBlocksManager.ClaimBlockSource.ADMIN_ADJUSTMENT,
                    (currentBlocks) -> currentBlocks + amount,
                    (newBalance) -> {
                        plugin.getLogger().info("Successfully added " + amount + " claim blocks to " + username + " (VIP).");
                        org.bukkit.entity.Player onlinePlayer = Bukkit.getPlayer(uuid);
                        if (onlinePlayer != null) {
                            onlinePlayer.sendMessage("§e§lTerrenos §8» §aSeus blocos de terrenos bônus VIP foram adicionados! Novo total: " + newBalance);
                        }
                    }
            );

            if (saveGold) {
                lpUser.data().add(MetaNode.builder("claims_gold_received", "true").build());
            }
            if (saveDiamond) {
                lpUser.data().add(MetaNode.builder("claims_diamond_received", "true").build());
            }
            if (saveNetherite) {
                lpUser.data().add(MetaNode.builder("claims_netherite_received", "true").build());
            }
            luckPerms.getUserManager().saveUser(lpUser);
        });
    }

    private void runTaskOnMainThread(Runnable runnable) {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            Bukkit.getGlobalRegionScheduler().run(plugin, (scheduledTask) -> runnable.run());
        } catch (ClassNotFoundException | NoSuchMethodError e) {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }
}