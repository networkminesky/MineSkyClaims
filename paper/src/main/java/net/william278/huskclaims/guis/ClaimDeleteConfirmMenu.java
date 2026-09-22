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

public class ClaimDeleteConfirmMenu {

    public static void open(Plugin plugin, Player player, Claim claim, ClaimWorld claimWorld) {
        player.getScheduler().run(plugin, task -> {
            Inventory inv = Bukkit.createInventory(null, 27, MenuHelper.text("<gradient:#ff416c:#ff4b2b>Confirmar Exclusão » Terreno</gradient>"));

            inv.setItem(4, MenuHelper.createItem(Material.BARRIER,
                    "<gradient:#ff416c:#ff4b2b>✦ Atenção: Ação Irreversível!</gradient>",
                    List.of(
                            "<#78716C>Exclusão Permanente da Claim</#78716C>",
                            "",
                            "<#E2E8F0>Você está prestes a excluir este terreno.</#E2E8F0>",
                            "<#E2E8F0>Todas as proteções serão removidas e qualquer</#E2E8F0>",
                            "<#E2E8F0>jogador poderá quebrar ou construir na área.</#E2E8F0>",
                            "",
                            "<gradient:#43e97b:#38f9d7>ⓘ Seus blocos de proteção serão devolvidos.</gradient>"
                    )
            ));

            inv.setItem(11, MenuHelper.createItem(Material.LIME_CONCRETE,
                    "<gradient:#43e97b:#38f9d7>+ Confirmar Exclusão</gradient>",
                    List.of(
                            "<#78716C>Abandonar Proteção</#78716C>",
                            "",
                            "<#E2E8F0>Clique para remover a claim</#E2E8F0>",
                            "<#E2E8F0>deste local permanentemente.</#E2E8F0>",
                            "",
                            "<gradient:#ff416c:#ff4b2b>▶ Clique para confirmar</gradient>"
                    )
            ));

            inv.setItem(15, MenuHelper.createItem(Material.RED_CONCRETE,
                    "<gradient:#ff416c:#ff4b2b>✦ Cancelar & Voltar</gradient>",
                    List.of(
                            "<#78716C>Manter Terreno</#78716C>",
                            "",
                            "<#E2E8F0>Clique para cancelar esta ação</#E2E8F0>",
                            "<#E2E8F0>e voltar em segurança ao menu.</#E2E8F0>",
                            "",
                            "<gradient:#f5af19:#f12711>▶ Clique para retornar</gradient>"
                    )
            ));

            player.openInventory(inv);
        }, null);
    }
}