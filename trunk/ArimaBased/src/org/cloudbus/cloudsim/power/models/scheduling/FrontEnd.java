
package org.cloudbus.cloudsim.power.models.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 *
 * @author Márcio Costa Junior - University of São Paulo
 *
 *
 */
public class FrontEnd extends DatacenterBroker {

    
    /*This refiring class number*/
    private int classNumber;

    /*Wait queue of virtual machines*/
    private List<Vm> waitQueueVm;

    /*Wait queue of cloudlets*/
    private List<Cloudlet> waitQueueCloudlet;

    /*Number of hosts from front-end's local datacenter*/
    private int numberHosts;

    /*Number of processors that each host has*/
    private int numberPe;

    /*Host's characteristics*/
    private HostCaracteristic hCarac;

    /*Front-end's local datacenter*/
    private LocalDatacenter localDatacenter;

    /*Requests number estimator*/
    private Estimator estimator;
         /*
	 * Created a new Front-End object.
	 *
	 * @param name 	name to be associated with this entity (as
	 * required by Sim_entity class from simjava package)
         * @param classNumber the id from this refering class
	 *
	 * @throws Exception the exception
	 *
	 * @pre name != null
         * @pre classNumber != null
	 * @post $none
	 */
    public FrontEnd(String name, int classNumber) throws Exception{
        super(name);

        /*Inicialization of the privates variables*/
        setNumberHosts(0);
        setClassNumber(classNumber);
        setWaitQueueVm(new ArrayList<Vm>());
        setWaitQueueCloudlet(new ArrayList<Cloudlet>());
        sethCarac(new HostCaracteristic());

        /*Inicialization of the extended variables*/
        setVmList(new ArrayList<Vm>());
        setVmsCreatedList(new ArrayList<Vm>());
        setCloudletList(new ArrayList<Cloudlet>());
        setCloudletSubmittedList(new ArrayList<Cloudlet>());
        setCloudletReceivedList(new ArrayList<Cloudlet>());

        setVmsRequested(0);
        setVmsAcks(0);
        setVmsDestroyed(0);

        setDatacenterIdsList(new LinkedList<Integer>());
        setDatacenterRequestedIdsList(new ArrayList<Integer>());
        setVmsToDatacentersMap(new HashMap<Integer, Integer>());
        setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
    }

        /*
	 * Set the hosts caracteristics in the datacenter
	 *
	 * @param mips: the number of MIPS of the host
         * @param ram: the number of ram of the host
         * @param storage: the number of storage of the host
         * @param bw: the number of bandwave
         * @param timeZone: the time zone of the datacenter
         * @param peCost: the cost to use the processor
         * @param costPerVm: the cost to create a new vm
         * @param costPerStorage: the cost to use the hard drive
         * @param costPerBw: the cost to use the bandwave
         * @param pePotency: the processor's potency
         * @param powerPercent: the processor's consuption energy percentage
         * @param processingCost: the cost to process something
         * @param os: the operacional system used in the host
         * @param arch: the host's architecture
         * @param vmm: the virtual machine manager
         *
	 *
	 * @pre name != null
         * @pre classNumber != null
	 * @post $none
	 */
    public void setHostsCaracteristics(int mips, int ram, long storage, int bw, double timeZone, double peCost, 
            long costPerMem, long costPerStorage, long costPerBw, double pePotency, double powerPercent,
            double processingCost, String os, String arch, String vmm){

            //The processor potency and efficiency
            hCarac.setPePotency(pePotency);
            hCarac.setStaticPowerPercent(powerPercent);
            hCarac.setMips(mips);

            //Memory, storage and bandwave
            hCarac.setRam(ram);
            hCarac.setStorage(storage);
            hCarac.setBw(bw);

            //The datacenter timezone
            hCarac.setTimeZone(timeZone);

            //The cost per bw, storage, memory and processing
            hCarac.setCostPerBW(costPerBw);
            hCarac.setCostPerStorage(costPerStorage);
            hCarac.setCostPerMem(costPerMem);
            hCarac.setProcessingCost(processingCost);

            //The architecture, operacional system and the virtual machine manager
            hCarac.setArch(arch);
            hCarac.setOs(os);
            hCarac.setVmm(vmm);   
    }
        /*
	 * Created a new LocalDatacenter object.
	 *
	 * @param name 	name to be associated with this entity (as
	 * required by Sim_entity class from simjava package)
	 * @return $true if the creation succeeded or $false otherside
         *
	 * @throws Exception the exception
         *
	 * @pre name != null
         * @pre hCarac != null
         * @pre numberHosts != 0
         * @pos the local variable localDatacenter is allocated
	 */
    public boolean createLocalDatacenter(String name) throws Exception{

        //Checking for possible errors
        if(gethCarac() == null){
                Log.printLine("The host's caracteristics aren't informed");
                return false;
        }
        else
        if(numberHosts == 0){
                Log.printLine("There is no hosts in datacenter");
                return false;
            }
        else
        if(getNumberPe() == 0){
            Log.printLine("There is no Pe for each host");
            return false;
        }
        else{
            //List of hosts inside of local datacenter
            List<MoreRealisticHost> hostList = new ArrayList<MoreRealisticHost>();
            GreenVmAllocationPolicy allocationPolicy = new GreenVmAllocationPolicy(hostList, 1.0);

            //Local datacenter creation with "numberHosts" hosts
            for (int i = 0; i < numberHosts; i++) {
                    //The Pes inside each host
                    List<MoreRealisticPe> peList = new ArrayList<MoreRealisticPe>();
                    //Creating the Pe for each host
                    for(int j = 0; j < getNumberPe(); j++){
                        //Adding the Pe multi-core or single-core
                        peList.add(new MoreRealisticPe(0, new PeProvisionerSimple(
                            hCarac.getMips()),
                            new PowerModelLinear(hCarac.getPePotency(), hCarac.getStaticPowerPercent())));
                    }

                    //Creating each host object
                    MoreRealisticHost host =  new MoreRealisticHost(
                                    i,
                                    new RamProvisionerSimple(hCarac.getRam()),
                                    new BwProvisionerSimple(hCarac.getBw()),
                                    hCarac.getStorage(),
                                    peList,
                                    new VmSchedulerTimeShared(peList)
                            );
                    host.setRealisticPeList(peList);
                    host.setAllocationPolicy(allocationPolicy);


                    host.setSupportedMips((int) (hCarac.getMips() + hCarac.getMips() * (0.3)));
                    //Allocating the hosts
                    hostList.add(host);
            }

            //Setting the local datacenter caracteristics
            DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                            hCarac.getArch(),
                            hCarac.getOs(),
                            hCarac.getVmm(),
                            hostList,
                            hCarac.getTimeZone(),
                            hCarac.getProcessingCost(),
                            hCarac.getCostPerMem(),
                            hCarac.getCostPerStorage(),
                            hCarac.getCostPerBW());

            //Creatig the local datacenter with the caracteristics above
            LocalDatacenter localDatacenter = new LocalDatacenter(
                                    name,
                                    characteristics,
                                    allocationPolicy,
                                    new LinkedList<Storage>(),
                                    5.0);
            setLocalDatacenter(localDatacenter);
            getLocalDatacenter().setType(getClassNumber());
            return true;
        }
    }

     /*
	 * Associates a cloudlet to a vm
	 *
	 * @param cloudlet: the cloudlet
         * @param vm: the vm to associate
         *
	 * @pre cloudlet != null
         * @pre vm != null
         * @pos none
	 */
    protected void associateCloudletToVm(Cloudlet cloudlet, Vm vm){

        //Associating the cloudlet to the vm
        cloudlet.setVmId(vm.getId());
        cloudlet.setUserId(vm.getUserId());

        //Updating the local datacenter vm map
        this.getVmsToDatacentersMap().put(vm.getId(), getLocalDatacenter().getId());

    }

    /*
	 * Gets the VmsToDatacenterMap
	 *
         * @return the host's vmToDatacenterMap
         *
	 * @pre none
         * @pos none
	 */
    public Map<Integer, Integer> getVmsToDatacentersMapPublic() {
		return getVmsToDatacentersMap();
	}

    /*
	 * Creates a list of vm and cloudlet in the local datacenter
	 *
	 * @param vmList: the list of vm
         * @param cloudletList: the list of cloudlet
         *
         *@return $true if the creation was successful and $false otherwise
         *
	 * @pre none
         * @pos none
	 */
    public boolean createVmAndCloudlet(List<Vm> vmList, List<Cloudlet> cloudletList) throws InterruptedException{

        if(vmList.size() != cloudletList.size()){
            Log.printLine("The list of vms and the list of cloudlets have different sizes");
            return false;
        }
        else
        if(vmList.isEmpty() || cloudletList.isEmpty()){
            return false;
        }

        boolean changed = false;

        for(int i = 0; i < vmList.size(); i++){
            //Trying to create the virtaul machine
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vmList.get(i).getId() + " in " + localDatacenter.getName());
            if(localDatacenter.getVmAllocationPolicy().allocateHostForVm(vmList.get(i))){
                //Associating the cloudlet to the vm
                associateCloudletToVm(cloudletList.get(i), vmList.get(i));
                //Sending the cloudlet to process
                sendNow(getVmsToDatacentersMap().get(vmList.get(i).getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudletList.get(i));
                //Updating the list of vm and cloudlet
                getVmsCreatedList().add(vmList.get(i));
                getCloudletSubmittedList().add(cloudletList.get(i));
                
            }
            else{
                //Checking if a change happened
                if(!changed){
                   //Send one signal to responsable entity
                   send(getLocalDatacenter().getId(), 0, CloudSimTags.CLEAR_WAIT_QUEUE , this);
                   changed = true;
                }
                   
                    Log.printLine(CloudSim.clock() + ": " + getName() + ": It's not possible to Create VM #" + vmList.get(i).getId() + " in " + localDatacenter.getName());
                    Log.printLine(CloudSim.clock() + ": " + getName() + ": Adding to wait queue");
                    //Adding the remaining vms int the wait queue
                    getWaitQueueVm().add(vmList.get(i));
                    getWaitQueueCloudlet().add(cloudletList.get(i));
            }
        }
        //Sending the most recently request number to this respective estimator
        getEstimator().processEstimator(vmList.size());
        
        return true;
    }

    /*
	 * Clears the wait queues
	 *
         *@return $true if the it was successful and $false otherwise
         *
	 * @pre none
         * @pos none
	 */
    public boolean clearWaitQueue() throws InterruptedException{
        return createVmAndCloudlet(waitQueueVm, waitQueueCloudlet);
    }

    /**
     * @return the classNumber
     */
    public int getClassNumber() {
        return classNumber;
    }

    /**
     * @param classNumber the classNumber to set
     */
    public void setClassNumber(int classNumber) {
        this.classNumber = classNumber;
    }

    /**
     * @return the waitQueueVm
     */
    public List<Vm> getWaitQueueVm() {
        return waitQueueVm;
    }

    /**
     * @param waitQueueVm the waitQueueVm to set
     */
    public void setWaitQueueVm(List<Vm> waitQueueVm) {
        this.waitQueueVm = waitQueueVm;
    }

    /**
     * @return the waitQueueCloudlet
     */
    public List<Cloudlet> getWaitQueueCloudlet() {
        return waitQueueCloudlet;
    }

    /**
     * @param waitQueueCloudlet the waitQueueCloudlet to set
     */
    public void setWaitQueueCloudlet(List<Cloudlet> waitQueueCloudlet) {
        this.waitQueueCloudlet = waitQueueCloudlet;
    }

    /**
     * @return the numberHosts
     */
    public int getNumberHosts() {
        return numberHosts;
    }

    /**
     * @param numberHosts the numberHosts to set
     */
    public void setNumberHosts(int numberHosts) {
        this.numberHosts = numberHosts;
    }

    /**
     * @return the hCarac
     */
    public HostCaracteristic gethCarac() {
        return hCarac;
    }

    /**
     * @param hCarac the hCarac to set
     */
    public void sethCarac(HostCaracteristic hCarac) {
        this.hCarac = hCarac;
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
     * @return the numberPe
     */
    public int getNumberPe() {
        return numberPe;
    }

    /**
     * @param numberPe the numberPe to set
     */
    public void setNumberPe(int numberPe) {
        this.numberPe = numberPe;
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
}
