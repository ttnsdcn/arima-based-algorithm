/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 *This class represents the global broker that manages the all system
 *
 *
 * @author Marcio Costa Junior - University of Sao Paulo
 */
public final class Middleware extends SimEntity{

    private List<Class> classList;
    private double mipsDivisor;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;

     /*
	 * Created a new Global Broker object.
	 *
	 * @param name 	name to be associated with this entity (as
	 * required by Sim_entity class from simjava package)
	 *
	 * @throws Exception the exception
	 *
	 * @pre name != null
	 * @post $none
	 */
    public Middleware(String name, List<Class> classList, double mipsDivisor) throws Exception{
        super(name);
        setClassList(classList);
        setMipsDivisor(mipsDivisor);

        setVmList(new ArrayList<Vm>());
        setCloudletList(new ArrayList<Cloudlet>());
    }

    @Override
    public void startEntity() {
        System.out.println("Iniciando middleware");
        for(int i = 0; i < getClassList().size(); i++){
            getClassList().get(i).getFrontEnd().startEntity();
            //getClassList().get(i).getLocalDatacenter().startEntity();
            getClassList().get(i).getEstimator().startEntity();
        }
        
    }

    @Override
    public void processEvent(SimEvent ev) {
       System.out.println("Processando o evento");
    }

    @Override
    public void shutdownEntity() {
        System.out.println("Terminando a entidade");
    }


    private boolean receiveRequest(Vm vm, Cloudlet cloudlet){
        if(vm == null || cloudlet == null){
            return false;
        }
        
        getVmList().add(vm);
        getCloudletList().add(cloudlet);

        return true;
    }

    private void receiveAnalysis(Class cl, boolean analysis){
        cl.setCanReceive(analysis);
    }

    private void processRequest(){

        boolean index;
        int count = 0;
        for(Vm vm : getVmList()){
            index = false;
            for(int i = 0; i < getClassList().size() || !index; i++){
                if(vm.getMips() <= getClassList().get(i).getMaxMips() && getClassList().get(i).isCanReceive()){
                    getClassList().get(i).getVmList().add(vm);
                    getClassList().get(i).getCloudletList().add(getCloudletList().get(count));
                    count++;
                }
            }
            if(!index)
                Log.formatLine("%.2f: It's not possible to send the vm #%d", CloudSim.clock(), vm.getId());
            
        }

    }

    private void sendRequest(){

        for(Class cl :  getClassList()){
            cl.getFrontEnd().submitVmList(cl.getVmList());
            cl.getFrontEnd().submitCloudletList(cl.getCloudletList());
            try {
                cl.getFrontEnd().createVmAndCloudlet(cl.getVmList(), cl.getCloudletList());
            } catch (InterruptedException ex) {
                Logger.getLogger(Middleware.class.getName()).log(Level.SEVERE, null, ex);
            }
            cl.getVmList().clear();
            cl.getCloudletList().clear();
        }
    }



    /**
     * @return the classList
     */
    public List<Class> getClassList() {
        return classList;
    }

    /**
     * @param classList the classList to set
     */
    public void setClassList(List<Class> classList) {
        this.classList = classList;
    }

    /**
     * @return the mipsDivisor
     */
    public double getMipsDivisor() {
        return mipsDivisor;
    }

    /**
     * @param mipsDivisor the mipsDivisor to set
     */
    public void setMipsDivisor(double mipsDivisor) {
        this.mipsDivisor = mipsDivisor;
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
