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
import net.william278.huskclaims.api.HuskClaimsAPI;
import net.william278.huskclaims.user.ClaimBlocksManager;
import net.william278.huskclaims.user.User;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VipClaimBlocksListener implements Listener {

    private final BukkitHuskClaims plugin;

    public VipClaimBlocksListener(BukkitHuskClaims plugin) {
        this.plugin = plugin;
    }

    public void register() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            plugin.getLogger().warning("LuckPerms not found! VIP claim blocks delivery system will not work.");
            return;
        }

        setupDatabase();

        Bukkit.getPluginManager().registerEvents(this, plugin);

        LuckPerms luckPerms = LuckPermsProvider.get();
        EventBus eventBus = luckPerms.getEventBus();

        eventBus.subscribe(plugin, NodeAddEvent.class, this::onNodeAdd);
        eventBus.subscribe(plugin, NodeRemoveEvent.class, this::onNodeRemove);
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().trim();
        String[] args = message.split(" ");
        String command = args[0].toLowerCase();

        if (command.equals("/blocos")) {
            Player player = event.getPlayer();
            handleBlocosCommand(player);
        }
    }

    private void handleBlocosCommand(Player player) {
        UUID uuid = player.getUniqueId();
        String username = player.getName();

        CompletableFuture.runAsync(() -> {
            try {
                LuckPerms luckPerms = LuckPermsProvider.get();
                net.luckperms.api.model.user.User lpUser = luckPerms.getUserManager().getUser(uuid);

                if (lpUser == null) {
                    try {
                        lpUser = luckPerms.getUserManager().loadUser(uuid).get();
                    } catch (Exception e) {
                        player.sendMessage("§e§lTerrenos §8» §cOcorreu um erro ao carregar seus dados de VIP. Tente novamente mais tarde.");
                        return;
                    }
                }

                if (lpUser == null) {
                    player.sendMessage("§e§lTerrenos §8» §cNão foi possível verificar suas permissões VIP.");
                    return;
                }

                boolean hasNetherite = hasVipPermission(lpUser, "mineskyclaims.vip.netherite");
                boolean hasDiamond = hasVipPermission(lpUser, "mineskyclaims.vip.diamante");
                boolean hasGold = hasVipPermission(lpUser, "mineskyclaims.vip.ouro");

                long targetBonus = hasNetherite ? 3000 : (hasDiamond ? 1500 : (hasGold ? 500 : 0));

                VipStatus status = getVipStatus(uuid);
                long currentBonus = status.netherite ? 3000 : (status.diamond ? 1500 : (status.gold ? 500 : 0));

                if (targetBonus > currentBonus) {
                    player.sendMessage("§e§lTerrenos §8» §aDetectamos blocos VIP pendentes! Processando o resgate...");
                    updateVipClaimBlocksDeferred(uuid, username);
                } else if (targetBonus < currentBonus) {
                    player.sendMessage("§e§lTerrenos §8» §cDetectamos uma mudança no seu VIP. Ajustando seus blocos...");
                    updateVipClaimBlocksDeferred(uuid, username);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Erro ao processar o comando /blocos para o jogador " + username + ": " + e.getMessage());
                player.sendMessage("§e§lTerrenos §8» §cOcorreu um erro interno ao processar seus blocos.");
            }
        });
    }

    private void onNodeAdd(NodeAddEvent event) {
        if (!event.isUser()) {
            return;
        }

        if (isVipRelatedNode(event.getNode())) {
            net.luckperms.api.model.user.User lpUser = (net.luckperms.api.model.user.User) event.getTarget();
            updateVipClaimBlocksDeferred(lpUser.getUniqueId(), lpUser.getUsername());
        }
    }

    private void onNodeRemove(NodeRemoveEvent event) {
        if (!event.isUser()) {
            return;
        }

        if (isVipRelatedNode(event.getNode())) {
            net.luckperms.api.model.user.User lpUser = (net.luckperms.api.model.user.User) event.getTarget();
            updateVipClaimBlocksDeferred(lpUser.getUniqueId(), lpUser.getUsername());
        }
    }

    private boolean isVipRelatedNode(net.luckperms.api.node.Node node) {
        String key = node.getKey().toLowerCase();

        if (key.equals("mineskyclaims.vip.ouro") ||
                key.equals("mineskyclaims.vip.diamante") ||
                key.equals("mineskyclaims.vip.netherite")) {
            return true;
        }

        return key.startsWith("group.");
    }

    private void updateVipClaimBlocksDeferred(UUID uuid, String username) {
        final String playerName = username != null ? username : "Player";

        runTaskOnMainThread(() -> {
            LuckPerms luckPerms = LuckPermsProvider.get();
            net.luckperms.api.model.user.User lpUser = luckPerms.getUserManager().getUser(uuid);

            if (lpUser == null) {
                return;
            }

            boolean hasNetherite = hasVipPermission(lpUser, "mineskyclaims.vip.netherite");
            boolean hasDiamond = hasVipPermission(lpUser, "mineskyclaims.vip.diamante");
            boolean hasGold = hasVipPermission(lpUser, "mineskyclaims.vip.ouro");

            long targetBonus = hasNetherite ? 3000 : (hasDiamond ? 1500 : (hasGold ? 500 : 0));

            VipStatus status = getVipStatus(uuid);
            long currentBonus = status.netherite ? 3000 : (status.diamond ? 1500 : (status.gold ? 500 : 0));

            if (targetBonus == currentBonus) {
                return;
            }

            final long difference = Math.abs(targetBonus - currentBonus);
            final boolean isAddition = targetBonus > currentBonus;

            final boolean newGold = hasNetherite || hasDiamond || hasGold;
            final boolean newDiamond = hasNetherite || hasDiamond;
            final boolean newNetherite = hasNetherite;

            User user = User.of(uuid, playerName);

            plugin.editClaimBlocks(
                    user,
                    ClaimBlocksManager.ClaimBlockSource.ADMIN_ADJUSTMENT,
                    (currentBlocks) -> {
                        plugin.getLogger().info("[MineSky VIP] Player: " + playerName + " | Current Blocks: " + currentBlocks + " | Diff: " + difference + " | Addition: " + isAddition);

                        long targetBlocks;
                        if (isAddition) {
                            targetBlocks = currentBlocks + difference;
                        } else {
                            targetBlocks = currentBlocks - difference;
                        }

                        long startingBlocks = plugin.getSettings().getClaims().getStartingClaimBlocks();
                        long spent = plugin.getSpentClaimBlocks(uuid);
                        long minimumAllowed = Math.max(startingBlocks, spent);

                        if (targetBlocks < minimumAllowed) {
                            return minimumAllowed;
                        }
                        return targetBlocks;
                    },
                    (newBalance) -> {
                        Player onlinePlayer = Bukkit.getPlayer(uuid);
                        if (isAddition) {
                            plugin.getLogger().info("Successfully added " + difference + " claim blocks to " + playerName + " (VIP Upgrade/Activation).");
                            if (onlinePlayer != null) {
                                onlinePlayer.sendMessage("§e§lTerrenos §8» §aSeus blocos de terrenos bônus VIP foram adicionados! Novo total: " + newBalance);
                            }
                        } else {
                            plugin.getLogger().info("Successfully removed " + difference + " claim blocks from " + playerName + " (VIP Expired/Downgrade).");
                            if (onlinePlayer != null) {
                                onlinePlayer.sendMessage("§e§lTerrenos §8» §cSeus blocos bônus de terreno expiraram devido ao fim do seu VIP! Novo total: " + newBalance);
                            }
                        }
                    }
            );

            saveVipStatus(uuid, newGold, newDiamond, newNetherite);
        });
    }

    private boolean hasVipPermission(net.luckperms.api.model.user.User lpUser, String permission) {
        boolean hasDirect = lpUser.getNodes().stream()
                .anyMatch(n -> n.getKey().equalsIgnoreCase(permission));
        if (hasDirect) {
            return true;
        }

        LuckPerms luckPerms = LuckPermsProvider.get();
        return lpUser.getNodes().stream()
                .filter(n -> n.getKey().startsWith("group."))
                .map(n -> n.getKey().substring("group.".length()))
                .map(groupName -> luckPerms.getGroupManager().getGroup(groupName))
                .filter(Objects::nonNull)
                .anyMatch(group -> group.getNodes().stream()
                        .anyMatch(n -> n.getKey().equalsIgnoreCase(permission)));
    }

    private void runTaskOnMainThread(Runnable runnable) {
        try {
            java.lang.reflect.Method getGlobalRegionSchedulerMethod = Bukkit.class.getMethod("getGlobalRegionScheduler");
            Object globalScheduler = getGlobalRegionSchedulerMethod.invoke(null);

            java.lang.reflect.Method runMethod = globalScheduler.getClass().getMethod(
                    "run",
                    org.bukkit.plugin.Plugin.class,
                    java.util.function.Consumer.class
            );

            java.util.function.Consumer<?> taskConsumer = (task) -> runnable.run();
            runMethod.invoke(globalScheduler, plugin, taskConsumer);

        } catch (Exception e) {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    private File getDatabaseFile() {
        return new File(plugin.getDataFolder(), "vip_claimsblocks.db");
    }

    private Connection getConnection() throws SQLException {
        File dbFile = getDatabaseFile();
        if (!dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
    }

    private void setupDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS vip_received (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "gold_received INTEGER DEFAULT 0, " +
                    "diamond_received INTEGER DEFAULT 0, " +
                    "netherite_received INTEGER DEFAULT 0" +
                    ")");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize VIP claims SQLite database: " + e.getMessage());
        }
    }

    private VipStatus getVipStatus(UUID uuid) {
        String query = "SELECT gold_received, diamond_received, netherite_received FROM vip_received WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new VipStatus(
                            rs.getInt("gold_received") == 1,
                            rs.getInt("diamond_received") == 1,
                            rs.getInt("netherite_received") == 1
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to query VIP claims status for " + uuid + ": " + e.getMessage());
        }
        return new VipStatus(false, false, false);
    }

    private void saveVipStatus(UUID uuid, boolean gold, boolean diamond, boolean netherite) {
        String sql = "INSERT INTO vip_received (uuid, gold_received, diamond_received, netherite_received) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET " +
                "gold_received = excluded.gold_received, " +
                "diamond_received = excluded.diamond_received, " +
                "netherite_received = excluded.netherite_received";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setInt(2, gold ? 1 : 0);
            pstmt.setInt(3, diamond ? 1 : 0);
            pstmt.setInt(4, netherite ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save VIP claims status for " + uuid + ": " + e.getMessage());
        }
    }

    private static class VipStatus {
        final boolean gold;
        final boolean diamond;
        final boolean netherite;

        VipStatus(boolean gold, boolean diamond, boolean netherite) {
            this.gold = gold;
            this.diamond = diamond;
            this.netherite = netherite;
        }
    }
}