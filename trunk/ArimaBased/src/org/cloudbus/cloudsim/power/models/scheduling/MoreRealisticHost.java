/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling;

import java.util.List;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * @author Márcio Costa Junior - University of São Paulo
 */
public class MoreRealisticHost extends PowerHost{

    /*Host's situation(0 = turned on, 1 = standby, 2 = hibernating, 3 = turned off)*/
    private int situation;

    /*Host's inicial temperature*/
    private double inicialTemperatura;

    public static int TURNED_ON = 0;
    public static int STANDBY = 1;
    public static int HIBERNATING = 2;
    public static int TURNED_OFF = 3;

    //Virtual machine allocation policy*/
    private GreenVmAllocationPolicy allocationPolicy;

    /** The pe list. */
    private List<? extends MoreRealisticPe> realisticPeList;

    //MIPS quantity supported by the host
    private int supportedMips;

    //MIPS quantity used by the host
    private int maxUsedMips;

    //Holds each situation time that the host passed through
    private double [] usedTime;

    //Holds each inicial situation time that the host passed through
    private double [] inicialTime;

    //Blocks the host
    private boolean block;

        /**
	 * Instantiates a new more realistic host.
	 *
	 * @param id the id
	 * @param cpuProvisioner the cpu provisioner
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage
	 * @param vmScheduler the VM scheduler
	 */
    public MoreRealisticHost(int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler){
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);

        setInicialTemperatura(20.0);
        setSituation(0);
        setSupportedMips(0);
        setMaxUsedMips(0);

        usedTime = new double[4];
        inicialTime = new double[4];
        for(int i = 0; i < 4; i++){
            usedTime[i] = CloudSim.clock();
            inicialTime[i] = CloudSim.clock();
        }
        setBlock(false);
    }

        /**
	 * Allocates Ram, Memory and Pes to a Vm in the Host.
	 *
	 * @param vm Vm being started
	 *
	 * @return $true if the VM could be started in the host; $false otherwise
	 *
	 * @pre $none
	 * @post $none
	 */
    @Override
    public boolean vmCreate(Vm vm){

        if(getSituation() == TURNED_ON && !isBlock()){
            //Estimate the host's power after the allocation of the vm
             double estimatePower = getAllocationPolicy().estimatePower(this, vm);
             //Calculate the host's temperature
             if(calculateHostTemperature(estimatePower)){
                 
                if (!getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam())) {
                                Log.printLine("Allocation of VM #" + vm.getId() + " to Host #" + getId() + " failed by RAM");
                                return false;
                        }

                        if (!getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
                                Log.printLine("Allocation of VM #" + vm.getId() + " to Host #" + getId() + " failed by BW");
                                getRamProvisioner().deallocateRamForVm(vm);
                                return false;
                        }

                        if (!getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
                                Log.printLine("[VmScheduler.vmCreate] Allocation of VM #" + vm.getId() + " to Host #" + getId() + " failed by MIPS");
                                getRamProvisioner().deallocateRamForVm(vm);
                                getBwProvisioner().deallocateBwForVm(vm);
                                return false;
                        }

                        getVmList().add(vm);
                        vm.setHost(this);
                        return true;
            }
            else{
                  Log.printLine("Allocation of VM #" + vm.getId() + " to Host #" + getId() + "has failed because the temperature is too high");
                  return false;
            }
        }
        //Errors treatment
        else{
            if(getSituation() == STANDBY)
                Log.printLine("Allocation of VM #" + vm.getId() + " to Host #" + getId() + "has failed because the host is in Stand by");
            else
                if(getSituation() == HIBERNATING)
                    Log.printLine("Allocation of VM #" + vm.getId() + " to Host #" + getId() + "has failed because the host is Hibernating");

                else
                    Log.printLine("Allocation of VM #" + vm.getId() + " to Host #" + getId() + "has failed because the host is Turned Off");

           return false;
        }

    }

         /**
	 * Calculates the host temperature.
	 *In this case, we're considering the worst case, host
         *temperature is the same that Pe temperature.
          *
	 *@param energyConsumed the energy consumed by the host
          *
	 * @return $temperature the host temperature
	 *
	 * @pre $none
	 * @post $none
	 */
    protected boolean calculateHostTemperature(double energyConsumed){

        double temperature = 0.0;

        if(getRealisticPeList().get(0).getWeight() > 0.0){
                //Calculates the temperature of one of the processors
                temperature = (energyConsumed - getPower())/(getRealisticPeList().get(0).getWeight()*getRealisticPeList().get(0).SPECIFIC_HEAT) + getInicialTemperatura();
        }
        //Return true if the temperature is less than tCase
        if(temperature <= getRealisticPeList().get(0).gettCase()){
                //Updates the inicial temperature
                setInicialTemperatura(temperature);
                return true;
        }
        
        return false;
    }


    /**

          *Calculates the delay time to change
          *the host's situation
          *
          *
         *@param oldSituation the situation before the change
         *
	 * @return $delay the delay time
	 *
	 * @pre $none
	 * @post $none
	 */
    public double delayToChangeSituation(int oldSituation){
        double delay = 0.0;

        //Checking the current situation and the old situation to calculate the delay time
        if((getSituation() == TURNED_ON) && (oldSituation == HIBERNATING)){
            delay = 30.0;
        }
        else
        if((getSituation() == TURNED_ON) && (oldSituation == STANDBY)){
            delay = 5.0;
        }
        else
        if((getSituation() == TURNED_ON) && (oldSituation == TURNED_OFF)){
            delay = 30.0;
        }
        else
        if((getSituation() == STANDBY) && (oldSituation == TURNED_ON)){
            delay = 5.0;
        }
        else
        if((getSituation() == HIBERNATING) && (oldSituation == TURNED_ON)){
            delay = 30.0;
        }
        else
        if((getSituation() == TURNED_OFF) && (oldSituation == TURNED_ON)){
            delay = 30.0;
        }

        return delay;
    }

    /**

          *Calculates the energy consumed to change
          *the host's situation
          *
          *
         *@param oldSituation the situation before the change
         *
	 * @return $energy the energy consumed
	 *
	 * @pre $none
	 * @post $none
	 */
    public double energyToChangeSituation(int oldSituation){
        double energy = 0.0;

        //Checking the current situation and the old situation to calculate the energy consumed
        if((getSituation() == TURNED_ON) && (oldSituation == HIBERNATING)){
            energy = 60.0;
        }
        else
        if((getSituation() == TURNED_ON) && (oldSituation == STANDBY)){
            energy = 14.3;
        }
        else
        if((getSituation() == TURNED_ON) && (oldSituation == TURNED_OFF)){
            energy = 110.0;
        }
        else
        if((getSituation() == STANDBY) && (oldSituation == TURNED_ON)){
            energy = 14.3;
        }
        else
        if((getSituation() == HIBERNATING) && (oldSituation == TURNED_ON)){
            energy = 60.0;
        }
        else
        if((getSituation() == TURNED_OFF) && (oldSituation == TURNED_ON)){
            energy = 60.0;
        }

        return energy;
    }

    /**
	 * Return the energy consumed by staying in a specific situation
	 *
	 * @param status host's status
	 *
	 * @return $energy energy consumed during usage (in Watts)
	 *
	 * @pre $status != null
	 * @post $none
	 */
    public double getEnergyConsumedDuringUsage(int status){

        double energy = 0.0;

        if(status == TURNED_ON){
            energy = 100;
        }
        else
        if(status == STANDBY){
            energy = 60;
        }

        return energy;
    }

    /* (non-Javadoc)
	 * @see cloudsim.Host#updateVmsProcessing(double)
	 */
    @Override
    public double updateVmsProcessing(double currentTime) {
            double smallerTime = super.updateVmsProcessing(currentTime);

            //Setting the used mips
            setUtilizationMips(0);

            for (Vm vm : getVmList()) {
                    getVmScheduler().deallocatePesForVm(vm);
            }

            for (Vm vm : getVmList()) {
                    getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips());
            }

            for (Vm vm : getVmList()) {
                    double totalRequestedMips = vm.getCurrentRequestedTotalMips();

                    if (totalRequestedMips == 0) {
                            Log.printLine("VM #" + vm.getId() + " has completed its execution and destroyed");
                            continue;
                    }

                    double totalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);

                    if (totalAllocatedMips + 0.1 < totalRequestedMips) {
                            Log.printLine("Under allocated MIPS for VM #" + vm.getId() + ": requested " + totalRequestedMips + ", allocated " + totalAllocatedMips);
                    }

                    updateUnderAllocatedMips(vm, totalRequestedMips, totalAllocatedMips);

                    Log.formatLine("%.2f: Total allocated MIPS for VM #" + vm.getId() + " (Host #" + vm.getHost().getId() + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)", CloudSim.clock(), totalAllocatedMips, totalRequestedMips, vm.getMips(), totalRequestedMips / vm.getMips() * 100);

                    if (vm.isInMigration()) {
                            Log.printLine("VM #" + vm.getId() + " is in migration");
                            totalAllocatedMips /= 0.9; // performance degradation due to migration - 10%
                    }

                    //Updating host's used mips
                    setUtilizationMips(getUtilizationMips() + totalAllocatedMips);
            }

            return smallerTime;
    }


     /**
	 * Updating the time vector which has each situation passed by
	 *
	 * @param time recent time
         * @param status new host situation
	 * @param oldStatus old host situation
         *
	 *
	 * @pre $status != null
         * @pre $time != null
         * @pre $oldStatus != null
	 * @post $none
	 */
    public void calculateUsedTime(double time, int status, int oldStatus){
        double [] usedTime = getUsedTime();
        double [] iniTime = getInicialTime();

        usedTime[oldStatus] += time - iniTime[oldStatus];
        iniTime[oldStatus] = time;
        iniTime[status] = time;

        setUsedTime(usedTime);
        setInicialTime(iniTime);
    }

    /**
	 * Calculate the extra energy consumtion
         *
	 *
	 * @pre $none
	 * @post $none
	 */
    public double calculateExtraEnergyConsumption(){
        double energy = 0.0;

        for(int i = 0; i < 4; i++){
            energy += getEnergyConsumedDuringUsage(i)*getUsedTime()[i];
        }

        return energy;
    }

    
    


    /**
     * @return the situation
     */
    public int getSituation() {
        return situation;
    }

    /**
     * @param situation the situation to set
     */
    public void setSituation(int situation) {
        this.situation = situation;
    }

    /**
     * @return the inicialTemperatura
     */
    public double getInicialTemperatura() {
        return inicialTemperatura;
    }

    /**
     * @param inicialTemperatura the inicialTemperatura to set
     */
    public void setInicialTemperatura(double inicialTemperatura) {
        this.inicialTemperatura = inicialTemperatura;
    }

    /**
     * @return the realisticPeList
     */
    public List<? extends MoreRealisticPe> getRealisticPeList() {
        return realisticPeList;
    }

    /**
     * @param realisticPeList the realisticPeList to set
     */
    public void setRealisticPeList(List<? extends MoreRealisticPe> realisticPeList) {
        this.realisticPeList = realisticPeList;
    }

    /**
     * @return the allocationPolicy
     */
    public GreenVmAllocationPolicy getAllocationPolicy() {
        return allocationPolicy;
    }

    /**
     * @param allocatioPolicy the allocatioPolicy to set
     */
    public void setAllocationPolicy(GreenVmAllocationPolicy allocatioPolicy) {
        this.allocationPolicy = allocatioPolicy;
    }

    /**
     * @return the supportedMips
     */
    public int getSupportedMips() {
        return supportedMips;
    }

    /**
     * @param supportedMips the supportedMips to set
     */
    public void setSupportedMips(int supportedMips) {
        this.supportedMips = supportedMips;
    }

    /**
     * @return the maxUsedMips
     */
    public int getMaxUsedMips() {
        return maxUsedMips;
    }

    /**
     * @param maxUsedMips the maxUsedMips to set
     */
    public void setMaxUsedMips(int maxUsedMips) {
        this.maxUsedMips = maxUsedMips;
    }


    /**
     * @return the inicialTime
     */
    public double [] getInicialTime() {
        return inicialTime;
    }

    /**
     * @param inicialTime the inicialTime to set
     */
    public void setInicialTime(double [] inicialTime) {
        this.inicialTime = inicialTime;
    }

    /**
     * @return the usedTime
     */
    public double[] getUsedTime() {
        return usedTime;
    }

    /**
     * @param usedTime the usedTime to set
     */
    public void setUsedTime(double[] usedTime) {
        this.usedTime = usedTime;
    }

    /**
     * @return the block
     */
    public boolean isBlock() {
        return block;
    }

    /**
     * @param block the block to set
     */
    public void setBlock(boolean block) {
        this.block = block;
    }

}
