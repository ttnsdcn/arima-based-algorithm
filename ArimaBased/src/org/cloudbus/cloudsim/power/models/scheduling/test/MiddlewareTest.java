/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.models.scheduling.Estimator;
import org.cloudbus.cloudsim.power.models.scheduling.FrontEnd;
import org.cloudbus.cloudsim.power.models.scheduling.Middleware;
import org.cloudbus.cloudsim.power.models.scheduling.MoreRealisticHost;
import org.cloudbus.cloudsim.power.models.scheduling.Class;

/**
 *
 * This is a simple test to the front end with:
 * - 5 vms
 * - 3 hosts turned on
 * - 1 host at stand by
 * - 1 host hibernating
 * - 1 host turned off
 *
 *This is a adaptaion of the DVFS example
 *
 * @author Marcio Costa Junior - Unisersity of Sao Paulo
 */
public class MiddlewareTest {

	private static List<Cloudlet> cloudletList;
	private static List<Vm> vmList;

	private static double vmsNumber = 20;
	private static double cloudletsNumber = 20;

        public static void main(String[] args) {

		Log.printLine("Small Test Front End");

		try {
                        //Setting the CloudSim proprieties
			int num_user = 1; 
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; 
			CloudSim.init(num_user, calendar, trace_flag);

                        //Creating one front end and one local datacenter
                        FrontEnd frontEnd = new FrontEnd("FrontEnd_0", 0);
                        frontEnd.setNumberHosts(6);
                        frontEnd.setNumberPe(1);

                        frontEnd.setHostsCaracteristics(3000, 10000, 1000000, 100000,
                                10.0, 3.0, (long) 0.05, (long) 0.001, (long) 0.0, 250,
                                0.7, 3.0, "Linux", "x86", "Xen");

                        frontEnd.createLocalDatacenter("LocalDatacenter_0");
                        frontEnd.getLocalDatacenter().setDisableMigrations(true);

                        //Changing each host status
                        frontEnd.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(0), MoreRealisticHost.TURNED_ON);
                        frontEnd.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(1), MoreRealisticHost.TURNED_ON);
                        frontEnd.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(2), MoreRealisticHost.TURNED_ON);
                        frontEnd.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(3), MoreRealisticHost.TURNED_ON);
                        frontEnd.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(4), MoreRealisticHost.HIBERNATING);
                        frontEnd.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(5), MoreRealisticHost.TURNED_OFF);

			int frontEndId = frontEnd.getId();

			vmList = createVms(frontEndId);
			frontEnd.submitVmList(vmList);
			cloudletList = createCloudletList(frontEndId);
			frontEnd.submitCloudletList(cloudletList);

                        Estimator estimador = new Estimator("Estimador_0", (int) vmsNumber, frontEnd);

                        List<Class> classList = new ArrayList<Class>();
                        Class cl = new Class(0, frontEnd, frontEnd.getLocalDatacenter(), estimador, 1000.0);
                        classList.add(cl);

                        //Creating one front end and one local datacenter
                        FrontEnd frontEnd2 = new FrontEnd("FrontEnd_1", 1);
                        frontEnd2.setNumberHosts(6);
                        frontEnd2.setNumberPe(1);

                        frontEnd2.setHostsCaracteristics(3000, 10000, 1000000, 100000,
                                10.0, 3.0, (long) 0.05, (long) 0.001, (long) 0.0, 250,
                                0.7, 3.0, "Linux", "x86", "Xen");

                        frontEnd2.createLocalDatacenter("LocalDatacenter_1");
                        frontEnd2.getLocalDatacenter().setDisableMigrations(true);

                        //Changing each host status
                        frontEnd2.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(0), MoreRealisticHost.TURNED_ON);
                        frontEnd2.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(1), MoreRealisticHost.TURNED_ON);
                        frontEnd2.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(2), MoreRealisticHost.TURNED_ON);
                        frontEnd2.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(3), MoreRealisticHost.TURNED_ON);
                        frontEnd2.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(4), MoreRealisticHost.HIBERNATING);
                        frontEnd2.getLocalDatacenter().changeStatus((MoreRealisticHost) frontEnd.getLocalDatacenter().getHostList().get(5), MoreRealisticHost.TURNED_OFF);

			frontEndId = frontEnd2.getId();

                        vmsNumber += 20;
                        cloudletsNumber += 20;
			vmList = createVms2(frontEndId);
			frontEnd2.submitVmList(vmList);
			cloudletList = createCloudletList2(frontEndId);
			frontEnd2.submitCloudletList(cloudletList);

                        Estimator estimador2 = new Estimator("Estimador_1", (int) vmsNumber, frontEnd2);

                        Class cl2 = new Class(1, frontEnd2, frontEnd2.getLocalDatacenter(), estimador2, 2000.0);
                        classList.add(cl);

                       Middleware mid = new Middleware("Middleware", classList, 1000.0);

			// Sixth step: Starts the simulation
			double lastClock = CloudSim.startSimulation();

			CloudSim.stopSimulation();

                       


		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}

		
	}

        private static List<Vm> createVms(int brokerId) {
		List<Vm> vms = new ArrayList<Vm>();

		// VM description
		int[] mips = { 250, 500, 750, 1000 }; // MIPSRating
		int pesNumber = 1; // number of cpus
		int ram = 128; // vm memory (MB)
		long bw = 2500; // bandwidth
		long size = 2500; // image size (MB)
		String vmm = "Xen"; // VMM name

		for (int i = 0; i < vmsNumber; i++) {
			vms.add(
				new Vm(i, brokerId, mips[i % mips.length], pesNumber, ram, bw, size, vmm, new CloudletSchedulerDynamicWorkload(mips[i % mips.length], pesNumber))
			);
		}

		return vms;
	}

        private static List<Cloudlet> createCloudletList(int brokerId) {
		List<Cloudlet> list = new ArrayList<Cloudlet>();

		long length = 150000; // 10 min on 250 MIPS
		int pesNumber = 1;
		long fileSize = 300;
		long outputSize = 300;

		for (int i = 0; i < cloudletsNumber; i++) {
			Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, new UtilizationModelStochastic(), new UtilizationModelStochastic(), new UtilizationModelStochastic());
			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(i);
			list.add(cloudlet);
		}

		return list;
	}private static List<Vm> createVms2(int brokerId) {
		List<Vm> vms = new ArrayList<Vm>();

		// VM description
		int[] mips = { 1250, 1500, 1750, 2000 }; // MIPSRating
		int pesNumber = 1; // number of cpus
		int ram = 128; // vm memory (MB)
		long bw = 2500; // bandwidth
		long size = 2500; // image size (MB)
		String vmm = "Xen"; // VMM name

		for (int i = 20; i < vmsNumber; i++) {
			vms.add(
				new Vm(i, brokerId, mips[i % mips.length], pesNumber, ram, bw, size, vmm, new CloudletSchedulerDynamicWorkload(mips[i % mips.length], pesNumber))
			);
		}

		return vms;
	}

        private static List<Cloudlet> createCloudletList2(int brokerId) {
		List<Cloudlet> list = new ArrayList<Cloudlet>();

		long length = 150000; // 10 min on 250 MIPS
		int pesNumber = 1;
		long fileSize = 300;
		long outputSize = 300;

		for (int i = 20; i < cloudletsNumber; i++) {
			Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, new UtilizationModelStochastic(), new UtilizationModelStochastic(), new UtilizationModelStochastic());
			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(i);
			list.add(cloudlet);
		}

		return list;
	}

}
