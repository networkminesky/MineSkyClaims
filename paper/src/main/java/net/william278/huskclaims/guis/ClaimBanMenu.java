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
            Inventory inv = Bukkit.createInventory(null, 45, Component.text("Moderação e Banimentos", NamedTextColor.DARK_GRAY));

            inv.setItem(4, ClaimMainMenu.createItem(Material.ANVIL, "§c§lBanir Novo Jogador",
                    List.of("§7Impede um jogador de entrar", "§7ou pisar dentro da claim.", "", "§eClique para digitar o nick no chat")));

            NamespacedKey key = new NamespacedKey(plugin, BANNED_TAG);

            int slot = 9;
            for (UUID bannedUuid : claim.getBannedUsers().keySet()) {
                if (slot >= 36) break;

                OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(bannedUuid);
                String name = claimWorld.getUser(bannedUuid)
                        .map(User::getName)
                        .orElse(offlineTarget.getName() != null ? offlineTarget.getName() : "Desconhecido");

                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();

                meta.setOwningPlayer(offlineTarget);
                meta.displayName(Component.text("§c" + name));

                meta.lore(List.of(
                        Component.text("§7Este usuário está banido da claim."),
                        Component.empty(),
                        Component.text("§eClique para desbanir")
                ));

                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, bannedUuid.toString());

                head.setItemMeta(meta);
                inv.setItem(slot++, head);
            }

            inv.setItem(40, ClaimMainMenu.createItem(Material.BARRIER, "§cVoltar", List.of("§7Retornar ao menu principal.")));
            player.openInventory(inv);
        }, null);
    }
}