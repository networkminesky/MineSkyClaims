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

import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.claim.ClaimWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class TrustSelectionMenu {

    public static void open(Plugin plugin, Player player, Claim claim, ClaimWorld claimWorld) {
        player.getScheduler().run(plugin, task -> {
            Inventory inv = Bukkit.createInventory(null, 27, MenuHelper.text("<gradient:#4facfe:#00f2fe>Adicionar Confiança » Seleção</gradient>"));

            inv.setItem(10, MenuHelper.createItem(Material.OAK_DOOR,
                    "<gradient:#43e97b:#38f9d7>+ Conceder: Acesso</gradient>",
                    List.of(
                            "<#78716C>Nível Básico de Entrada</#78716C>",
                            "",
                            "<#E2E8F0>Permite o jogador circular pela claim e</#E2E8F0>",
                            "<#E2E8F0>utilizar portas, botões e alavancas.</#E2E8F0>",
                            "",
                            "<gradient:#ff416c:#ff4b2b>ⓘ Não libera baús ou construções.</gradient>",
                            "",
                            "<gradient:#f5af19:#f12711>▶ Clique para selecionar este nível</gradient>"
                    )
            ));

            // Nível 2: Containers (Container)
            inv.setItem(12, MenuHelper.createItem(Material.CHEST,
                    "<gradient:#f7971e:#ffd200>+ Conceder: Recipientes</gradient>",
                    List.of(
                            "<#78716C>Nível de Armazenamento</#78716C>",
                            "",
                            "<#E2E8F0>Inclui Acesso e adiciona interação com</#E2E8F0>",
                            "<#E2E8F0>baús, fornalhas, barris e dispensers.</#E2E8F0>",
                            "",
                            "<gradient:#ff416c:#ff4b2b>ⓘ Não libera colocar ou quebrar blocos.</gradient>",
                            "",
                            "<gradient:#f5af19:#f12711>▶ Clique para selecionar este nível</gradient>"
                    )
            ));

            // Nível 3: Construção (Build)
            inv.setItem(14, MenuHelper.createItem(Material.CRAFTING_TABLE,
                    "<gradient:#ffe259:#ffa751>+ Conceder: Construção</gradient>",
                    List.of(
                            "<#78716C>Nível de Construtor</#78716C>",
                            "",
                            "<#E2E8F0>Inclui níveis anteriores e permite</#E2E8F0>",
                            "<#E2E8F0>colocar e destruir blocos livremente.</#E2E8F0>",
                            "",
                            "<gradient:#ff416c:#ff4b2b>ⓘ Não pode gerenciar outros membros.</gradient>",
                            "",
                            "<gradient:#f5af19:#f12711>▶ Clique para selecionar este nível</gradient>"
                    )
            ));

            inv.setItem(16, MenuHelper.createItem(Material.NETHER_STAR,
                    "<gradient:#ff416c:#ff4b2b>+ Conceder: Gerenciar</gradient>",
                    List.of(
                            "<#78716C>Acesso Total / Co-Dono</#78716C>",
                            "",
                            "<#E2E8F0>Inclui TODOS os privilégios anteriores e</#E2E8F0>",
                            "<#E2E8F0>permite gerenciar a confiança de outros.</#E2E8F0>",
                            "",
                            "<gradient:#ff416c:#ff4b2b>ⓘ Use apenas com quem você confia 100%!</gradient>",
                            "",
                            "<gradient:#f5af19:#f12711>▶ Clique para selecionar este nível</gradient>"
                    )
            ));

            inv.setItem(22, MenuHelper.createItem(Material.ARROW,
                    "<gradient:#ff416c:#ff4b2b>✦ Voltar</gradient>",
                    List.of(
                            "<#78716C>Lista de Membros</#78716C>",
                            "",
                            "<gradient:#f5af19:#f12711>▶ Clique para retornar</gradient>"
                    )
            ));

            player.openInventory(inv);
        }, null);
    }
}