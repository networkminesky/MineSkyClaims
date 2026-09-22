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
import net.william278.cloplib.operation.OperationType;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.claim.ClaimWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ClaimFlagsMenu {

    public record FlagItem(String id, Material material, String name, String desc) {}

    public static final List<FlagItem> ALL_FLAGS = List.of(
            new FlagItem("player_damage_player", Material.NETHERITE_SWORD, "Combate PvP", "Permite que jogadores lutem entre si na claim."),
            new FlagItem("monster_spawn", Material.ZOMBIE_HEAD, "Spawn de Monstros", "Permite o nascimento natural de mobs hostis."),
            new FlagItem("passive_mob_spawn", Material.SHEEP_SPAWN_EGG, "Spawn de Animais", "Permite o nascimento e procriação de animais."),
            new FlagItem("explosion_damage_terrain", Material.TNT, "Explosões no Terreno", "Permite que TNT e Creepers destruam blocos."),
            new FlagItem("explosion_damage_entity", Material.GUNPOWDER, "Dano de Explosões", "Explosões ferem jogadores e animais."),
            new FlagItem("fire_burn", Material.FLINT_AND_STEEL, "Propagação de Fogo", "Permite o fogo se alastrar e queimar madeiras."),
            new FlagItem("monster_damage_terrain", Material.CREEPER_HEAD, "Griefing de Monstros", "Endermans pegam blocos e Creepers quebram blocos."),
            new FlagItem("ender_pearl_teleport", Material.ENDER_PEARL, "Teleporte com Pérola", "Visitantes podem usar pérolas do End na área."),
            new FlagItem("container_open", Material.CHEST, "Abrir Recipientes", "Permite abrir baús, barris, fornalhas e droppers."),
            new FlagItem("block_interact", Material.OAK_DOOR, "Portas e Portões", "Permite abrir/fechar portas, alçapões e portões."),
            new FlagItem("redstone_interact", Material.LEVER, "Mecanismos de Redstone", "Visitantes podem ativar botões e alavancas."),
            new FlagItem("block_place", Material.BRICKS, "Colocar Blocos", "Permite posicionar blocos comuns no terreno."),
            new FlagItem("block_break", Material.DIAMOND_PICKAXE, "Quebrar Blocos", "Permite minerar e quebrar blocos do terreno."),
            new FlagItem("empty_bucket", Material.LAVA_BUCKET, "Esvaziar Baldes", "Permite despejar líquidos (água, lava) no chão."),
            new FlagItem("fill_bucket", Material.BUCKET, "Encher Baldes", "Permite coletar líquidos com balde vazio."),
            new FlagItem("farm_block_place", Material.WHEAT_SEEDS, "Plantar Sementes", "Permite plantar sementes em terras aradas."),
            new FlagItem("farm_block_break", Material.WHEAT, "Colher Plantações", "Permite colher trigo, cenouras, batatas, etc."),
            new FlagItem("farm_block_interact", Material.FARMLAND, "Pisotear Plantações", "Pulos e saltos destroem terras aradas."),
            new FlagItem("entity_interact", Material.SADDLE, "Interagir com Entidades", "Montar cavalos, tosquiar ovelhas ou ordenhar vacas."),
            new FlagItem("player_damage_entity", Material.IRON_SWORD, "Dano a Animais", "Permite atacar e matar animais da claim."),
            new FlagItem("player_damage_monster", Material.GOLDEN_SWORD, "Atacar Monstros", "Permite que jogadores ataquem mobs hostis."),
            new FlagItem("player_damage_persistent_entity", Material.NAME_TAG, "Dano a Pets/Nomeados", "Permite atacar animais domesticados ou com etiqueta."),
            new FlagItem("start_raid", Material.CROSSBOW, "Iniciar Invasões (Raids)", "Inicia ataques de Pillagers se tiver Mau Presságio."),
            new FlagItem("place_vehicle", Material.OAK_BOAT, "Posicionar Veículos", "Permite colocar barcos e carrinhos de mina."),
            new FlagItem("break_vehicle", Material.MINECART, "Quebrar Veículos", "Permite quebrar e remover barcos e carrinhos."),
            new FlagItem("place_hanging_entity", Material.PAINTING, "Pendurar Decorações", "Permite pendurar quadros e molduras."),
            new FlagItem("break_hanging_entity", Material.ITEM_FRAME, "Remover Decorações", "Permite quebrar quadros e molduras na parede."),
            new FlagItem("use_spawn_egg", Material.EGG, "Usar Ovos de Invocação", "Permite invocar criaturas usando ovos de spawn.")
    );

    private static final int PAGE_SIZE = 21;

    public static void open(Plugin plugin, Player player, Claim claim, ClaimWorld claimWorld, int page) {
        player.getScheduler().run(plugin, task -> {
            int totalPages = (int) Math.ceil((double) ALL_FLAGS.size() / PAGE_SIZE);
            int currentPage = Math.max(0, Math.min(page, totalPages - 1));

            Inventory inv = Bukkit.createInventory(null, 54, MenuHelper.text("<#78716C>Propriedades » Pág. " + (currentPage + 1) + "/" + totalPages));

            int startIndex = currentPage * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, ALL_FLAGS.size());

            int[] slots = {
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34
            };

            for (int i = startIndex; i < endIndex; i++) {
                FlagItem flag = ALL_FLAGS.get(i);
                boolean isEnabled = isFlagActive(claim, flag.id());

                ItemStack item = MenuHelper.createItem(flag.material(),
                        (isEnabled ? "<#22C55E>✦ " : "<#EF4444>✦ ") + flag.name(),
                        List.of(
                                "<#78716C>Regra da Claim</#78716C>",
                                "",
                                "<#E2E8F0>" + flag.desc() + "</#E2E8F0>",
                                "",
                                "<#E2E8F0>Status: " + (isEnabled ? "<#22C55E>✔ Ativado (Livre)</#22C55E>" : "<#EF4444>✖ Desativado (Bloqueado)</#EF4444>"),
                                "",
                                "<#FACC15>▶ Clique para alternar</#FACC15>"
                        )
                );

                inv.setItem(slots[i - startIndex], item);
            }

            if (currentPage > 0) {
                inv.setItem(45, MenuHelper.createItem(Material.ARROW,
                        "<#FFB84D>✦ Página Anterior</#FFB84D>",
                        List.of(
                                "<#78716C>Navegação</#78716C>",
                                "",
                                "<#FACC15>▶ Ir para página " + currentPage + "</#FACC15>"
                        )
                ));
            }

            inv.setItem(49, MenuHelper.createItem(Material.BARRIER,
                    "<#EF4444>✦ Voltar</#EF4444>",
                    List.of(
                            "<#78716C>Menu Principal</#78716C>",
                            "",
                            "<#FACC15>▶ Clique para retornar</#FACC15>"
                    )
            ));

            if (currentPage < totalPages - 1) {
                inv.setItem(53, MenuHelper.createItem(Material.ARROW,
                        "<#FFB84D>✦ Próxima Página</#FFB84D>",
                        List.of(
                                "<#78716C>Navegação</#78716C>",
                                "",
                                "<#FACC15>▶ Ir para página " + (currentPage + 2) + "</#FACC15>"
                        )
                ));
            }

            player.openInventory(inv);
        }, null);
    }

    public static boolean isFlagActive(Claim claim, String flagId) {
        if (claim.getDefaultFlags() == null) return false;
        return claim.getDefaultFlags().stream().anyMatch(op -> matchOp(op, flagId));
    }

    public static void toggleFlag(Claim claim, String flagId) {
        if (isFlagActive(claim, flagId)) {
            claim.getDefaultFlags().removeIf(op -> matchOp(op, flagId));
        } else {
            try {
                claim.getDefaultFlags().add(OperationType.valueOf(flagId.toUpperCase()));
            } catch (Exception e) {
                claim.getDefaultFlags().removeIf(op -> matchOp(op, flagId));
            }
        }
    }

    private static boolean matchOp(OperationType op, String flagId) {
        if (op == null) return false;
        return op.toString().equalsIgnoreCase(flagId) || op.name().equalsIgnoreCase(flagId);
    }
}