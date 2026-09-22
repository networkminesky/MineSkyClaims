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
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ClaimMainMenu {

    public static void open(Plugin plugin, Player player, Claim claim, ClaimWorld claimWorld) {
        player.getScheduler().run(plugin, task -> {
            BukkitHuskClaimsAPI api = BukkitHuskClaimsAPI.getInstance();
            Inventory inv = Bukkit.createInventory(null, 45, MenuHelper.text("<gradient:#4facfe:#00f2fe>Gerenciador de Terrenos</gradient>"));

            Region region = claim.getRegion();
            int area = region.getSurfaceArea();
            Region.Point center = region.getCenter();

            api.getClaimBlocks(player.getUniqueId()).thenAccept(blocks -> {
                player.getScheduler().run(plugin, subTask -> {
                    ItemStack infoItem = new ItemStack(Material.GOLDEN_SHOVEL);
                    var meta = infoItem.getItemMeta();
                    OfflinePlayer p = Bukkit.getOfflinePlayer(claim.getOwner().get());
                    meta.displayName(MenuHelper.text("<gradient:#f7971e:#ffd200>✦ Área Protegida</gradient>"));

                    meta.lore(List.of(
                            MenuHelper.text("<#78716C>Informações do Terreno</#78716C>"),
                            Component.empty(),
                            MenuHelper.text("<#E2E8F0>Mundo: <white>" + player.getWorld().getName() + "</white>"),
                            MenuHelper.text("<#E2E8F0>Centro: <gradient:#4facfe:#00f2fe>X: " + center.getBlockX() + " | Z: " + center.getBlockZ() + "</gradient>"),
                            MenuHelper.text("<#E2E8F0>Área de Bloco: <white>" + area + " blocos²</white>"),
                            MenuHelper.text("<#E2E8F0>Seus Blocos Restantes: <gradient:#43e97b:#38f9d7>" + blocks + " blocos</gradient>"),
                            Component.empty(),
                            MenuHelper.text("<#78716C>Dono: <white>" + (claim.isAdminClaim() ? "Administração" : p.getName()) + "</white>")
                    ));
                    infoItem.setItemMeta(meta);
                    inv.setItem(4, infoItem);
                }, null);
            });

            inv.setItem(20, MenuHelper.createItem(Material.WRITABLE_BOOK,
                    "<gradient:#4facfe:#00f2fe>✦ Membros Confiados</gradient>",
                    List.of(
                            "<#78716C>Gerenciamento de Amigos</#78716C>",
                            "",
                            "<#E2E8F0>Veja e configure quem possui</#E2E8F0>",
                            "<#E2E8F0>acesso para construir ou usar recipientes.</#E2E8F0>",
                            "",
                            "<gradient:#f5af19:#f12711>▶ Clique para ver o trustlist</gradient>"
                    )
            ));

            inv.setItem(22, MenuHelper.createItem(Material.REDSTONE_TORCH,
                    "<gradient:#f7971e:#ffd200>✦ Propriedades (Flags)</gradient>",
                    List.of(
                            "<#78716C>Regras do Terreno</#78716C>",
                            "",
                            "<#E2E8F0>Configure regras de PvP, fogo, explosões,</#E2E8F0>",
                            "<#E2E8F0>nascimento de monstros e portas.</#E2E8F0>",
                            "",
                            "<gradient:#f5af19:#f12711>▶ Clique para configurar</gradient>"
                    )
            ));

            inv.setItem(24, MenuHelper.createItem(Material.IRON_SWORD,
                    "<gradient:#ff416c:#ff4b2b>✦ Banimentos & Moderação</gradient>",
                    List.of(
                            "<#78716C>Segurança da Claim</#78716C>",
                            "",
                            "<#E2E8F0>Impeça invasores de entrar na área</#E2E8F0>",
                            "<#E2E8F0>ou gerencie jogadores banidos.</#E2E8F0>",
                            "",
                            "<gradient:#f5af19:#f12711>▶ Clique para moderar</gradient>"
                    )
            ));

            boolean isPrivate = claim.isPrivateClaim();
            inv.setItem(30, MenuHelper.createItem(isPrivate ? Material.IRON_DOOR : Material.OAK_DOOR,
                    isPrivate ? "<gradient:#ff416c:#ff4b2b>✦ Terreno Privado</gradient>" : "<gradient:#43e97b:#38f9d7>✦ Terreno Público</gradient>",
                    List.of(
                            "<#78716C>Controle de Acesso</#78716C>",
                            "",
                            "<#E2E8F0>Status: " + (isPrivate ? "<gradient:#ff416c:#ff4b2b>Bloqueado para visitantes</gradient>" : "<gradient:#43e97b:#38f9d7>Livre para visitantes</gradient>"),
                            "<#78716C>Quando privado, apenas membros entram.</#78716C>",
                            "",
                            "<gradient:#f5af19:#f12711>▶ Clique para alternar</gradient>"
                    )
            ));

            inv.setItem(32, MenuHelper.createItem(Material.GLOWSTONE_DUST,
                    "<gradient:#f7971e:#ffd200>✦ Visualizar Bordas</gradient>",
                    List.of(
                            "<#78716C>Projeção Visual</#78716C>",
                            "",
                            "<#E2E8F0>Projeta blocos brilhantes no contorno</#E2E8F0>",
                            "<#E2E8F0>e nos cantos do seu terreno.</#E2E8F0>",
                            "",
                            "<gradient:#f5af19:#f12711>▶ Clique para destacar</gradient>"
                    )
            ));

            inv.setItem(40, MenuHelper.createItem(Material.BARRIER,
                    "<gradient:#ff416c:#ff4b2b>✦ Abandonar Terreno</gradient>",
                    List.of(
                            "<#78716C>Exclusão Permanente</#78716C>",
                            "",
                            "<#E2E8F0>Remove toda a proteção e devolve</#E2E8F0>",
                            "<#E2E8F0>seus blocos de claim de volta.</#E2E8F0>",
                            "",
                            "<gradient:#ff416c:#ff4b2b>ⓘ Esta ação não pode ser desfeita!</gradient>",
                            "<gradient:#ff416c:#ff4b2b>▶ Clique para deletar</gradient>"
                    )
            ));

            player.openInventory(inv);
        }, null);
    }
}