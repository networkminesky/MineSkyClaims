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
import java.util.UUID;

public class ClaimBanMenu {

    public static final String BANNED_TAG = "banned_player_uuid";

    public static void open(Plugin plugin, Player player, Claim claim, ClaimWorld claimWorld) {
        player.getScheduler().run(plugin, task -> {
            Inventory inv = Bukkit.createInventory(null, 45, MenuHelper.text("<#78716C>Banimentos da Claim"));

            inv.setItem(4, MenuHelper.createItem(Material.ANVIL,
                    "<#EF4444>+ Banir Jogador</#EF4444>",
                    List.of(
                            "<#78716C>Bloquear Entrada na Claim</#78716C>",
                            "",
                            "<#E2E8F0>Impede um jogador específico</#E2E8F0>",
                            "<#E2E8F0>de cruzar os limites do terreno.</#E2E8F0>",
                            "",
                            "<#F87171>▶ Clique para banir pelo chat</#F87171>"
                    )
            ));

            NamespacedKey key = new NamespacedKey(plugin, BANNED_TAG);

            int slot = 9;
            for (UUID bannedUuid : claim.getBannedUsers().keySet()) {
                if (slot >= 36) break;

                OfflinePlayer target = Bukkit.getOfflinePlayer(bannedUuid);
                String name = claimWorld.getUser(bannedUuid)
                        .map(User::getName)
                        .orElse(target.getName() != null ? target.getName() : "Desconhecido");

                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(target);

                meta.displayName(MenuHelper.text("<#EF4444>✦ " + name + "</#EF4444>"));
                meta.lore(List.of(
                        MenuHelper.text("<#78716C>Jogador Banido</#78716C>"),
                        Component.empty(),
                        MenuHelper.text("<#E2E8F0>Status: <#EF4444>Entrada Proibida</#EF4444>"),
                        Component.empty(),
                        MenuHelper.text("<#FACC15>▶ Clique para desbanir</#FACC15>")
                ));

                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, bannedUuid.toString());
                head.setItemMeta(meta);

                inv.setItem(slot++, head);
            }

            inv.setItem(40, MenuHelper.createItem(Material.ARROW,
                    "<#EF4444>✦ Voltar</#EF4444>",
                    List.of(
                            "<#78716C>Menu Principal</#78716C>",
                            "",
                            "<#FACC15>▶ Clique para retornar</#FACC15>"
                    )
            ));

            player.openInventory(inv);
        }, null);
    }
}