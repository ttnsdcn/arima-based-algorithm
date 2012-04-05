/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling;

import java.util.List;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySingleThreshold;

/**
 *
 * @author Marcio Costa Junior - University of São Paulo
 */
public class GreenVmAllocationPolicy extends PowerVmAllocationPolicySingleThreshold{

    /**
	 * Instantiates a new vM provisioner mpp. Although, adding new factors
         * ownnig an efficient power management.
         *
	 *
	 * @param list the list
	 * @param utilizationThreshold the utilization bound
	 */
    public GreenVmAllocationPolicy(List<? extends PowerHost> list, double utilizationThreshold){
        super(list, utilizationThreshold);

    }

    /**
	 * Estimate the power consumption by one specific vm allocation
	 *
	 * @param vm VM specification
	 * @param host host which we'll try to allocate the vm
         *
	 * @return $power power consumption estimative after the allocantion
	 *
	 * @pre $host != null
         * @pre $vm != null
	 * @post $none
	 */
    public double estimatePower(PowerHost host, Vm vm){
        return getPowerAfterAllocation(host, vm);

    }

    /**
	 * Allocates a host for a given VM.
	 *
	 * @param vm VM specification
	 *
	 * @return $true if the host could be allocated; $false otherwise
	 *
	 * @pre $none
	 * @post $none
	 */
    @Override
    public boolean allocateHostForVm(Vm vm){

        //Searching for one disponible host
        for(MoreRealisticHost host : this.<MoreRealisticHost>getHostList()){
            //Checking if that host is turned on
            if(host.getSituation() == MoreRealisticHost.TURNED_ON && !host.isBlock()){
                //Trying to create the vm in the host
                if((isPossibleToCreate(host, vm)) && (host.vmCreate(vm))){
                    //Updating the vm table
                    this.getVmTable().put(vm.getUid(), host);
                    if(!Log.isDisabled()){
                        Log.print(String.format("%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId() + "\n", CloudSim.clock()));
                    }
                    return true;
                }
            }
        }        
        return false;
    }

    /**
	 * Checks if is possible to create a vm in a specific host
	 *
	 * @param vm VM specification
	 * @param host host which we'll try to allocate the vm
	 *
         * @return $true if the host could be allocated; $false otherwise
	 *
	 * @pre $host != null
         * @pre $vm != null
	 * @post $none
	 */
    protected boolean isPossibleToCreate(MoreRealisticHost host, Vm vm){

        double mipsRequest = vm.getCurrentRequestedTotalMips();
        host.setMaxUsedMips((int) (host.getMaxUsedMips() + mipsRequest));

        if(host.getMaxUsedMips() <= host.getSupportedMips()){
            return true;
        }

        host.setMaxUsedMips((int) (host.getMaxUsedMips() - mipsRequest));
        return false;
    }

    
}


