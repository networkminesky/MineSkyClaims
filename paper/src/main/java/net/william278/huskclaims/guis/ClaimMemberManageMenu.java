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

public class ClaimMemberManageMenu {

    public static final String TARGET_UUID_KEY = "manage_target_uuid";

    public static void open(Plugin plugin, Player player, Claim claim, ClaimWorld claimWorld, UUID targetUuid) {
        player.getScheduler().run(plugin, task -> {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetUuid);
            String targetName = claimWorld.getUser(targetUuid)
                    .map(User::getName)
                    .orElse(targetPlayer.getName() != null ? targetPlayer.getName() : "Membro");

            Inventory inv = Bukkit.createInventory(null, 27, MenuHelper.text("<#78716C>Gerenciar » " + targetName));

            String currentLevel = claim.getTrustedUsers().getOrDefault(targetUuid, "access");

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            headMeta.setOwningPlayer(targetPlayer);
            headMeta.displayName(MenuHelper.text("<#2AD1E8>✦ " + targetName + "</#2AD1E8>"));
            headMeta.lore(List.of(
                    MenuHelper.text("<#78716C>Configurações de Permissão</#78716C>"),
                    Component.empty(),
                    MenuHelper.text("<#E2E8F0>Nível Atual: " + ClaimTrustMenu.formatTrustLevel(currentLevel))
            ));

            NamespacedKey key = new NamespacedKey(plugin, TARGET_UUID_KEY);
            headMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, targetUuid.toString());
            head.setItemMeta(headMeta);
            inv.setItem(4, head);

            inv.setItem(10, createLevelItem(Material.OAK_DOOR, "Acesso", "access", currentLevel, "Permite abrir portas, botões e alavancas."));
            inv.setItem(11, createLevelItem(Material.CHEST, "Recipientes", "container", currentLevel, "Permite abrir baús, barris e fornalhas."));
            inv.setItem(12, createLevelItem(Material.CRAFTING_TABLE, "Construção", "build", currentLevel, "Permite construir e quebrar blocos."));
            inv.setItem(13, createLevelItem(Material.NETHER_STAR, "Gerenciar", "manage", currentLevel, "Permite gerenciar outros membros."));

            inv.setItem(15, MenuHelper.createItem(Material.BARRIER,
                    "<#EF4444>✦ Revogar Permissão</#EF4444>",
                    List.of(
                            "<#78716C>Remover Membro</#78716C>",
                            "",
                            "<#E2E8F0>Remove completamente o jogador</#E2E8F0>",
                            "<#E2E8F0>da lista de membros do terreno.</#E2E8F0>",
                            "",
                            "<#F87171>▶ Clique para expulsar da claim</#F87171>"
                    )
            ));

            inv.setItem(18, MenuHelper.createItem(Material.ARROW,
                    "<#EF4444>✦ Voltar</#EF4444>",
                    List.of(
                            "<#78716C>Lista de Membros</#78716C>",
                            "",
                            "<#FACC15>▶ Clique para retornar</#FACC15>"
                    )
            ));

            player.openInventory(inv);
        }, null);
    }

    private static ItemStack createLevelItem(Material mat, String name, String levelId, String currentLevel, String desc) {
        boolean isCurrent = currentLevel.equalsIgnoreCase(levelId);
        return MenuHelper.createItem(mat,
                (isCurrent ? "<#22C55E>✦ " : "<#78716C>✦ ") + name,
                List.of(
                        "<#78716C>Nível de Acesso</#78716C>",
                        "",
                        "<#E2E8F0>" + desc + "</#E2E8F0>",
                        "",
                        isCurrent ? "<#22C55E>✔ Nível Atual Ativo</#22C55E>" : "<#FACC15>▶ Clique para definir este nível</#FACC15>"
                )
        );
    }
}