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
import net.william278.huskclaims.BukkitHuskClaims;
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.claim.ClaimWorld;
import net.william278.huskclaims.highlighter.Highlighter;
import net.william278.huskclaims.managers.TrustInputManager;
import net.william278.huskclaims.guis.*;
import net.william278.huskclaims.position.Position;
import net.william278.huskclaims.user.OnlineUser;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

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

        String rawTitle = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (!rawTitle.contains("Gerenciador") && !rawTitle.contains("Propriedades")
                && !rawTitle.contains("Banimentos") && !rawTitle.contains("Membros")
                && !rawTitle.contains("Gerenciar »") && !rawTitle.contains("Adicionar Confiança")
                && !rawTitle.contains("Confirmar Exclusão")) {
            return;
        }

        event.setCancelled(true);

        BukkitHuskClaimsAPI api = getApi();
        Position pos = api.getPosition(player.getLocation());
        Optional<ClaimWorld> worldOpt = api.getClaimWorld(api.getWorld(player.getWorld()));
        Optional<Claim> claimOpt = api.getClaimAt(pos);

        if (worldOpt.isEmpty() || claimOpt.isEmpty()) {
            player.closeInventory();
            MenuHelper.playToggleOff(player);
            player.sendMessage(MenuHelper.text("<#EF4444>Você precisa estar dentro do terreno!</#EF4444>"));
            return;
        }

        Claim claim = claimOpt.get();
        ClaimWorld claimWorld = worldOpt.get();
        int rawSlot = event.getRawSlot();

        if (rawTitle.contains("Gerenciador de Terrenos")) {
            switch (rawSlot) {
                case 20 -> {
                    MenuHelper.playClick(player);
                    ClaimTrustMenu.open(plugin, player, claim, claimWorld);
                }
                case 22 -> {
                    MenuHelper.playClick(player);
                    ClaimFlagsMenu.open(plugin, player, claim, claimWorld, 0);
                }
                case 24 -> {
                    MenuHelper.playClick(player);
                    ClaimBanMenu.open(plugin, player, claim, claimWorld);
                }
                case 30 -> {
                    boolean newState = !claim.isPrivateClaim();
                    claim.setPrivateClaim(newState);
                    if (newState) {
                        MenuHelper.playToggleOff(player);
                    } else {
                        MenuHelper.playToggleOn(player);
                    }
                    player.sendMessage(MenuHelper.text(newState
                            ? "<#EF4444>✦ O terreno agora é PRIVADO (apenas membros podem entrar).</#EF4444>"
                            : "<#22C55E>✦ O terreno agora é PÚBLICO (aberto a visitantes).</#22C55E>"));
                    ClaimMainMenu.open(plugin, player, claim, claimWorld);
                }
                case 32 -> {
                    player.closeInventory();
                    BukkitHuskClaims huskPlugin = (BukkitHuskClaims) plugin;
                    OnlineUser onlineUser = huskPlugin.getOnlineUser(player);
                    Highlighter highlighter = huskPlugin.getHighlighter(onlineUser);

                    if (highlighter != null) {
                        highlighter.stopHighlighting(onlineUser);
                        highlighter.startHighlighting(
                                onlineUser,
                                api.getWorld(player.getWorld()),
                                java.util.List.of(claim)
                        );
                        MenuHelper.playHighlight(player);
                        player.sendMessage(MenuHelper.text("<#FFB84D>✦ As bordas do terreno foram destacadas com blocos brilhantes!</#FFB84D>"));
                    }
                }
                case 40 -> {
                    boolean isOwner = claim.getOwner().map(uuid -> uuid.equals(player.getUniqueId())).orElse(false);
                    boolean isAdmin = player.hasPermission("huskclaims.admin_claim");

                    if (!isOwner && !isAdmin) {
                        MenuHelper.playToggleOff(player);
                        player.sendMessage(MenuHelper.text("<#EF4444>✦ Apenas o dono do terreno pode abandoná-lo!</#EF4444>"));
                        return;
                    }

                    MenuHelper.playClick(player);
                    ClaimDeleteConfirmMenu.open(plugin, player, claim, claimWorld);
                }
            }
        }

        else if (rawTitle.contains("Confirmar Exclusão")) {
            if (rawSlot == 15) {
                MenuHelper.playPage(player);
                ClaimMainMenu.open(plugin, player, claim, claimWorld);
                return;
            }

            if (rawSlot == 11) {
                player.closeInventory();
                MenuHelper.playDanger(player);
                claimWorld.removeClaim(claim);
                player.sendMessage(MenuHelper.text("<#EF4444>✦ Você abandonou o seu terreno! Seus blocos foram devolvidos.</#EF4444>"));
            }
        }

        else if (rawTitle.contains("Membros Confiados")) {
            if (rawSlot == 40) {
                MenuHelper.playPage(player);
                ClaimMainMenu.open(plugin, player, claim, claimWorld);
                return;
            }

            if (rawSlot == 4) {
                MenuHelper.playClick(player);
                TrustSelectionMenu.open(plugin, player, claim, claimWorld);
                return;
            }

            NamespacedKey key = new NamespacedKey(plugin, ClaimTrustMenu.TRUST_UUID_KEY);
            if (current.getItemMeta() != null && current.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                String uuidStr = current.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
                if (uuidStr != null) {
                    try {
                        UUID targetUuid = UUID.fromString(uuidStr);
                        MenuHelper.playClick(player);
                        ClaimMemberManageMenu.open(plugin, player, claim, claimWorld, targetUuid);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }

        else if (rawTitle.contains("Gerenciar »")) {
            ItemStack headItem = event.getInventory().getItem(4);
            if (headItem == null || headItem.getItemMeta() == null) return;

            NamespacedKey key = new NamespacedKey(plugin, ClaimMemberManageMenu.TARGET_UUID_KEY);
            String targetUuidStr = headItem.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (targetUuidStr == null) return;

            UUID targetUuid = UUID.fromString(targetUuidStr);

            if (rawSlot == 18) {
                MenuHelper.playPage(player);
                ClaimTrustMenu.open(plugin, player, claim, claimWorld);
                return;
            }

            if (rawSlot == 15) {
                claim.getTrustedUsers().remove(targetUuid);
                MenuHelper.playDanger(player);
                player.sendMessage(MenuHelper.text("<#EF4444>✦ O jogador foi removido do terreno com sucesso!</#EF4444>"));
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
                api.getTrustLevelByName(selectedLevelId).ifPresent(level -> {
                    claim.setUserTrustLevel(targetUuid, level);
                    MenuHelper.playSuccess(player);
                    player.sendMessage(MenuHelper.text("<#22C55E>✦ Nível de acesso alterado para: <white>" + level.getDisplayName() + "</white>"));
                    ClaimMemberManageMenu.open(plugin, player, claim, claimWorld, targetUuid);
                });
            }
        }

        else if (rawTitle.contains("Adicionar Confiança")) {
            if (rawSlot == 22) {
                MenuHelper.playPage(player);
                ClaimTrustMenu.open(plugin, player, claim, claimWorld);
                return;
            }

            String trustLevelId = switch (rawSlot) {
                case 10 -> "access";
                case 12 -> "container";
                case 14 -> "build";
                case 16 -> "manage";
                default -> null;
            };

            if (trustLevelId != null) {
                api.getTrustLevelByName(trustLevelId).ifPresent(level -> {
                    inputManager.setWaitingTrust(player.getUniqueId(), claim, claimWorld, level);
                    player.closeInventory();
                    MenuHelper.playPrompt(player);
                    player.sendMessage(MenuHelper.text("<#78716C>-----------------------------------------</#78716C>"));
                    player.sendMessage(MenuHelper.text("<#22C55E>+ Nível Selecionado: <white>" + level.getDisplayName() + "</white>"));
                    player.sendMessage(MenuHelper.text("<#E2E8F0>Digite no chat o <#FACC15>nick do jogador</#FACC15> que receberá o acesso.</#E2E8F0>"));
                    player.sendMessage(MenuHelper.text("<#78716C>(Ou digite <#EF4444>cancelar</#EF4444> para abortar)</#78716C>"));
                    player.sendMessage(MenuHelper.text("<#78716C>-----------------------------------------</#78716C>"));
                });
            }
        }

        else if (rawTitle.contains("Propriedades »")) {
            int currentPage = 0;
            try {
                currentPage = Integer.parseInt(rawTitle.split("Pág. ")[1].split("/")[0]) - 1;
            } catch (Exception ignored) {}

            if (rawSlot == 45) {
                MenuHelper.playPage(player);
                ClaimFlagsMenu.open(plugin, player, claim, claimWorld, currentPage - 1);
            } else if (rawSlot == 53) {
                MenuHelper.playPage(player);
                ClaimFlagsMenu.open(plugin, player, claim, claimWorld, currentPage + 1);
            } else if (rawSlot == 49) {
                MenuHelper.playPage(player);
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
                            boolean wasActive = ClaimFlagsMenu.isFlagActive(claim, flagId);

                            ClaimFlagsMenu.toggleFlag(claim, flagId);

                            if (!wasActive) {
                                MenuHelper.playToggleOn(player);
                            } else {
                                MenuHelper.playToggleOff(player);
                            }

                            ClaimFlagsMenu.open(plugin, player, claim, claimWorld, currentPage);
                        }
                        break;
                    }
                }
            }
        }

        else if (rawTitle.contains("Banimentos")) {
            if (rawSlot == 40) {
                MenuHelper.playPage(player);
                ClaimMainMenu.open(plugin, player, claim, claimWorld);
                return;
            }

            if (rawSlot == 4) {
                inputManager.setWaitingBan(player.getUniqueId(), claim, claimWorld);
                player.closeInventory();
                MenuHelper.playPrompt(player);
                player.sendMessage(MenuHelper.text("<#78716C>-----------------------------------------</#78716C>"));
                player.sendMessage(MenuHelper.text("<#EF4444>+ Banimento de Jogador</#EF4444>"));
                player.sendMessage(MenuHelper.text("<#E2E8F0>Digite no chat o <#FACC15>nick do jogador</#FACC15> que deseja banir.</#E2E8F0>"));
                player.sendMessage(MenuHelper.text("<#78716C>(Ou digite <#EF4444>cancelar</#EF4444> para abortar)</#78716C>"));
                player.sendMessage(MenuHelper.text("<#78716C>-----------------------------------------</#78716C>"));
                return;
            }

            NamespacedKey key = new NamespacedKey(plugin, ClaimBanMenu.BANNED_TAG);
            if (current.getItemMeta() != null && current.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                String uuidString = current.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
                if (uuidString != null) {
                    try {
                        UUID targetUuid = UUID.fromString(uuidString);
                        claim.getBannedUsers().remove(targetUuid);
                        MenuHelper.playSuccess(player);
                        player.sendMessage(MenuHelper.text("<#22C55E>✦ Jogador desbanido da claim com sucesso!</#22C55E>"));
                        ClaimBanMenu.open(plugin, player, claim, claimWorld);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
    }
}