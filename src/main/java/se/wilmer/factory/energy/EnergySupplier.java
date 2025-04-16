package se.wilmer.factory.energy;

public interface EnergySupplier extends EnergyComponent {
    long getSuppliedEnergy();

    long getMaxSuppliedEnergy();

    void setSuppliedEnergy(long energy);
}