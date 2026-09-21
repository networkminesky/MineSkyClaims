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

public class ClaimMemberManageMenu {

    public static final String TARGET_UUID_KEY = "manage_target_uuid";

    public static void open(Plugin plugin, Player player, Claim claim, ClaimWorld claimWorld, UUID targetUuid) {
        player.getScheduler().run(plugin, task -> {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetUuid);
            String targetName = claimWorld.getUser(targetUuid)
                    .map(User::getName)
                    .orElse(targetPlayer.getName() != null ? targetPlayer.getName() : "Membro");

            Inventory inv = Bukkit.createInventory(null, 27, Component.text("Gerenciar: " + targetName, NamedTextColor.DARK_GRAY));

            String currentLevel = claim.getTrustedUsers().getOrDefault(targetUuid, "access");

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            headMeta.setOwningPlayer(targetPlayer);
            headMeta.displayName(Component.text("§e" + targetName));
            headMeta.lore(List.of(
                    Component.text("§7Nível Atual: " + ClaimTrustMenu.formatTrustLevel(currentLevel)),
                    Component.empty(),
                    Component.text("§7Altere o nível clicando nos ícones"),
                    Component.text("§7ou remova o acesso do jogador abaixo.")
            ));

            NamespacedKey key = new NamespacedKey(plugin, TARGET_UUID_KEY);
            headMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, targetUuid.toString());
            head.setItemMeta(headMeta);
            inv.setItem(4, head);

            inv.setItem(10, createLevelItem(Material.OAK_DOOR, "Acesso", "access", currentLevel,
                    "Permite entrar e usar botões/portas."));

            inv.setItem(11, createLevelItem(Material.CHEST, "Containers", "container", currentLevel,
                    "Permite abrir baús e fornalhas."));

            inv.setItem(12, createLevelItem(Material.CRAFTING_TABLE, "Construção", "build", currentLevel,
                    "Permite quebrar e colocar blocos."));

            inv.setItem(13, createLevelItem(Material.NETHER_STAR, "Gerenciar", "manage", currentLevel,
                    "Permite gerenciar outros membros."));

            inv.setItem(15, ClaimMainMenu.createItem(Material.BARRIER, "§c§lRemover do Terreno", List.of(
                    "§7Revoga totalmente qualquer acesso",
                    "§7deste jogador à sua claim.",
                    "",
                    "§cClique para remover o jogador"
            )));

            inv.setItem(18, ClaimMainMenu.createItem(Material.ARROW, "§cVoltar", List.of("§7Retornar à lista de membros.")));

            player.openInventory(inv);
        }, null);
    }

    private static ItemStack createLevelItem(Material mat, String name, String levelId, String currentLevel, String desc) {
        boolean isCurrent = currentLevel.equalsIgnoreCase(levelId);
        return ClaimMainMenu.createItem(mat,
                (isCurrent ? "§a✔ " : "§7") + name,
                List.of(
                        "§7" + desc,
                        "",
                        isCurrent ? "§a§lNÍVEL ATUAL DO JOGADOR" : "§eClique para definir este nível"
                )
        );
    }
}