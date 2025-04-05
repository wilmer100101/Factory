package se.wilmer.factory.energy;

public interface EnergyConsumer extends EnergyComponent {
    long getMaxEnergyConsumption();

    void setCurrentEnergyLimit(long currentEnergyLimit);

    long getCurrentEnergyLimit();
}