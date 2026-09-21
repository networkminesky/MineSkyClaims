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

package net.william278.huskclaims.managers;

import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.claim.ClaimWorld;
import net.william278.huskclaims.trust.TrustLevel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrustInputManager {

    public enum ActionType {
        ADD_TRUST,
        BAN_USER
    }

    public record PendingAction(ActionType type, Claim claim, ClaimWorld claimWorld, TrustLevel trustLevel) {}

    private final Map<UUID, PendingAction> pendingMap = new ConcurrentHashMap<>();

    public void setWaitingTrust(UUID player, Claim claim, ClaimWorld world, TrustLevel level) {
        pendingMap.put(player, new PendingAction(ActionType.ADD_TRUST, claim, world, level));
    }

    public void setWaitingBan(UUID player, Claim claim, ClaimWorld world) {
        pendingMap.put(player, new PendingAction(ActionType.BAN_USER, claim, world, null));
    }

    public PendingAction getPending(UUID player) {
        return pendingMap.get(player);
    }

    public void removePending(UUID player) {
        pendingMap.remove(player);
    }

    public boolean isPending(UUID player) {
        return pendingMap.containsKey(player);
    }
}