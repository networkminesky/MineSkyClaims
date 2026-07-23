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

import net.william278.huskclaims.BukkitHuskClaims;
import net.william278.huskclaims.claim.Region;
import net.william278.huskclaims.event.BukkitCreateClaimEvent;
import net.william278.huskclaims.event.BukkitDeleteClaimEvent;
import net.william278.huskclaims.user.OnlineUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.util.StructureSearchResult;
import org.jetbrains.annotations.NotNull;

public class ClaimListener extends BukkitListener {
    private static final int STRONGHOLD_PROTECTION_RADIUS = 100;

    public ClaimListener(@NotNull BukkitHuskClaims plugin) {
        super(plugin);
    }

    @EventHandler
    public void onClaimCreate(BukkitCreateClaimEvent e) {
        OnlineUser user = e.getOnlineUser();
        Player player = Bukkit.getPlayer(user.getUuid());

        if (player == null) return;

        Region region = e.getRegion();
        World bukkitWorld = Bukkit.getWorld(e.getClaimWorld().getName(plugin));

        if (bukkitWorld == null) return;

        int centerX = region.getCenter().getBlockX();
        int centerZ = region.getCenter().getBlockZ();
        double playerY = player.getLocation().getY();

        Location claimCenter = new Location(bukkitWorld, centerX, playerY, centerZ);

        if (isNearStronghold(claimCenter, STRONGHOLD_PROTECTION_RADIUS)) {
            e.setCancelled(true);
            user.sendTitle(
                    "&c&lAÇÃO BLOQUEADA",
                    "&cVocê não pode criar terrenos perto de uma Stronghold!",
                    10, 40, 10
            );
            return;
        }

        user.sendTitle("", "&a🪓 Seu terreno foi criado!", 10, 40, 10);
    }

    @EventHandler
    public void onClaimDelete(BukkitDeleteClaimEvent e) {
        OnlineUser user = e.getOnlineUser();
        user.sendTitle(
                "",
                "&c🪓 Seu terreno foi deletado!",
                10, 40, 10
        );
    }

    /**
     * Verifica se uma localização está próxima de uma Stronghold no mundo.
     */
    private boolean isNearStronghold(Location location, int radius) {
        World world = location.getWorld();
        if (world == null) return false;

        StructureSearchResult searchResult = world.locateNearestStructure(location, Structure.STRONGHOLD, radius, false);

        if (searchResult == null || searchResult.getLocation() == null) {
            return false;
        }

        Location strongholdLoc = searchResult.getLocation();
        strongholdLoc.setY(location.getY());

        return location.distance(strongholdLoc) <= radius;
    }
}
