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

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.claim.ClaimWorld;
import net.william278.huskclaims.claim.Region;
import net.william278.huskclaims.guis.*;
import net.william278.huskclaims.highlighter.Highlightable;
import net.william278.huskclaims.managers.TrustInputManager;
import net.william278.huskclaims.position.Position;
import net.william278.huskclaims.trust.TrustLevel;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClaimMenuListener implements Listener {

    private final Plugin plugin;
    private final TrustInputManager inputManager;
    private BukkitHuskClaimsAPI api;

    public ClaimMenuListener(Plugin plugin, TrustInputManager inputManager) {
        this.plugin = plugin;
        this.inputManager = inputManager;
    }

    private BukkitHuskClaimsAPI getApi() {
        if (api == null) {
            api = BukkitHuskClaimsAPI.getInstance();
        }
        return api;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack current = event.getCurrentItem();
        if (current == null) return;

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (!title.contains("Gerenciador de Terreno") && !title.contains("Flags")
                && !title.contains("Banimentos") && !title.contains("Adicionar Confiança")
                && !title.contains("Membros Confiados") && !title.contains("Gerenciar:")) {
            return;
        }

        event.setCancelled(true);

        BukkitHuskClaimsAPI api = getApi();
        Position pos = api.getPosition(player.getLocation());
        Optional<ClaimWorld> worldOpt = api.getClaimWorld(api.getWorld(player.getWorld()));
        Optional<Claim> claimOpt = api.getClaimAt(pos);

        if (worldOpt.isEmpty() || claimOpt.isEmpty()) {
            player.closeInventory();
            player.sendMessage("§cVocê precisa estar dentro do terreno!");
            return;
        }

        Claim claim = claimOpt.get();
        ClaimWorld claimWorld = worldOpt.get();
        int rawSlot = event.getRawSlot();

        if (title.equals("Gerenciador de Terreno")) {
            switch (rawSlot) {
                case 20 -> ClaimTrustMenu.open(plugin, player, claim, claimWorld);
                case 22 -> ClaimFlagsMenu.open(plugin, player, claim, claimWorld, 0);
                case 24 -> ClaimBanMenu.open(plugin, player, claim, claimWorld);
                case 30 -> {
                    boolean newState = !claim.isPrivateClaim();
                    claim.setPrivateClaim(newState);
                    player.sendMessage(newState ? "§dO terreno agora é §c§lPRIVADO§d!" : "§dO terreno agora é §a§lPÚBLICO§d!");
                    ClaimMainMenu.open(plugin, player, claim, claimWorld);
                }
                case 32 -> {
                    player.closeInventory();
                    Position viewer = api.getPosition(player.getLocation());

                    Map<Region.Point, Highlightable.Type> points = claim.getHighlightPoints(
                            claimWorld,
                            false,
                            viewer,
                            128L
                    );

                    org.bukkit.World bukkitWorld = player.getWorld();
                    double playerY = player.getLocation().getY();

                    points.forEach((point, type) -> {
                        double x = point.getBlockX() + 0.5;
                        double z = point.getBlockZ() + 0.5;

                        int groundY = bukkitWorld.getHighestBlockYAt(point.getBlockX(), point.getBlockZ());
                        double y = (Math.abs(groundY - playerY) <= 10) ? groundY + 1.1 : playerY + 0.5;

                        if (type.name().contains("CORNER")) {
                            player.spawnParticle(org.bukkit.Particle.FLAME, x, y, z, 5, 0.05, 0.2, 0.05, 0.01);
                            player.spawnParticle(org.bukkit.Particle.END_ROD, x, y + 0.5, z, 2, 0.0, 0.0, 0.0, 0.01);
                        }
                        else {
                            player.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, x, y, z, 2, 0.1, 0.1, 0.1, 0);
                        }
                    });

                    player.sendMessage("§aAs bordas do terreno foram destacadas com partículas!");
                }
                case 40 -> {
                    player.closeInventory();
                    if (!claim.getOwner().get().equals(player.getUniqueId())) {
                        player.sendMessage("§cVocê não é o dono do terreno.");
                        return;
                    }
                    claimWorld.removeClaim(claim);
                    player.sendMessage("§cVocê abandonou o seu terreno com sucesso!");
                }
            }
        }

        else if (title.contains("Membros Confiados")) {
            if (rawSlot == 40) {
                ClaimMainMenu.open(plugin, player, claim, claimWorld);
                return;
            }

            if (rawSlot == 4) {
                TrustSelectionMenu.open(plugin, player, claim, claimWorld);
                return;
            }

            NamespacedKey key = new NamespacedKey(plugin, ClaimTrustMenu.TRUST_UUID_KEY);
            if (current.getItemMeta() != null && current.getItemMeta().getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
                String uuidStr = current.getItemMeta().getPersistentDataContainer().get(key, org.bukkit.persistence.PersistentDataType.STRING);
                if (uuidStr != null) {
                    try {
                        UUID targetUuid = UUID.fromString(uuidStr);
                        ClaimMemberManageMenu.open(plugin, player, claim, claimWorld, targetUuid);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }

        else if (title.contains("Gerenciar:")) {
            ItemStack headItem = event.getInventory().getItem(4);
            if (headItem == null || headItem.getItemMeta() == null) return;

            NamespacedKey key = new NamespacedKey(plugin, ClaimMemberManageMenu.TARGET_UUID_KEY);
            String targetUuidStr = headItem.getItemMeta().getPersistentDataContainer().get(key, org.bukkit.persistence.PersistentDataType.STRING);
            if (targetUuidStr == null) return;

            UUID targetUuid = UUID.fromString(targetUuidStr);

            if (rawSlot == 18) {
                ClaimTrustMenu.open(plugin, player, claim, claimWorld);
                return;
            }

            if (rawSlot == 15) {
                claim.getTrustedUsers().remove(targetUuid);
                player.sendMessage("§aO jogador foi removido do terreno com sucesso!");
                ClaimTrustMenu.open(plugin, player, claim, claimWorld);
                return;
            }

            String selectedLevelId = switch (rawSlot) {
                case 10 -> "access";
                case 11 -> "container";
                case 12 -> "build";
                case 13 -> "manage";
                default -> null;
            };

            if (selectedLevelId != null) {
                Optional<TrustLevel> optLevel = api.getTrustLevelByName(selectedLevelId);
                if (optLevel.isPresent()) {
                    TrustLevel level = optLevel.get();
                    claim.setUserTrustLevel(targetUuid, level);
                    player.sendMessage("§aNível de acesso alterado para: §e" + level.getDisplayName());
                    ClaimMemberManageMenu.open(plugin, player, claim, claimWorld, targetUuid);
                }
            }
        }


        else if (title.equals("Adicionar Confiança")) {
            if (rawSlot == 22) {
                ClaimTrustMenu.open(plugin, player, claim, claimWorld);
                return;
            }

            String trustLevelId = switch (current.getType()) {
                case OAK_DOOR -> "access";
                case CHEST -> "container";
                case CRAFTING_TABLE -> "build";
                case NETHER_STAR -> "manage";
                default -> null;
            };

            if (trustLevelId != null) {
                Optional<TrustLevel> levelOpt = api.getTrustLevelByName(trustLevelId);
                if (levelOpt.isPresent()) {
                    TrustLevel level = levelOpt.get();
                    inputManager.setWaitingTrust(player.getUniqueId(), claim, claimWorld, level);
                    player.closeInventory();
                    player.sendMessage("§8-----------------------------------------");
                    player.sendMessage("§eNível selecionado: §a" + level.getDisplayName());
                    player.sendMessage("§fDigite no chat o §enick do jogador §fque receberá o acesso.");
                    player.sendMessage("§7(Ou digite §ccancelar §7para abortar)");
                    player.sendMessage("§8-----------------------------------------");
                }
            }
        }

        else if (title.startsWith("Flags - Página")) {
            int currentPage = Integer.parseInt(title.split("Página ")[1].split("/")[0]) - 1;

            if (rawSlot == 45) {
                ClaimFlagsMenu.open(plugin, player, claim, claimWorld, currentPage - 1);
            } else if (rawSlot == 53) {
                ClaimFlagsMenu.open(plugin, player, claim, claimWorld, currentPage + 1);
            } else if (rawSlot == 49) {
                ClaimMainMenu.open(plugin, player, claim, claimWorld);
            } else {
                int[] slots = {
                        10, 11, 12, 13, 14, 15, 16,
                        19, 20, 21, 22, 23, 24, 25,
                        28, 29, 30, 31, 32, 33, 34
                };

                for (int i = 0; i < slots.length; i++) {
                    if (slots[i] == rawSlot) {
                        int flagIndex = (currentPage * 21) + i;
                        if (flagIndex < ClaimFlagsMenu.ALL_FLAGS.size()) {
                            String flagId = ClaimFlagsMenu.ALL_FLAGS.get(flagIndex).id();
                            ClaimFlagsMenu.toggleFlag(claim, flagId);
                            ClaimFlagsMenu.open(plugin, player, claim, claimWorld, currentPage);
                        }
                        break;
                    }
                }
            }
        }

        else if (title.contains("Banimentos")) {
            if (rawSlot == 40) {
                ClaimMainMenu.open(plugin, player, claim, claimWorld);
                return;
            }

            if (rawSlot == 4) {
                inputManager.setWaitingBan(player.getUniqueId(), claim, claimWorld);
                player.closeInventory();
                player.sendMessage("§8-----------------------------------------");
                player.sendMessage("§c§lBANIMENTO DE JOGADOR");
                player.sendMessage("§fDigite no chat o §enick do jogador §fque deseja banir.");
                player.sendMessage("§7(Ou digite §ccancelar §7para abortar)");
                player.sendMessage("§8-----------------------------------------");
                return;
            }

            NamespacedKey key = new NamespacedKey(plugin, ClaimBanMenu.BANNED_TAG);
            if (current.getItemMeta() != null && current.getItemMeta().getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
                String uuidString = current.getItemMeta().getPersistentDataContainer().get(key, org.bukkit.persistence.PersistentDataType.STRING);

                if (uuidString != null) {
                    try {
                        UUID targetUuid = UUID.fromString(uuidString);
                        claim.getBannedUsers().remove(targetUuid);
                        player.sendMessage("§aJogador desbanido do terreno com sucesso!");
                        ClaimBanMenu.open(plugin, player, claim, claimWorld);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
    }
}