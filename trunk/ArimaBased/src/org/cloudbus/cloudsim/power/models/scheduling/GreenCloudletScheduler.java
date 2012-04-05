/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling;

import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.ResCloudlet;

/**
 *
 * @author Marcio
 */
public class GreenCloudletScheduler extends CloudletSchedulerDynamicWorkload{

    public GreenCloudletScheduler(double mips, int pesNumber){
        super(mips, pesNumber);

    }

    @Override
    public double getEstimatedFinishTime(ResCloudlet rcl, double time) {
    	double estimatedTime;

        estimatedTime = time + ((rcl.getRemainingCloudletLength()) / getTotalCurrentAllocatedMipsForCloudlet(rcl, time));

        if(Double.isInfinite(estimatedTime)){

            estimatedTime = time + (rcl.getCloudletLength()*0.5)/getMips();
        }

        return estimatedTime;
	}

}
