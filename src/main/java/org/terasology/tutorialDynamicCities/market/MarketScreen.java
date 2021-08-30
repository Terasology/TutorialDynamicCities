// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.tutorialDynamicCities.market;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.module.inventory.ui.InventoryGrid;
import org.terasology.nui.databinding.ReadOnlyBinding;

/**
 */
public class MarketScreen extends CoreScreenLayer {

    @In
    private LocalPlayer localPlayer;


    @Override
    public void initialise() {
        InventoryGrid inventory = find("inventory", InventoryGrid.class);
        inventory.bindTargetEntity(new ReadOnlyBinding<EntityRef>() {
            @Override
            public EntityRef get() {
                return localPlayer.getCharacterEntity();
            }
        });
        inventory.setCellOffset(10);
    }

    @Override
    public boolean isModal() {
        return false;
    }

}
