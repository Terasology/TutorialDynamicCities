/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.tutorialDynamicCities.settlement;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.dialogs.components.DialogComponent;
import org.terasology.dialogs.components.DialogPage;
import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.construction.events.BuildingEntitySpawnedEvent;
import org.terasology.dynamicCities.playerTracking.PlayerTracker;
import org.terasology.dynamicCities.settlements.components.MarketComponent;
import org.terasology.economy.components.MarketSubscriberComponent;
import org.terasology.economy.components.MultiInvStorageComponent;
import org.terasology.economy.events.ResourceInfoRequestEvent;
import org.terasology.economy.events.ResourceStoreEvent;
import org.terasology.economy.events.SubscriberRegistrationEvent;
import org.terasology.economy.systems.MarketLogisticSystem;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.registry.In;
import org.terasology.tutorialDynamicCities.market.events.MarketScreenRequestEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RegisterSystem
public class MarketManagementSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    @In
    private MarketLogisticSystem marketLogisticSystem;

    @In
    private AssetManager assetManager;

    @In
    private PlayerTracker playerTracker;

    private Logger logger = LoggerFactory.getLogger(MarketManagementSystem.class);

    private int counter = 0;

    @ReceiveEvent(components = MarketSubscriberComponent.class)
    public void onBuildingEntitySpawned(BuildingEntitySpawnedEvent event, EntityRef entityRef) {
        SettlementRefComponent settlementRefComponent = entityRef.getComponent(SettlementRefComponent.class);
        MarketComponent marketComponent = settlementRefComponent.settlement.getComponent(MarketComponent.class);
        entityRef.setAlwaysRelevant(true);
        MarketSubscriberComponent marketSubscriberComponent = entityRef.getComponent(MarketSubscriberComponent.class);
        marketSubscriberComponent.productStorage = marketComponent.market;
        marketSubscriberComponent.consumptionStorage = marketComponent.market;
        entityRef.saveComponent(marketSubscriberComponent);

        entityRef.send(new SubscriberRegistrationEvent());
    }

    @ReceiveEvent
    public void onMarketScreenAction(MarketScreenRequestEvent event, EntityRef entityRef) {
        EntityRef market = entityManager.getEntity(event.market);
        DialogComponent dialogComponent = entityRef.getComponent(DialogComponent.class);
        ResourceInfoRequestEvent resourceInfoRequestEvent = new ResourceInfoRequestEvent();
        Map<String, Integer> resources;
        market.send(resourceInfoRequestEvent);

        if (resourceInfoRequestEvent.isHandled) {
            resources = resourceInfoRequestEvent.resources;
        } else {
            logger.error("Could not retrieve resource information.");
            return;
        }
        List<String> paragraphText = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : resources.entrySet()) {
            Prefab itemPrefab = entityManager.getPrefabManager().getPrefab(entry.getKey());
            if (itemPrefab != null && itemPrefab.hasComponent(DisplayNameComponent.class)) {
                DisplayNameComponent displayNameComponent = itemPrefab.getComponent(DisplayNameComponent.class);
                paragraphText.add(displayNameComponent.name + ": " + entry.getValue() + "x");
            } else {
                paragraphText.add(entry.getKey() + ": " + entry.getValue() + "x");
            }
        }
        DialogPage dialogPage = dialogComponent.getPage("WARES");
        dialogPage.paragraphText = paragraphText;
    }

    @Override
    public void update(float delta) {
        if (counter != 0) {
            counter --;
            return;
        }
        Iterable<EntityRef> bldgsWithChests = entityManager.getEntitiesWith(MultiInvStorageComponent.class, SettlementRefComponent.class);
        for (EntityRef bldg : bldgsWithChests) {
            if (!bldg.isActive() || !bldg.exists()) {
                continue;
            }
            ResourceInfoRequestEvent requestEvent = new ResourceInfoRequestEvent();
            bldg.send(requestEvent);
            SettlementRefComponent settlementRefComponent = bldg.getComponent(SettlementRefComponent.class);
            if (requestEvent.isHandled && !requestEvent.resources.isEmpty()) {
                for (String resource : requestEvent.resources.keySet()) {
                    if (requestEvent.resources.get(resource) != 0) {
                        bldg.send(new ResourceStoreEvent(resource, requestEvent.resources.get(resource), settlementRefComponent.settlement.getComponent(MarketComponent.class).market));
                    }
                }
            }
        }
        counter = 200;
    }
}
