/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling.test;

import cloudsim.Timer;
import cloudsim.TimerListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.models.scheduling.FrontEnd;
import org.cloudbus.cloudsim.power.models.scheduling.MoreRealisticHost;


/**
 *
 * This is a simple test to the front end with:
 * - 10 vms
 * - 3 hosts turned on
 * - 1 host at stand by
 * - 1 host hibernating
 * - 1 host turned off
 *
 *This is a adaptaion of the DVFS example
 *
 * @author Marcio Costa Junior - Unisersity of Sao Paulo
 */
public class MediumTestFrondEnd implements TimerListener{

	private static List<Cloudlet> cloudletList;
	private static List<Vm> vmList;

	private static double vmsNumber = 5;
	private static double cloudletsNumber = 5;
        private static FrontEnd frontEnd;
        private Timer timer;

        public Timer getTimer(){
            return timer;
        }

        public static void main(String[] args) {

		Log.printLine("Small Test Front End");

                MediumTestFrondEnd test = new MediumTestFrondEnd();
                test.createThread();

		try {
                        //Setting the CloudSim proprieties
			int num_user = 1; 
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; 
			CloudSim.init(num_user, calendar, trace_flag);
                        //Creating one front end and one local datacenter
                        frontEnd = new FrontEnd("FrontEnd_0", 0);
                        frontEnd.setNumberHosts(6);
                        frontEnd.setNumberPe(1);

                        frontEnd.setHostsCaracteristics(3000, 10000, 1000000, 100000,
                                10.0, 3.0, (long) 0.05, (long) 0.001, (long) 0.0, 250,
                                0.7, 3.0, "Linux", "x86", "Xen");

                        frontEnd.createLocalDatacenter("LocalDatacenter_0");
                        frontEnd.getLocalDatacenter().setDisableMigrations(true);

                        //Changing each host status
//                        frontEnd.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(0), MoreRealisticHost.TURNED_ON);
//                        frontEnd.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(1), MoreRealisticHost.TURNED_ON);
//                        frontEnd.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(2), MoreRealisticHost.TURNED_ON);
//                        frontEnd.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(3), MoreRealisticHost.STANDBY);
//                        frontEnd.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(4), MoreRealisticHost.HIBERNATING);
//                        frontEnd.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(5), MoreRealisticHost.TURNED_OFF);

			int frontEndId = frontEnd.getId();

			vmList = createVms(frontEndId, 0);
			frontEnd.submitVmList(vmList);
			cloudletList = createCloudletList(frontEndId, 0);
			frontEnd.submitCloudletList(cloudletList);

                        test.timer.start();
			// Sixth step: Starts the simulation
			double lastClock = CloudSim.startSimulation();

			CloudSim.stopSimulation();

                        int totalTotalRequested = 0;
		    int totalTotalAllocated = 0;
		    ArrayList<Double> sla = new ArrayList<Double>();
		    int numberOfAllocations = 0;
			for (Entry<String, List<List<Double>>> entry : frontEnd.getLocalDatacenter().getUnderAllocatedMips().entrySet()) {
			    List<List<Double>> underAllocatedMips = entry.getValue();
			    double totalRequested = 0;
			    double totalAllocated = 0;
			    for (List<Double> mips : underAllocatedMips) {
			    	if (mips.get(0) != 0) {
			    		numberOfAllocations++;
			    		totalRequested += mips.get(0);
			    		totalAllocated += mips.get(1);
			    		double _sla = (mips.get(0) - mips.get(1)) / mips.get(0) * 100;
			    		if (_sla > 0) {
			    			sla.add(_sla);
			    		}
			    	}
				}
			    totalTotalRequested += totalRequested;
			    totalTotalAllocated += totalAllocated;
			}

			double averageSla = 0;
			if (sla.size() > 0) {
			    double totalSla = 0;
			    for (Double _sla : sla) {
			    	totalSla += _sla;
				}
			    averageSla = totalSla / sla.size();
			}

			Log.printLine();
			Log.printLine(String.format("Total simulation time: %.2f sec", lastClock));
			Log.printLine(String.format("Energy consumption: %.2f kWh", frontEnd.getLocalDatacenter().getPower() / (3600 * 1000)));
			Log.printLine(String.format("Hosts use energy consumption: %.2f kWh", frontEnd.getLocalDatacenter().getEnergyConsumed() / (3600 * 1000)));
                        Log.printLine(String.format("Number of VM migrations: %d", frontEnd.getLocalDatacenter().getMigrationCount()));
			Log.printLine(String.format("Number of SLA violations: %d", sla.size()));
			Log.printLine(String.format("SLA violation percentage: %.2f%%", (double) sla.size() * 100 / numberOfAllocations));
			Log.printLine(String.format("Average SLA violation: %.2f%%", averageSla));
			Log.printLine();


		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}

		Log.printLine("Small Test Front End Finished!");
	}

        private static List<Vm> createVms(int brokerId, int ini) {
		List<Vm> vms = new ArrayList<Vm>();

		// VM description
		int[] mips = { 250, 500, 750, 1000 }; // MIPSRating
		int pesNumber = 1; // number of cpus
		int ram = 128; // vm memory (MB)
		long bw = 2500; // bandwidth
		long size = 2500; // image size (MB)
		String vmm = "Xen"; // VMM name

		for (int i = ini; i < vmsNumber; i++) {
			vms.add(
				new Vm(i, brokerId, mips[i % mips.length], pesNumber, ram, bw, size, vmm, new CloudletSchedulerDynamicWorkload(mips[i % mips.length], pesNumber))
			);
		}

		return vms;
	}

        private static List<Cloudlet> createCloudletList(int brokerId, int ini) {
		List<Cloudlet> list = new ArrayList<Cloudlet>();

		long length = 150000; // 10 min on 250 MIPS
		int pesNumber = 1;
		long fileSize = 300;
		long outputSize = 300;

		for (int i = ini; i < cloudletsNumber; i++) {
			Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, new UtilizationModelStochastic(), new UtilizationModelStochastic(), new UtilizationModelStochastic());
			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(i);
			list.add(cloudlet);
		}

		return list;
	}

    public void update() {
        System.out.println("Esperei");
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            Logger.getLogger(MediumTestFrondEnd.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Mudando o status");
        try {
            frontEnd.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(3), MoreRealisticHost.HIBERNATING);
            System.out.println("Mudado");
            /*vmsNumber = 10;
            cloudletsNumber = 10;
            vmList = createVms(frontEnd.getId(), 5);
            cloudletList = createCloudletList(frontEnd.getId(), 5);
            frontEnd.createVmAndCloudlet(vmList, cloudletList);*/
        } catch (InterruptedException ex) {
            Logger.getLogger(MediumTestFrondEnd.class.getName()).log(Level.SEVERE, null, ex);
        }


        
    }
    public void createThread(){
        timer = new Timer(this);

    }

}
