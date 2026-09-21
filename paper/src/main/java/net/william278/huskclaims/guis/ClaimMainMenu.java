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
import net.kyori.adventure.text.format.TextDecoration;
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.claim.ClaimWorld;
import net.william278.huskclaims.claim.Region;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimMainMenu {

    public static void open(Plugin plugin, Player player, Claim claim, ClaimWorld claimWorld) {
        player.getScheduler().run(plugin, task -> {
            BukkitHuskClaimsAPI api = BukkitHuskClaimsAPI.getInstance();
            Inventory inv = Bukkit.createInventory(null, 45, Component.text("Gerenciador de Terreno", NamedTextColor.DARK_GRAY));

            Region region = claim.getRegion();
            int area = region.getSurfaceArea();
            int width = region.getLongestEdge();
            int length = region.getShortestEdge();
            Region.Point near = region.getNearCorner();
            Region.Point far = region.getFarCorner();

            api.getClaimBlocks(player.getUniqueId()).thenAccept(blocks -> {
                player.getScheduler().run(plugin, subTask -> {
                    ItemStack infoItem = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta skull = (SkullMeta) infoItem.getItemMeta();
                    OfflinePlayer p = Bukkit.getOfflinePlayer(claim.getOwner().get());
                    skull.setOwningPlayer(p);
                    skull.displayName(Component.text("Informações do Terreno", NamedTextColor.GOLD, TextDecoration.BOLD));

                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("§7Dono: §e" + (claim.isAdminClaim() ? "Administração" : p.getName())));
                    lore.add(Component.text("§7Tipo: §f" + (claim.isChildClaim() ? "Sub-divisão" : (claim.isAdminClaim() ? "Admin" : "Comum"))));
                    lore.add(Component.text("§7Tamanho: §b" + width + " §7x §b" + length + " §7(" + area + " blocos²)"));
                    lore.add(Component.text("§7Cantos: §8[§7" + near.getBlockX() + ", " + near.getBlockZ() + "§8] §7até §8[§7" + far.getBlockX() + ", " + far.getBlockZ() + "§8]"));
                    lore.add(Component.empty());
                    lore.add(Component.text("§7Seus blocos de claim: §a" + blocks));
                    skull.lore(lore);
                    infoItem.setItemMeta(skull);
                    inv.setItem(4, infoItem);
                }, null);
            });

            inv.setItem(20, createItem(Material.WRITABLE_BOOK, "§a§lMembros Confiados",
                    List.of("§7Veja quem tem acesso à sua claim,",
                            "§7adicione novos membros ou revogue permissões.", "",
                            "§eClique para ver o trustlist")));

            inv.setItem(22, createItem(Material.REDSTONE_TORCH, "§6§lPropriedades (Flags)",
                    List.of("§7Ative ou desative PvP, fogo,", "§7explosões, monstros e interações.", "", "§eClique para configurar")));

            inv.setItem(24, createItem(Material.IRON_SWORD, "§c§lBanimentos do Terreno",
                    List.of("§7Gerencie a lista de banidos", "§7ou bana jogadores do terreno.", "", "§eClique para moderar")));

            boolean isPrivate = claim.isPrivateClaim();
            inv.setItem(30, createItem(isPrivate ? Material.IRON_DOOR : Material.OAK_DOOR,
                    "§d§lStatus: " + (isPrivate ? "§c§lPRIVADO" : "§a§lPÚBLICO"),
                    List.of("§7Se o terreno estiver privado,", "§7visitantes não podem entrar.", "",
                            "§7Estado atual: " + (isPrivate ? "§cBloqueado para visitantes" : "§aAberto ao público"),
                            "", "§eClique para alternar")));

            inv.setItem(32, createItem(Material.GLOWSTONE_DUST, "§e§lDestacar Bordas",
                    List.of("§7Mostra partículas nos limites", "§7deste terreno temporariamente.", "", "§eClique para visualizar")));

            inv.setItem(40, createItem(Material.BARRIER, "§4§lAbandonar Terreno",
                    List.of("§7Deleta este terreno e devolve", "§7seus blocos de proteção.", "", "§c§lCUIDADO: §cAção irreversível!")));

            player.openInventory(inv);
        }, null);
    }

    public static ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(lore.stream().map(Component::text).toList());
        item.setItemMeta(meta);
        return item;
    }
}