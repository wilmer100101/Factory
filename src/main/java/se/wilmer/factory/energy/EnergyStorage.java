package se.wilmer.factory.energy;

public interface EnergyStorage {

    long storeEnergy(long amount);

    long getEnergyStored();
}
