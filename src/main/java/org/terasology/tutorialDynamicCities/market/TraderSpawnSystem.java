// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.tutorialDynamicCities.market;


import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dialogs.action.CloseDialogAction;
import org.terasology.dialogs.components.DialogComponent;
import org.terasology.dialogs.components.DialogPage;
import org.terasology.dialogs.components.DialogResponse;
import org.terasology.dynamicCities.buildings.GenericBuildingComponent;
import org.terasology.dynamicCities.buildings.components.DynParcelRefComponent;
import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.construction.events.BuildingEntitySpawnedEvent;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.settlements.components.MarketComponent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.tutorialDynamicCities.dialogs.actions.ShowDialogAction;
import org.terasology.tutorialDynamicCities.dialogs.actions.ShowMarketScreenAction;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.BlockArea;

import java.util.ArrayList;
import java.util.Optional;

@RegisterSystem
public class TraderSpawnSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    private Logger logger = LoggerFactory.getLogger(TraderSpawnSystem.class);

    @ReceiveEvent(components = GenericBuildingComponent.class)
    public void onMarketPlaceSpawn(BuildingEntitySpawnedEvent event, EntityRef entityRef) {
        GenericBuildingComponent genericBuildingComponent = entityRef.getComponent(GenericBuildingComponent.class);
        if (genericBuildingComponent.name.equals("marketplace")) {
            DynParcel dynParcel = entityRef.getComponent(DynParcelRefComponent.class).dynParcel;

            Optional<Prefab> blackPawnOptional = Assets.getPrefab("LightAndShadowResources:blackKing");
            if (blackPawnOptional.isPresent()) {
                BlockArea area = dynParcel.shape;
                Vector3f spawnPosition = new Vector3f(area.minX() + area.getSizeX() / 2, dynParcel.getHeight() + 1, area.minY() + area.getSizeY() / 2);
                EntityRef trader = entityManager.create(blackPawnOptional.get(), spawnPosition);
                SettlementRefComponent settlementRefComponent = entityRef.getComponent(SettlementRefComponent.class);
                trader.addComponent(settlementRefComponent);
                MarketComponent marketComponent = settlementRefComponent.settlement.getComponent(MarketComponent.class);


                DialogComponent dialogComponent = new DialogComponent();
                dialogComponent.pages = new ArrayList<>();

                DialogPage dialogPage1 = new DialogPage();
                dialogPage1.paragraphText = new ArrayList<>();
                dialogPage1.responses = new ArrayList<>();

                DialogPage dialogPage2 = new DialogPage();
                dialogPage2.responses = new ArrayList<>();

                DialogResponse dialogResponse1 = new DialogResponse();
                dialogResponse1.action = new ArrayList<>();

                DialogResponse dialogResponse2 = new DialogResponse();
                dialogResponse2.action = new ArrayList<>();



                dialogPage1.id = "MainScreen";
                dialogPage1.paragraphText.add("What would you like to talk about?");
                dialogPage1.title = "Welcome to the market";
                dialogPage1.responses.add(dialogResponse1);

                dialogPage2.title = "Wares";
                dialogPage2.id = "WARES";
                dialogPage2.responses.add(dialogResponse2);

                dialogResponse1.action.add(new ShowMarketScreenAction(marketComponent.market.getId()));
                dialogResponse1.action.add(new ShowDialogAction(dialogPage2.id));
                dialogResponse1.text = "Show me what you got!";

                dialogResponse2.action.add(new CloseDialogAction());
                dialogResponse2.text = "Goodbye";

                dialogComponent.pages.add(dialogPage1);
                dialogComponent.pages.add(dialogPage2);
                dialogComponent.firstPage = dialogPage1.id;
                trader.addComponent(dialogComponent);
            }
        }
    }
}
