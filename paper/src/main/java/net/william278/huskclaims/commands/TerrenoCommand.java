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

package net.william278.huskclaims.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.claim.ClaimWorld;
import net.william278.huskclaims.guis.ClaimMainMenu;
import net.william278.huskclaims.position.Position;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TerrenoCommand implements BasicCommand, CommandExecutor {

    private final Plugin plugin;
    private BukkitHuskClaimsAPI api;

    public TerrenoCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    private BukkitHuskClaimsAPI getApi() {
        if (api == null) {
            api = BukkitHuskClaimsAPI.getInstance();
        }
        return api;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!(stack.getSender() instanceof Player player)) {
            stack.getSender().sendMessage(Component.text("Apenas jogadores podem executar este comando.", NamedTextColor.RED));
            return;
        }
        handleCommand(player);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Apenas jogadores podem executar este comando.", NamedTextColor.RED));
            return true;
        }
        handleCommand(player);
        return true;
    }

    private void handleCommand(Player player) {
        BukkitHuskClaimsAPI api = getApi();
        Optional<ClaimWorld> claimWorldOpt = api.getClaimWorld(api.getWorld(player.getWorld()));
        if (claimWorldOpt.isEmpty()) {
            player.sendMessage(Component.text("O sistema de proteção de terrenos não está ativo neste mundo.", NamedTextColor.RED));
            return;
        }

        ClaimWorld claimWorld = claimWorldOpt.get();
        Position position = api.getPosition(player.getLocation());

        Optional<Claim> claimOpt = claimWorld.getClaimAt(position);
        if (claimOpt.isEmpty()) {
            player.sendMessage(Component.text("Você precisa estar pisando em um terreno para gerenciá-lo!", NamedTextColor.RED));
            return;
        }

        Claim claim = claimOpt.get();

        boolean isOwner = claim.getOwner()
                .map(uuid -> uuid.equals(player.getUniqueId()))
                .orElse(false);

        boolean isAdmin = player.hasPermission("huskclaims.admin_claim")
                || player.hasPermission("huskclaims.claim.manage.other");

        String trustLevel = claim.getTrustedUsers().get(player.getUniqueId());
        boolean isManager = trustLevel != null && trustLevel.equalsIgnoreCase("manage");

        if (!isManager && claim.isInheritParent() && claim.getParent().isPresent()) {
            String parentTrust = claim.getParent().get().getTrustedUsers().get(player.getUniqueId());
            isManager = parentTrust != null && parentTrust.equalsIgnoreCase("manage");
        }

        if (!isOwner && !isAdmin && !isManager) {
            player.sendMessage(Component.text("Você não tem permissão para gerenciar este terreno!", NamedTextColor.RED));
            return;
        }

        ClaimMainMenu.open(plugin, player, claim, claimWorld);
    }
}