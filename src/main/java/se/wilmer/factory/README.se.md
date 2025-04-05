
# Energy
Energi kommer från [EnergySupplier](https://github.com/wilmer100101/Factory/blob/main/src/main/java/se/wilmer/factory/energy/EnergySupplier.java), och används av [EnergyConsumer](https://github.com/wilmer100101/Factory/blob/main/src/main/java/se/wilmer/factory/energy/EnergyConsumer.java). För att en supplier ska kunna överföra energi till en consumer behöver det vara kopplade till samma [EnergyNetwork](https://github.com/wilmer100101/Factory/blob/main/src/main/java/se/wilmer/factory/energy/EnergyNetwork.java).

## Networks
Ett nätverk består av flera [EnergyComponent](https://github.com/wilmer100101/Factory/blob/main/src/main/java/se/wilmer/factory/energy/EnergyComponent.java). Nätverket innehåller även en HashMap, som håller reda på vilka komponenter som är kopplade till vilka. Detta för att kunna gå igenom hela nätverket och dela upp det i mindre nätverk när en component tas bort.

## Distribution

### Defualt
EnergyConsumers och EnergySuppliers sparar aldrig energi. För att simulera att en EnergyConsumer tar emot energi varje tick används metoden setCurrentEnergyLimit tillsammans med getMaxEnergyConsumption. Detta möjliggör att energinivån sätts till ett värde som inte överskrider den maximala energi nivån. Värdet behöver dock inte uppdateras varje tick. Om exempelvis en SolarPanel och en TreeCutter befinner sig i samma nätverk, där SolarPanel genererar 100 energi och TreeCutter har ett maximalt energibehov på 200, behöver SolarPanel suppliedEnergy endast justeras vid förändringar mellan natt och dag. Detta medför att metoden updateNetwork i Distributor endast körs två gånger per 24 000 tick, istället för varje tick.

### Storage
EnergyStorage får energi som blir över, när den vanliga utdelning av energi har skett. Samt fördelar sin energi rättvist över nätverkts komponenter varje tick sålänge den har energi.

# Component


Ett [ComponentEntity](https://github.com/wilmer100101/Factory/blob/main/src/main/java/se/wilmer/factory/component/ComponentEntity.java) motsvarar ett placerat block, varje ComponentEntity har ett unikt uuid, samt en typ som motsvarar den komponent typ den tillhör, exempelvis `treecutter`, som blockets item får så fort det skapas. Genom att hämta blockets component typ, skapar man ComponentEntity objektet genom att hitta dess [Component](https://github.com/wilmer100101/Factory/blob/main/src/main/java/se/wilmer/factory/component/Component.java).

## Components
Varje komponent har sin egna folder under `/component/components`, där all komponents logik är.
## Wire
Alla components som är i ett EnergyNetwork är kopplade ihop via wires. En [wire](https://github.com/wilmer100101/Factory/blob/fcc69097bba088a77b74e0a49449a4308e0c1ce9/src/main/java/se/wilmer/factory/component/wire/Wire.java) består av två entities. Varje EnergyComponent sparar deras wire kopplingar direkt i blocket, eftersom det endast används när blocket är laddat.