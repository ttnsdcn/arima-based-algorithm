/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling;

/**
 *
 * @author Marcio
 */
public class VMCaracteristic{

    private int mips;
    private int peNumber;
    private int ram;
    private long bw;
    private long fileSize;
    private String vmm;

    public VMCaracteristic(){

        setBw(0);
        setFileSize(0);
        setMips(1);
        setPeNumber(0);
        setRam(0);
        setVmm("No created virtual machine");

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
     * @return the peNumber
     */
    public int getPeNumber() {
        return peNumber;
    }

    /**
     * @param peNumber the peNumber to set
     */
    public void setPeNumber(int peNumber) {
        this.peNumber = peNumber;
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
     * @return the bw
     */
    public long getBw() {
        return bw;
    }

    /**
     * @param bw the bw to set
     */
    public void setBw(long bw) {
        this.bw = bw;
    }

    /**
     * @return the fileSize
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize the fileSize to set
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
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

    



}
