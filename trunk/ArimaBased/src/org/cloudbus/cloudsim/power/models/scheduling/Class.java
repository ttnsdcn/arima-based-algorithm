/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling;

import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

/**
 *
 * @author Marcio
 */
public class Class {

    private int number;
    private boolean canReceive;
    private double maxMips;
    private FrontEnd frontEnd;
    private LocalDatacenter localDatacenter;
    private Estimator estimator;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;

    public Class(int number, FrontEnd frontEnd, LocalDatacenter localDatacenter, Estimator estimator, double maxMips){
        setNumber(number);
        setEstimator(estimator);
        setFrontEnd(frontEnd);
        setLocalDatacenter(localDatacenter);
        setMaxMips(maxMips);
        setCloudletList(new ArrayList<Cloudlet>());
        setVmList(new ArrayList<Vm>());
    }

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * @return the frontEnd
     */
    public FrontEnd getFrontEnd() {
        return frontEnd;
    }

    /**
     * @param frontEnd the frontEnd to set
     */
    public void setFrontEnd(FrontEnd frontEnd) {
        this.frontEnd = frontEnd;
    }

    /**
     * @return the localDatacenter
     */
    public LocalDatacenter getLocalDatacenter() {
        return localDatacenter;
    }

    /**
     * @param localDatacenter the localDatacenter to set
     */
    public void setLocalDatacenter(LocalDatacenter localDatacenter) {
        this.localDatacenter = localDatacenter;
    }

    /**
     * @return the estimator
     */
    public Estimator getEstimator() {
        return estimator;
    }

    /**
     * @param estimator the estimator to set
     */
    public void setEstimator(Estimator estimator) {
        this.estimator = estimator;
    }

    /**
     * @return the canReceive
     */
    public boolean isCanReceive() {
        return canReceive;
    }

    /**
     * @param canReceive the canReceive to set
     */
    public void setCanReceive(boolean canReceive) {
        this.canReceive = canReceive;
    }

    /**
     * @return the maxMips
     */
    public double getMaxMips() {
        return maxMips;
    }

    /**
     * @param maxMips the maxMips to set
     */
    public void setMaxMips(double maxMips) {
        this.maxMips = maxMips;
    }

    /**
     * @return the vmList
     */
    public List<Vm> getVmList() {
        return vmList;
    }

    /**
     * @param vmList the vmList to set
     */
    public void setVmList(List<Vm> vmList) {
        this.vmList = vmList;
    }

    /**
     * @return the cloudletList
     */
    public List<Cloudlet> getCloudletList() {
        return cloudletList;
    }

    /**
     * @param cloudletList the cloudletList to set
     */
    public void setCloudletList(List<Cloudlet> cloudletList) {
        this.cloudletList = cloudletList;
    }


}
