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
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.claim.ClaimWorld;
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
            Inventory inv = Bukkit.createInventory(null, 45, MenuHelper.text("<gradient:#4facfe:#00f2fe>Membros Confiados » Pág. 1</gradient>"));

            inv.setItem(4, MenuHelper.createItem(Material.BEACON,
                    "<gradient:#43e97b:#38f9d7>+ Nova Confiança</gradient>",
                    List.of(
                            "<#78716C>Conceder acesso a um amigo.</#78716C>",
                            "",
                            "<#E2E8F0>ⓘ Também pode usar <gradient:#f7971e:#ffd200>/trust <nome></gradient></#E2E8F0>",
                            "<gradient:#43e97b:#38f9d7>▶ Clique para definir pelo chat</gradient>"
                    )
            ));

            NamespacedKey key = new NamespacedKey(plugin, TRUST_UUID_KEY);
            Map<UUID, String> trustedUsers = claim.getTrustedUsers();

            if (trustedUsers.isEmpty()) {
                inv.setItem(22, MenuHelper.createItem(Material.BARRIER,
                        "<#78716C>✦ Nenhum Membro Adicionado</#78716C>",
                        List.of(
                                "<#78716C>Lista Vazia</#78716C>",
                                "",
                                "<#E2E8F0>Você ainda não concedeu permissão</#E2E8F0>",
                                "<#E2E8F0>a nenhum jogador neste terreno.</#E2E8F0>"
                        )
                ));
            } else {
                int slot = 9;
                for (Map.Entry<UUID, String> entry : trustedUsers.entrySet()) {
                    if (slot >= 36) break;

                    UUID targetUuid = entry.getKey();
                    String levelId = entry.getValue();

                    OfflinePlayer target = Bukkit.getOfflinePlayer(targetUuid);
                    String name = claimWorld.getUser(targetUuid)
                            .map(User::getName)
                            .orElse(target.getName() != null ? target.getName() : "Desconhecido");

                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setOwningPlayer(target);

                    meta.displayName(MenuHelper.text("<gradient:#4facfe:#00f2fe>✦ " + name + "</gradient>"));
                    meta.lore(List.of(
                            MenuHelper.text("<#78716C>Membro da Claim</#78716C>"),
                            Component.empty(),
                            MenuHelper.text("<#E2E8F0>Nível de Acesso: " + formatTrustLevel(levelId)),
                            Component.empty(),
                            MenuHelper.text("<gradient:#f5af19:#f12711>▶ Clique para gerenciar</gradient>")
                    ));

                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, targetUuid.toString());
                    head.setItemMeta(meta);

                    inv.setItem(slot++, head);
                }
            }

            inv.setItem(40, MenuHelper.createItem(Material.ARROW,
                    "<gradient:#ff416c:#ff4b2b>✦ Voltar</gradient>",
                    List.of(
                            "<#78716C>Menu Principal</#78716C>",
                            "",
                            "<gradient:#f5af19:#f12711>▶ Clique para retornar</gradient>"
                    )
            ));

            player.openInventory(inv);
        }, null);
    }

    public static String formatTrustLevel(String levelId) {
        return switch (levelId.toLowerCase()) {
            case "access" -> "<gradient:#43e97b:#38f9d7>Acesso</gradient>";
            case "container" -> "<gradient:#f7971e:#ffd200>Containers</gradient>";
            case "build" -> "<gradient:#ffe259:#ffa751>Construção</gradient>";
            case "manage" -> "<gradient:#ff416c:#ff4b2b>Gerenciar</gradient>";
            default -> "<white>" + levelId + "</white>";
        };
    }
}