/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling;

import org.cloudbus.cloudsim.power.PowerPe;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;

/**
 *
 * @author Marcio Costa Junior - University of São Paulo
 */
public class MoreRealisticPe extends PowerPe{


    /*Maximum temperature supported by the processor*/
    private double tCase;

    /*Processor's weight*/
    private double weight;

    /*Processor's specific heat*/
    public static final double SPECIFIC_HEAT = 712;

    /**
	 * Instantiates a new MoreRealisticPe.
	 *
	 * @param id the id
	 * @param peProvisioner the PowerPe provisioner
	 * @param powerModel the power model
	 */
	public MoreRealisticPe(int id, PeProvisioner peProvisioner, PowerModel powerModel) {
		super(id, peProvisioner, powerModel);

               //Default values of TCase and processor's weight
               settCase(69);
               setWeight(0.55);
	}

    /**
     * @return the tCase
     */
    public double gettCase() {
        return tCase;
    }

    /**
     * @param tCase the tCase to set
     */
    public void settCase(double tCase) {
        this.tCase = tCase;
    }

    /**
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }
}
