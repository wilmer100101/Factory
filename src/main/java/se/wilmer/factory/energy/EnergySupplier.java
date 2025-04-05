package se.wilmer.factory.energy;

public interface EnergySupplier extends EnergyComponent {
    long getSuppliedEnergy();

    void setSuppliedEnergy(long energy);
}