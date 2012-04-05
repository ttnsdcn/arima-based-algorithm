/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling;

/**
 *
 * @author Marcio
 */
public class HostCaracteristic {

    //Potência do processador
    private double pePotency;
    //Porcentagem de consumo de energia utilizada para o processamento
    private double staticPowerPercent;

    //Host caracteristics
    private int mips;
    private int ram;
    private long storage;
    private int bw;

    //Arquitetura, sistema operacional e modelo de máquina virtual
    private String arch;
    private String os;
    private String vmm;

    //Custos para utilização dos recursos
    private double timeZone;
    private double processingCost;
    private double costPerMem;
    private double costPerStorage;
    private double costPerBW;


    public HostCaracteristic(){

        this.setPePotency(0.0);
        this.setStaticPowerPercent(0.0);

        this.setMips(0);
        this.setRam(0);
        this.setBw(0);
        this.setStorage(0);

        this.setArch("No architecture seted");
        this.setVmm("No virtual machine model seted");
        this.setOs("No operating system seted");

        this.setTimeZone(1);
        this.setProcessingCost(1.0);
        this.setCostPerBW(1.0);
        this.setCostPerStorage(1.0);
        this.setCostPerMem(1.0);
    }




    /**
     * @return the pePotency
     */
    public double getPePotency() {
        return pePotency;
    }

    /**
     * @param pePotency the pePotency to set
     */
    public void setPePotency(double pePotency) {
        this.pePotency = pePotency;
    }

    /**
     * @return the staticPowerPercent
     */
    public double getStaticPowerPercent() {
        return staticPowerPercent;
    }

    /**
     * @param staticPowerPercent the staticPowerPercent to set
     */
    public void setStaticPowerPercent(double staticPowerPercent) {
        this.staticPowerPercent = staticPowerPercent;
    }

    /**
     * @return the mips
     */
    public int getMips() {
        return mips;
    }

    /**
     * @param mips the mips to set
     */
    public void setMips(int mips) {
        this.mips = mips;
    }

    /**
     * @return the ram
     */
    public int getRam() {
        return ram;
    }

    /**
     * @param ram the ram to set
     */
    public void setRam(int ram) {
        this.ram = ram;
    }

    /**
     * @return the storage
     */
    public long getStorage() {
        return storage;
    }

    /**
     * @param storage the storage to set
     */
    public void setStorage(long storage) {
        this.storage = storage;
    }

    /**
     * @return the bw
     */
    public int getBw() {
        return bw;
    }

    /**
     * @param bw the bw to set
     */
    public void setBw(int bw) {
        this.bw = bw;
    }

    /**
     * @return the arch
     */
    public String getArch() {
        return arch;
    }

    /**
     * @param arch the arch to set
     */
    public void setArch(String arch) {
        this.arch = arch;
    }

    /**
     * @return the os
     */
    public String getOs() {
        return os;
    }

    /**
     * @param os the os to set
     */
    public void setOs(String os) {
        this.os = os;
    }

    /**
     * @return the vmm
     */
    public String getVmm() {
        return vmm;
    }

    /**
     * @param vmm the vmm to set
     */
    public void setVmm(String vmm) {
        this.vmm = vmm;
    }

    /**
     * @return the timeZone
     */
    public double getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone the timeZone to set
     */
    public void setTimeZone(double timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return the processingCost
     */
    public double getProcessingCost() {
        return processingCost;
    }

    /**
     * @param processingCost the processingCost to set
     */
    public void setProcessingCost(double processingCost) {
        this.processingCost = processingCost;
    }

    /**
     * @return the costPerMem
     */
    public double getCostPerMem() {
        return costPerMem;
    }

    /**
     * @param costPerMem the costPerMem to set
     */
    public void setCostPerMem(double costPerMem) {
        this.costPerMem = costPerMem;
    }

    /**
     * @return the costPerStorage
     */
    public double getCostPerStorage() {
        return costPerStorage;
    }

    /**
     * @param costPerStorage the costPerStorage to set
     */
    public void setCostPerStorage(double costPerStorage) {
        this.costPerStorage = costPerStorage;
    }

    /**
     * @return the costPerBW
     */
    public double getCostPerBW() {
        return costPerBW;
    }

    /**
     * @param costPerBW the costPerBW to set
     */
    public void setCostPerBW(double costPerBW) {
        this.costPerBW = costPerBW;
    }



}
