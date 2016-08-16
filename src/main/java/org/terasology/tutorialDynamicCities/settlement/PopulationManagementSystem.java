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


import org.terasology.dynamicCities.population.CultureComponent;
import org.terasology.dynamicCities.population.PopulationComponent;
import org.terasology.dynamicCities.settlements.components.ActiveSettlementComponent;
import org.terasology.dynamicCities.settlements.components.MarketComponent;
import org.terasology.dynamicCities.settlements.events.SettlementRegisterEvent;
import org.terasology.economy.components.InfiniteStorageComponent;
import org.terasology.economy.components.MarketSubscriberComponent;
import org.terasology.economy.events.SubscriberRegistrationEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;

@RegisterSystem
public class PopulationManagementSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @ReceiveEvent(components = {ActiveSettlementComponent.class, PopulationComponent.class, CultureComponent.class})
    public void onSettlementSpawnEvent(SettlementRegisterEvent event, EntityRef settlement) {
        PopulationComponent populationComponent = settlement.getComponent(PopulationComponent.class);
        CultureComponent cultureComponent = settlement.getComponent(CultureComponent.class);
        EntityRef market = entityManager.create(new InfiniteStorageComponent(1));
        MarketSubscriberComponent marketSubscriberComponent = new MarketSubscriberComponent(1);
        marketSubscriberComponent.consumptionStorage = market;
        marketSubscriberComponent.productStorage = settlement;
        marketSubscriberComponent.productionInterval = 500;
        marketSubscriberComponent.production.put(populationComponent.popResourceType, Math.round(cultureComponent.growthRate));
        MarketComponent marketComponent = new MarketComponent(market);
        settlement.addComponent(marketComponent);
        settlement.addComponent(marketSubscriberComponent);

        settlement.saveComponent(marketSubscriberComponent);
        settlement.saveComponent(marketComponent);

        settlement.send(new SubscriberRegistrationEvent());
    }
}
