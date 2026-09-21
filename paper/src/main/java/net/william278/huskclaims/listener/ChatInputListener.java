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

package net.william278.huskclaims.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.guis.ClaimBanMenu;
import net.william278.huskclaims.guis.ClaimMainMenu;
import net.william278.huskclaims.managers.TrustInputManager;
import net.william278.huskclaims.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class ChatInputListener implements Listener {

    private final Plugin plugin;
    private final TrustInputManager inputManager;
    private BukkitHuskClaimsAPI api;

    public ChatInputListener(Plugin plugin, TrustInputManager inputManager) {
        this.plugin = plugin;
        this.inputManager = inputManager;
    }

    private BukkitHuskClaimsAPI getApi() {
        if (api == null) {
            api = BukkitHuskClaimsAPI.getInstance();
        }
        return api;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (!inputManager.isPending(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        String targetName = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        TrustInputManager.PendingAction pending = inputManager.getPending(player.getUniqueId());
        inputManager.removePending(player.getUniqueId());

        if (targetName.equalsIgnoreCase("cancelar")) {
            player.sendMessage("§cAção cancelada.");
            return;
        }

        player.getScheduler().run(plugin, task -> {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);
            if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
                player.sendMessage("§cJogador '" + targetName + "' nunca entrou no servidor!");
                return;
            }

            User targetUser = User.of(targetPlayer.getUniqueId(), targetPlayer.getName() != null ? targetPlayer.getName() : targetName);
            User arbiterUser = User.of(player.getUniqueId(), player.getName());

            if (pending.type() == TrustInputManager.ActionType.ADD_TRUST) {
                try {
                    pending.claim().setUserTrustLevel(targetUser.getUuid(), pending.trustLevel());
                    pending.claimWorld().cacheUser(targetUser);
                    player.sendMessage("§aPermissão de §e" + pending.trustLevel().getDisplayName() + " §aconcedida para §e" + targetUser.getName() + "§a!");
                    ClaimMainMenu.open(plugin, player, pending.claim(), pending.claimWorld());
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cNão é possível adicionar um jogador que está banido do terreno!");
                }
            } else if (pending.type() == TrustInputManager.ActionType.BAN_USER) {
                try {
                    pending.claim().banUser(targetUser, arbiterUser);
                    player.sendMessage("§cO jogador §e" + targetUser.getName() + " §cfoi banido da sua claim!");
                    ClaimBanMenu.open(plugin, player, pending.claim(), pending.claimWorld());
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cVocê não pode banir a si mesmo ou o dono do terreno!");
                }
            }
        }, null);
    }
}