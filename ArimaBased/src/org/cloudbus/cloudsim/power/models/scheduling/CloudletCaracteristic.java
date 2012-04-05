/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling;

/**
 *
 * @author Marcio
 */
public class CloudletCaracteristic {

    private long length;
    private int peNumber;
    private long inputFileSize;
    private long outputFileSize;

    public CloudletCaracteristic(){

        this.setInputFileSize(0);
        this.setOutputFileSize(0);
        this.setLength(0);
        this.setPeNumber(0);
    }

    /**
     * @return the length
     */
    public long getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(long length) {
        this.length = length;
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
     * @return the inputFileSize
     */
    public long getInputFileSize() {
        return inputFileSize;
    }

    /**
     * @param inputFileSize the inputFileSize to set
     */
    public void setInputFileSize(long inputFileSize) {
        this.inputFileSize = inputFileSize;
    }

    /**
     * @return the outputFileSize
     */
    public long getOutputFileSize() {
        return outputFileSize;
    }

    /**
     * @param outputFileSize the outputFileSize to set
     */
    public void setOutputFileSize(long outputFileSize) {
        this.outputFileSize = outputFileSize;
    }

    

}
