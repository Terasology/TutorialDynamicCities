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
package org.terasology.TutorialDynamicCities.settlement;


import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.construction.events.BuildingEntitySpawnedEvent;
import org.terasology.economy.components.MarketSubscriberComponent;
import org.terasology.economy.events.SubscriberRegistrationEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;

@RegisterSystem
public class MarketManagementSystem extends BaseComponentSystem {

    @ReceiveEvent(components = MarketSubscriberComponent.class)
    public void onBuildingEntitySpawned(BuildingEntitySpawnedEvent event, EntityRef entityRef) {
        SettlementRefComponent settlementRefComponent = entityRef.getComponent(SettlementRefComponent.class);
        entityRef.setAlwaysRelevant(true);
        MarketSubscriberComponent marketSubscriberComponent = entityRef.getComponent(MarketSubscriberComponent.class);
        marketSubscriberComponent.productStorage = settlementRefComponent.settlement;
        marketSubscriberComponent.consumptionStorage = settlementRefComponent.settlement;
        entityRef.saveComponent(marketSubscriberComponent);

        entityRef.send(new SubscriberRegistrationEvent());
    }
}
