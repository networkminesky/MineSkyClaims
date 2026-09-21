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

package net.william278.huskclaims.guis;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.claim.ClaimWorld;
import net.william278.huskclaims.guis.ClaimMainMenu;
import net.william278.huskclaims.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClaimTrustMenu {

    public static final String TRUST_UUID_KEY = "trust_player_uuid";

    public static void open(Plugin plugin, Player player, Claim claim, ClaimWorld claimWorld) {
        player.getScheduler().run(plugin, task -> {
            Inventory inv = Bukkit.createInventory(null, 45, Component.text("Membros Confiados (Trustlist)", NamedTextColor.DARK_GRAY));

            inv.setItem(4, ClaimMainMenu.createItem(Material.WRITABLE_BOOK, "§a§lAdicionar Confiança", List.of(
                    "§7Conceda acesso a um novo amigo",
                    "§7escolhendo o nível de permissão.",
                    "",
                    "§eClique para adicionar"
            )));

            NamespacedKey key = new NamespacedKey(plugin, TRUST_UUID_KEY);
            Map<UUID, String> trustedUsers = claim.getTrustedUsers();

            if (trustedUsers.isEmpty()) {
                inv.setItem(22, ClaimMainMenu.createItem(Material.BARRIER, "§cNenhum amigo confiado", List.of(
                        "§7Você ainda não deu permissão",
                        "§7para nenhum jogador neste terreno."
                )));
            } else {
                int slot = 9;
                for (Map.Entry<UUID, String> entry : trustedUsers.entrySet()) {
                    if (slot >= 36) break;

                    UUID targetUuid = entry.getKey();
                    String trustLevelId = entry.getValue();

                    OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetUuid);
                    String name = claimWorld.getUser(targetUuid)
                            .map(User::getName)
                            .orElse(offlineTarget.getName() != null ? offlineTarget.getName() : "Desconhecido");

                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) head.getItemMeta();

                    meta.setOwningPlayer(offlineTarget);
                    meta.displayName(Component.text("§e" + name));

                    String formattedLevel = formatTrustLevel(trustLevelId);

                    meta.lore(List.of(
                            Component.text("§7Nível: " + formattedLevel),
                            Component.empty(),
                            Component.text("§eClique para gerenciar")
                    ));

                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, targetUuid.toString());
                    head.setItemMeta(meta);

                    inv.setItem(slot++, head);
                }
            }

            inv.setItem(40, ClaimMainMenu.createItem(Material.ARROW, "§cVoltar", List.of("§7Retornar ao menu do terreno.")));

            player.openInventory(inv);
        }, null);
    }

    public static String formatTrustLevel(String levelId) {
        return switch (levelId.toLowerCase()) {
            case "access" -> "§aAcesso";
            case "container" -> "§6Containers";
            case "build" -> "§eConstrução";
            case "manage" -> "§cGerenciar";
            default -> "§f" + levelId;
        };
    }
}