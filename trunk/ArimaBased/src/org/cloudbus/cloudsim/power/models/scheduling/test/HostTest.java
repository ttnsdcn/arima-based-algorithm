/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling.test;

import cloudsim.TimerListener;
import cloudsim.Timer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.models.scheduling.Estimator;
import org.cloudbus.cloudsim.power.models.scheduling.FrontEnd;
import org.cloudbus.cloudsim.power.models.scheduling.GreenCloudletScheduler;
import org.cloudbus.cloudsim.power.models.scheduling.MoreRealisticHost;

/**
 *
 * @author Marcio
 */
public class HostTest implements TimerListener{

    private Timer timer;
    private static int numberHosts = 20;
    private static int numberPePerHost = 1;
    private static int numberVms = 20;
    private static int numberCloudlets = 20;
    private static List<Vm> vmList;
    private static List<Cloudlet> cloudletList;
    private static FrontEnd frontEnd;
    private static Estimator estimator;
    private static BufferedWriter out;


    private static FileWriter file;

    public static void main(String[] args) throws Exception{
        HostTest test = new HostTest();
        test.timer = new Timer(test);
        file = new FileWriter("result.txt");
        out = new BufferedWriter(file);

        int num_user = 1;
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false;
        CloudSim.init(num_user, calendar, trace_flag);

        frontEnd = new FrontEnd("FrontEnd_0", 0);
        frontEnd.setNumberHosts(numberHosts);
        frontEnd.setNumberPe(numberPePerHost);

        frontEnd.setHostsCaracteristics(3000, 10000, 1000000, 100000,
                10.0, 3.0, (long) 0.05, (long) 0.001, (long) 0.0, 250,
                0.7, 3.0, "Linux", "x86", "Xen");

        frontEnd.createLocalDatacenter("LocalDatacenter_0");
        frontEnd.getLocalDatacenter().setDisableMigrations(true);

        for(int i = 0; i < numberHosts; i++){
            frontEnd.getLocalDatacenter().<MoreRealisticHost>getHostList().get(i).setSituation(MoreRealisticHost.TURNED_ON);
        }
        int frontEndId = frontEnd.getId();

            vmList = createVms(frontEndId, 0);
            frontEnd.submitVmList(vmList);
            cloudletList = createCloudletList(frontEndId, 0);
            frontEnd.submitCloudletList(cloudletList);

            estimator = new Estimator("Estimator_0", numberVms, frontEnd);
            frontEnd.setEstimator(estimator);

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
            Log.printLine(String.format("Energy consumption: %.4f kWh", frontEnd.getLocalDatacenter().getPower() / (3600 * 1000)));
            Log.printLine(String.format("Hosts use energy consumption: %.2f kWh", frontEnd.getLocalDatacenter().getEnergyConsumed() / (3600 * 1000)));
            Log.printLine(String.format("Number of VM migrations: %d", frontEnd.getLocalDatacenter().getMigrationCount()));
            Log.printLine(String.format("Number of SLA violations: %d", sla.size()));
            Log.printLine(String.format("SLA violation percentage: %.2f%%", (double) sla.size() * 100 / numberOfAllocations));
            Log.printLine(String.format("Average SLA violation: %.2f%%", averageSla));
            Log.printLine();


            HostTest.newLine();
            HostTest.writeFile(String.format("\n" +"Total simulation time: %.2f sec" + "\n", lastClock));
            HostTest.newLine();
            HostTest.writeFile(String.format("Energy consumption: %.4f kWh" + "\n", frontEnd.getLocalDatacenter().getPower() / (3600 * 1000)));
            HostTest.newLine();
            HostTest.writeFile(String.format("Hosts use energy consumption: %.2f kWh" + "\n", frontEnd.getLocalDatacenter().getEnergyConsumed() / (3600 * 1000)));
            HostTest.newLine();
            HostTest.writeFile(String.format("Number of VM migrations: %d" + "\n", frontEnd.getLocalDatacenter().getMigrationCount()));
            HostTest.newLine();
            HostTest.writeFile(String.format("Number of SLA violations: %d" + "\n", sla.size()));
            HostTest.newLine();
            HostTest.writeFile(String.format("SLA violation percentage: %.2f%%" + "\n", (double) sla.size() * 100 / numberOfAllocations));
            HostTest.newLine();
            HostTest.writeFile(String.format("Average SLA violation: %.2f%%" + "\n", averageSla));
            HostTest.newLine();

            out.close();


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

		for (int i = ini; i < numberVms; i++) {
			vms.add(
				new Vm(i, brokerId, mips[i % mips.length], pesNumber, ram, bw, size, vmm, new GreenCloudletScheduler(mips[i % mips.length], pesNumber))
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

		for (int i = ini; i < numberVms; i++) {
			Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, new UtilizationModelStochastic(), new UtilizationModelStochastic(), new UtilizationModelStochastic());
			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(i);
			list.add(cloudlet);
		}

		return list;
	}

	
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "\t";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Resource ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId());

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.printLine(indent + "SUCCESS"
					+ indent + indent + cloudlet.getResourceId()
					+ indent + cloudlet.getVmId()
					+ indent + dft.format(cloudlet.getActualCPUTime())
					+ indent + dft.format(cloudlet.getExecStartTime())
					+ indent + indent + dft.format(cloudlet.getFinishTime())
				);
			}
		}
	}

        public static void writeFile(String text){
            try {
                out.write(text);
                
            } catch (IOException e) {
            }

        }

        public static void newLine(){
        try {
            out.newLine();
        } catch (IOException ex) {
            Logger.getLogger(HostTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        }


        public static int request(int i){
            int value = 0;

            switch (i){
                case 0:
                    value = 20;
                    break;
                case 1:
                    value = 25;
                    break;
                case 2:
                    value = 24;
                    break;
                case 3:
                    value = 15;
                    break;
                case 4:
                    value = 13;
                    break;
                case 5:
                    value = 20;
                    break;
                case 6:
                    value = 25;
                    break;
                case 7:
                    value = 24;
                    break;
                case 8:
                    value = 28;
                    break;
                case 9:
                    value = 35;
                    break;
                case 10:
                    value = 30;
                    break;
                case 11:
                    value = 28;
                    break;
                case 12:
                    value = 25;
                    break;
                case 13:
                    value = 20;
                    break;

            }

            return value;
        }



    public void update() {

        boolean isACicle[] =  new boolean[14];
        int cicle = (int) Estimator.cicleSize;
        int j = 0;
        for(int i = 0; i < 14; i++){
            isACicle[i] = false;
        }

        while(true){
            if(CloudSim.clock() >= cicle && !isACicle[j] && frontEnd.getLocalDatacenter().isRun()){
                isACicle[j] = true;
                CloudSim.pauseSimulation();
                while(!CloudSim.isPaused()){
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(HostTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                int pValue = numberVms;
                numberVms += HostTest.request(j);
                List<Vm> vmList = createVms(frontEnd.getId(), pValue);
                frontEnd.submitVmList(vmList);
                pValue = numberCloudlets;
                numberCloudlets += HostTest.request(j);
                List<Cloudlet> cloudletList = createCloudletList(frontEnd.getId(), pValue);
                frontEnd.submitCloudletList(cloudletList);
                Log.printLine("Adding " + HostTest.request(j) + " new Vms...");
                try {
                    frontEnd.createVmAndCloudlet(vmList, cloudletList);
                } catch (InterruptedException ex) {
                    Logger.getLogger(HostTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                j++;
                cicle += Estimator.getCicleSize();

                //imprimindo no arquivo
                HostTest.writeFile("Tempo em cada estado" + CloudSim.clock());
                HostTest.newLine();
                for(MoreRealisticHost host : frontEnd.getLocalDatacenter().<MoreRealisticHost>getHostList()){
                    HostTest.newLine();
                    HostTest.writeFile("\n" + "Host number #" + host.getId() + "\n");
                    HostTest.newLine();
                    HostTest.writeFile("TURNED ON: " + host.getUsedTime()[0] + "\n");
                    HostTest.newLine();
                    HostTest.writeFile("STANDBY: " + host.getUsedTime()[1] + "\n");
                    HostTest.newLine();
                    HostTest.writeFile("HIBERNATING: " + host.getUsedTime()[2] + "\n");
                    HostTest.newLine();
                    HostTest.writeFile("TURNED OFF: " + host.getUsedTime()[3] + "\n");
                    HostTest.newLine();

                }

                HostTest.newLine();
                HostTest.writeFile("Nº de vms criadas: " + frontEnd.getVmsCreatedList().size() + "\n");
                HostTest.newLine();
//                HostTest.writeFile("Energia consumida com transições: " + frontEnd.getLocalDatacenter().getEnergyConsumed() / (3600 * 1000) + "\n");
//                HostTest.newLine();
                HostTest.writeFile("Nº de vms na lista de espera: " + frontEnd.getWaitQueueVm().size() + "\n");
                HostTest.newLine();
                HostTest.writeFile("Estimativa para o próximo ciclo: " + estimator.forecast() + "\n");
                HostTest.newLine();
                HostTest.writeFile("Configuração do data center" + "\n" + "\n");

                HostTest.newLine();
                HostTest.newLine();
                for(MoreRealisticHost host : frontEnd.getLocalDatacenter().<MoreRealisticHost>getHostList()){
                    String situation = null;
                    if(host.getSituation() == host.TURNED_ON)
                        situation = "TURNED ON";
                    else
                    if(host.getSituation() == host.TURNED_OFF)
                        situation = "TURNED OFF";
                    else
                    if(host.getSituation() == host.HIBERNATING)
                        situation = "HIBERNATING";
                    else
                    if(host.getSituation() == host.STANDBY)
                        situation = "STANDBY";

                    HostTest.writeFile("The host #" + host.getId() + " is " + situation + "\n");
                    HostTest.newLine();
                }

                CloudSim.resumeSimulation();
                if(j >= 12)
                    break;
            }
        }

    
    }

}
