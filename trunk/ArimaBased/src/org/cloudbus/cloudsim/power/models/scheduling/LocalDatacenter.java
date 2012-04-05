/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

/**
 *
 * Represents each local datacenter administered for that's respective front-end
 *
 * @author Marcio Costa Junior - University of Sao Paulo
 */
public class LocalDatacenter extends PowerDatacenter{

    //Represents the state in the hierarchy
    private int type;

    //The number of hosts that are turned on
    private int numTunedOn;

    //The number of hosts that are turned off
    private int numTunedOff;

    //The number of hosts that are hibernating
    private int numHibernating;

    //The number of hosts that are stand by
    private int numStandBy;

    //The energy consumed by this dayacenter
    private double energyConsumed;

    private long time;
    private boolean run;


    /**
	 * Instantiates a new datacenter.
	 *
	 * @param name the name
	 * @param characteristics the res config
	 * @param schedulingInterval the scheduling interval
	 * @param utilizationBound the utilization bound
	 * @param vmAllocationPolicy the vm provisioner
	 * @param storageList the storage list
	 *
	 * @throws Exception the exception
	 */
    public LocalDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

		setPower(0.0);
		setDisableMigrations(false);
		setCloudletSubmitted(-1);
		setMigrationCount(0);

                setNumHibernating(0);
                setNumStandBy(0);
                setNumTunedOff(0);
                setNumTunedOn(0);
                setType(0);
                setEnergyConsumed(0.0);
                run = false;
	}
    /**
	 * Change the status of a specific host
	 *
	 * @param host the host to change the status
	 * @param status the new host's status
         * @return energy the energy consumed during the process
	 *
         *@throws InterruptedException the exception
	 */
    public double changeStatus(final MoreRealisticHost host, int status) throws InterruptedException{
        //The energy consumed during the process
        double energy = 0.0;
        double newEnergy = getEnergyConsumed();
        //The host's old status
        final int oldStatus = host.getSituation();
        
        final double simTime = CloudSim.clock();

        host.calculateUsedTime(simTime, status, oldStatus);

        //Changing the host's status
        host.setSituation(status);

        //CloudSim.resumeSimulation();
        
        if(simTime != 0.0){
            time = (long) (host.delayToChangeSituation(oldStatus));
        }

        //Getting the energy consumed during the changing
        if(oldStatus != status)
            energy = host.energyToChangeSituation(oldStatus);

        //Updating the energy consumed
        newEnergy += energy;
        setEnergyConsumed(newEnergy);

        return time;
    }

    @Override
    public void processEvent(SimEvent ev) {
    int srcId = -1;
    //Log.printLine(CloudSim.clock()+"[PowerDatacenter]: event received:"+ev.getTag());

    switch (ev.getTag()) {
        // Resource characteristics inquiry
        case CloudSimTags.RESOURCE_CHARACTERISTICS:
            srcId = ((Integer) ev.getData()).intValue();
            sendNow(srcId, ev.getTag(), getCharacteristics());
            break;

            // Resource dynamic info inquiry
        case CloudSimTags.RESOURCE_DYNAMICS:
            srcId = ((Integer) ev.getData()).intValue();
            sendNow(srcId, ev.getTag(), 0);
            break;

        case CloudSimTags.RESOURCE_NUM_PE:
            srcId = ((Integer) ev.getData()).intValue();
            int numPE = getCharacteristics().getPesNumber();
            sendNow(srcId, ev.getTag(), numPE);
            break;

        case CloudSimTags.RESOURCE_NUM_FREE_PE:
            srcId = ((Integer) ev.getData()).intValue();
            int freePesNumber = getCharacteristics().getFreePesNumber();
            sendNow(srcId, ev.getTag(), freePesNumber);
            break;

            // New Cloudlet arrives
        case CloudSimTags.CLOUDLET_SUBMIT:
            processCloudletSubmit(ev, false);
            break;

            // New Cloudlet arrives, but the sender asks for an ack
        case CloudSimTags.CLOUDLET_SUBMIT_ACK:
            processCloudletSubmit(ev, true);
            break;

            // Cancels a previously submitted Cloudlet
        case CloudSimTags.CLOUDLET_CANCEL:
            processCloudlet(ev, CloudSimTags.CLOUDLET_CANCEL);
            break;

            // Pauses a previously submitted Cloudlet
        case CloudSimTags.CLOUDLET_PAUSE:
            processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE);
            break;

            // Pauses a previously submitted Cloudlet, but the sender
            // asks for an acknowledgement
        case CloudSimTags.CLOUDLET_PAUSE_ACK:
            processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE_ACK);
            break;

            // Resumes a previously submitted Cloudlet
        case CloudSimTags.CLOUDLET_RESUME:
            processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME);
            break;

            // Resumes a previously submitted Cloudlet, but the sender
            // asks for an acknowledgement
        case CloudSimTags.CLOUDLET_RESUME_ACK:
            processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME_ACK);
            break;

            // Moves a previously submitted Cloudlet to a different resource
        case CloudSimTags.CLOUDLET_MOVE:
            processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE);
            break;

            // Moves a previously submitted Cloudlet to a different resource
        case CloudSimTags.CLOUDLET_MOVE_ACK:
            processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE_ACK);
            break;

            // Checks the status of a Cloudlet
        case CloudSimTags.CLOUDLET_STATUS:
            processCloudletStatus(ev);
            break;

            // Ping packet
        case CloudSimTags.INFOPKT_SUBMIT:
            processPingRequest(ev);
            break;

            case CloudSimTags.VM_CREATE:
                    processVmCreate(ev, false);
                    break;

            case CloudSimTags.VM_CREATE_ACK:
                    processVmCreate(ev, true);
                    break;

            case CloudSimTags.VM_DESTROY:
                    processVmDestroy(ev, false);
                    break;

            case CloudSimTags.VM_DESTROY_ACK:
                    processVmDestroy(ev, true);
                    break;

            case CloudSimTags.VM_MIGRATE:
                    processVmMigrate(ev, false);
                    break;

            case CloudSimTags.VM_MIGRATE_ACK:
                    processVmMigrate(ev, true);
                    break;

            case CloudSimTags.VM_DATA_ADD:
                    processDataAdd(ev, false);
                    break;

            case CloudSimTags.VM_DATA_ADD_ACK:
                    processDataAdd(ev, true);
                    break;

            case CloudSimTags.VM_DATA_DEL:
                    processDataDelete(ev, false);
                    break;

            case CloudSimTags.VM_DATA_DEL_ACK:
                    processDataDelete(ev, true);
                    break;

            case CloudSimTags.VM_DATACENTER_EVENT:
                    updateCloudletProcessing();
            checkCloudletCompletion();
                    break;

            case CloudSimTags.BLOCKING_HOST:
                    blockHost((MoreRealisticHost) ev.getData());
                    break;
                    
            case CloudSimTags.UNBLOCKING_HOST:
                    unblockHost((MoreRealisticHost) ev.getData());
                    break;

        case CloudSimTags.CLEAR_WAIT_QUEUE:
        try {
            turningOnHosts((FrontEnd) ev.getData());
        } catch (InterruptedException ex) {
            Logger.getLogger(LocalDatacenter.class.getName()).log(Level.SEVERE, null, ex);
        }
                    break;

        case CloudSimTags.CLEAR_WAIT_QUEUE_ACK:
                    clearWaitQueue((FrontEnd) ev.getData());
                    break;

                    // other unknown tags are processed by this method
        default:
            processOtherEvent(ev);
            break;
    }
}


    @Override
    public void shutdownEntity() {

        double energy = getEnergyConsumed();
        List<MoreRealisticHost> hostList = getHostList();

        for(MoreRealisticHost host : hostList){
            host.calculateUsedTime(CloudSim.clock(), host.getSituation() ,host.getSituation());

            energy += host.calculateExtraEnergyConsumption();

        }
        setEnergyConsumed(energy);

        Log.printLine(getName() + " is shutting down...");
    }

    protected void blockHost(MoreRealisticHost host){
        host.setBlock(true);
    }

    protected void unblockHost(MoreRealisticHost host){
        host.setBlock(false);
    }

    protected void turningOnHosts(FrontEnd frontEnd) throws InterruptedException{

        double mips = 0.0;
        int numHosts;

        for(Vm vm : frontEnd.getWaitQueueVm()){
            mips += vm.getMips();
        }

        numHosts = (int) Math.round(mips/frontEnd.getLocalDatacenter().getHostList().get(0).getTotalMips());
        if(numHosts == 0)
            numHosts = 1;

        double time = 0.0;
        boolean sb = false;
        boolean hb = false;
        for(MoreRealisticHost host : frontEnd.getLocalDatacenter().<MoreRealisticHost>getHostList()){
            if(numHosts > 0){
                if(host.getSituation() == host.STANDBY){
                    send(frontEnd.getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
                    double value = frontEnd.getLocalDatacenter().changeStatus(host, host.TURNED_ON);
                    if(!sb){
                        time += value;
                        sb = true;
                    }                        
                    send(frontEnd.getLocalDatacenter().getId(), value, CloudSimTags.UNBLOCKING_HOST, host);
                    numHosts--;
                }
            }
            else
                break;
        }
        for(MoreRealisticHost host : frontEnd.getLocalDatacenter().<MoreRealisticHost>getHostList()){
            if(numHosts > 0){
                if(host.getSituation() == host.HIBERNATING){
                    send(frontEnd.getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
                    double value = frontEnd.getLocalDatacenter().changeStatus(host, host.TURNED_ON);
                    if(!hb){
                        time += value;
                        hb = true;
                    }                        
                    send(frontEnd.getLocalDatacenter().getId(), value, CloudSimTags.UNBLOCKING_HOST, host);
                    numHosts--;
                }
            }
            else
                break;
        }
        send(frontEnd.getLocalDatacenter().getId(), time, CloudSimTags.CLEAR_WAIT_QUEUE_ACK, frontEnd);
    }

    protected void clearWaitQueue(FrontEnd frontEnd){

        List<Vm> values = new ArrayList<Vm>();
        List<Cloudlet> valuesCl = new ArrayList<Cloudlet>();

         if(frontEnd.getWaitQueueVm().isEmpty() || frontEnd.getWaitQueueCloudlet().isEmpty()){
            return ;
        }

//        for(int i = 0; i < frontEnd.getWaitQueueVm().size(); i++){
//            //Trying to create the virtaul machine
//            Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + frontEnd.getWaitQueueVm().get(i).getId() + " in " + frontEnd.getLocalDatacenter().getName());
//            if(frontEnd.getLocalDatacenter().getVmAllocationPolicy().allocateHostForVm(frontEnd.getWaitQueueVm().get(i))){
//                //Associating the cloudlet to the vm
//                frontEnd.associateCloudletToVm(frontEnd.getWaitQueueCloudlet().get(i), frontEnd.getWaitQueueVm().get(i));
//                //Sending the cloudlet to process
//                sendNow(frontEnd.getVmsToDatacentersMapPublic().get(frontEnd.getWaitQueueVm().get(i).getId()), CloudSimTags.CLOUDLET_SUBMIT, frontEnd.getWaitQueueCloudlet().get(i));
//                //Updating the list of vm and cloudlet
//                frontEnd.getVmsCreatedList().add(frontEnd.getWaitQueueVm().get(i));
//                frontEnd.getCloudletSubmittedList().add(frontEnd.getWaitQueueCloudlet().get(i));
//                values.add(frontEnd.getWaitQueueVm().get(i).getId());
//                valuesCl.add(frontEnd.getWaitQueueCloudlet().get(i).getCloudletId());
//            }
//        }

        for(Vm vm : frontEnd.getWaitQueueVm()){

            Cloudlet cl = searchCloudlet(frontEnd.getWaitQueueCloudlet(), vm);
            if(cl == null){
                break;
            }
            //Trying to create the virtaul machine
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId() + " in " + frontEnd.getLocalDatacenter().getName());
            if(frontEnd.getLocalDatacenter().getVmAllocationPolicy().allocateHostForVm(vm)){
                //Associating the cloudlet to the vm
                frontEnd.associateCloudletToVm(cl, vm);
                //Sending the cloudlet to process
                sendNow(frontEnd.getVmsToDatacentersMapPublic().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cl);
                //Updating the list of vm and cloudlet
                frontEnd.getVmsCreatedList().add(vm);
                frontEnd.getCloudletSubmittedList().add(cl);
                values.add(vm);
                valuesCl.add(cl);
            }
        }


        for(int i = 0; i < values.size(); i++){
            frontEnd.getWaitQueueVm().remove(values.get(i));
            frontEnd.getWaitQueueCloudlet().remove(valuesCl.get(i));
        }

        if(!frontEnd.getWaitQueueVm().isEmpty()){
            send(frontEnd.getLocalDatacenter().getId(), Estimator.cicleSize - 50, CloudSimTags.CLEAR_WAIT_QUEUE_ACK, frontEnd);
        }




    }

    protected Cloudlet searchCloudlet(List<Cloudlet> clList, Vm vm){
        for(Cloudlet cl : clList){
            if(cl.getVmId() == vm.getId()){
                return cl;
            }
        }
        return null;
    }

    @Override
    protected void updateCloudletProcessing() {
            if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
                    CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
                    schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
                    return;
            }
            double currentTime = CloudSim.clock();
            double timeframePower = 0.0;

            //////////////////////////////////////////////////////////////////////
            //Início do ciclo de análise
            setRun(false);
            // if some time passed since last processing
            if (currentTime > getLastProcessTime()) {
                    double timeDiff = currentTime - getLastProcessTime();
                    double minTime = Double.MAX_VALUE;

                    Log.printLine("\n");

                    for (PowerHost host : this.<PowerHost>getHostList()) {

                            Log.formatLine("%.2f: Host #%d", CloudSim.clock(), host.getId());

                            double hostPower = 0.0;

                            if (host.getUtilizationOfCpu() > 0) {
                                    try {
                                            hostPower = host.getPower() * timeDiff;
                                            timeframePower += hostPower;
                                    } catch (Exception e) {
                                            e.printStackTrace();
                                    }
                            }

                            Log.formatLine("%.2f: Host #%d utilization is %.2f%%", CloudSim.clock(), host.getId(), host.getUtilizationOfCpu() * 100);
                            Log.formatLine("%.2f: Host #%d energy is %.2f W*sec", CloudSim.clock(), host.getId(), hostPower);
                    }

                    Log.formatLine("\n%.2f: Consumed energy is %.2f W*sec\n", CloudSim.clock(), timeframePower);

                    Log.printLine("\n\n--------------------------------------------------------------\n\n");

                    for (PowerHost host : this.<PowerHost>getHostList()) {
                            Log.formatLine("\n%.2f: Host #%d", CloudSim.clock(), host.getId());

                            double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
                            if (time < minTime) {
                                    minTime = time;
                            }

                            Log.formatLine("%.2f: Host #%d utilization is %.2f%%", CloudSim.clock(), host.getId(), host.getUtilizationOfCpu() * 100);
                    }

                    setPower(getPower() + timeframePower);

                    /** Remove completed VMs **/
                    for (MoreRealisticHost host : this.<MoreRealisticHost>getHostList()) {
                            for (Vm vm : host.getCompletedVms()) {
                                    host.setMaxUsedMips((int) (host.getMaxUsedMips() - vm.getMips()));
                                    GreenVmAllocationPolicy policy = (GreenVmAllocationPolicy) getVmAllocationPolicy();
                                    double power = policy.estimatePower(host, vm);
                                    getVmAllocationPolicy().deallocateHostForVm(vm);
                                    getVmList().remove(vm);
                                    Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
                            }
                    }

                    Log.printLine();

                    if (!isDisableMigrations()) {
                            List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(getVmList());

                            for (Map<String, Object> migrate : migrationMap) {
                                    Vm vm = (Vm) migrate.get("vm");
                                    PowerHost targetHost = (PowerHost) migrate.get("host");
                                    PowerHost oldHost = (PowerHost) vm.getHost();

                                    targetHost.addMigratingInVm(vm);

                                    if (oldHost == null) {
                                            Log.formatLine("%.2f: Migration of VM #%d to Host #%d is started", CloudSim.clock(), vm.getId(), targetHost.getId());
                                    } else {
                                            Log.formatLine("%.2f: Migration of VM #%d from Host #%d to Host #%d is started", CloudSim.clock(), vm.getId(), oldHost.getId(), targetHost.getId());
                                    }

                                    incrementMigrationCount();

                                    vm.setInMigration(true);

                                    /** VM migration delay = RAM / bandwidth + C    (C = 10 sec) **/
                                    send(getId(), vm.getRam() / ((double) vm.getBw() / 8000) + 0, CloudSimTags.VM_MIGRATE, migrate);
                            }
                    }

                    // schedules an event to the next time
                    if (minTime != Double.MAX_VALUE) {
                            CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
                            send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
                    }

                    setLastProcessTime(currentTime);
            }
            ///////////////////////////////////////////////////////////////////////////////////
            //Fim do ciclo de análise
            setRun(true);
	}

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }
    

    /**
     * @return the numTunedOn
     */
    public int getNumTunedOn() {
        return numTunedOn;
    }

    /**
     * @param numTunedOn the numTunedOn to set
     */
    public void setNumTunedOn(int numTunedOn) {
        this.numTunedOn = numTunedOn;
    }

    /**
     * @return the numTunedOff
     */
    public int getNumTunedOff() {
        return numTunedOff;
    }

    /**
     * @param numTunedOff the numTunedOff to set
     */
    public void setNumTunedOff(int numTunedOff) {
        this.numTunedOff = numTunedOff;
    }

    /**
     * @return the numHibernating
     */
    public int getNumHibernating() {
        return numHibernating;
    }

    /**
     * @param numHibernating the numHibernating to set
     */
    public void setNumHibernating(int numHibernating) {
        this.numHibernating = numHibernating;
    }

    /**
     * @return the numStandBy
     */
    public int getNumStandBy() {
        return numStandBy;
    }

    /**
     * @param numStandBy the numStandBy to set
     */
    public void setNumStandBy(int numStandBy) {
        this.numStandBy = numStandBy;
    }

    /**
     * @return the energyConsumed
     */
    public double getEnergyConsumed() {
        return energyConsumed;
    }

    /**
     * @param energyConsumed the energyConsumed to set
     */
    public void setEnergyConsumed(double energyConsumed) {
        this.energyConsumed = energyConsumed;
    }

    /**
     * @return the run
     */
    public boolean isRun() {
        return run;
    }

    /**
     * @param run the run to set
     */
    public void setRun(boolean run) {
        this.run = run;
    }

}
