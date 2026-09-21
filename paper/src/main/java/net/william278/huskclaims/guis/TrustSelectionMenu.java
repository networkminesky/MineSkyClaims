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
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.claim.ClaimWorld;
import net.william278.huskclaims.trust.TrustLevel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class TrustSelectionMenu {

    public static void open(Plugin plugin, Player player, Claim claim, ClaimWorld claimWorld) {
        player.getScheduler().run(plugin, task -> {
            Inventory inv = Bukkit.createInventory(null, 27, Component.text("Adicionar Confiança", NamedTextColor.DARK_GRAY));

            inv.setItem(10, createGuiItem(Material.OAK_DOOR, "§a§lConceder: Acesso", List.of(
                    "§7Permite o jogador entrar e circular,",
                    "§7além de usar portas, botões e alavancas.",
                    "",
                    "§cNão libera baús ou construções.",
                    "§eClique para selecionar este nível"
            )));

            inv.setItem(12, createGuiItem(Material.CHEST, "§6§lConceder: Containers", List.of(
                    "§7Inclui Acesso e adiciona interação com",
                    "§7baús, fornalhas, barris e recipientes.",
                    "",
                    "§cNão libera construção/destruição.",
                    "§eClique para selecionar este nível"
            )));

            inv.setItem(14, createGuiItem(Material.CRAFTING_TABLE, "§e§lConceder: Construção", List.of(
                    "§7Inclui níveis anteriores e permite",
                    "§7colocar e quebrar qualquer bloco.",
                    "",
                    "§cNão concede privilégio de gerenciamento.",
                    "§eClique para selecionar este nível"
            )));

            inv.setItem(16, createGuiItem(Material.NETHER_STAR, "§c§lConceder: Gerenciar", List.of(
                    "§7Inclui TODOS os níveis anteriores e",
                    "§7permite gerenciar a confiança de outros.",
                    "",
                    "§c§lUse somente para pessoas de total confiança!",
                    "§eClique para selecionar este nível"
            )));

            inv.setItem(22, ClaimMainMenu.createItem(Material.BARRIER, "§cVoltar", List.of("§7Retornar à lista de membros.")));

            player.openInventory(inv);
        }, null);
    }

    private static ItemStack createGuiItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(lore.stream().map(Component::text).toList());
        item.setItemMeta(meta);
        return item;
    }
}