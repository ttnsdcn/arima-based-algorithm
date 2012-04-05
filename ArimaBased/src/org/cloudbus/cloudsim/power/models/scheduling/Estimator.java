/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cloudbus.cloudsim.power.models.scheduling;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.scheduling.test.HostTest;

/**
 *
 * @author Marcio
 */
public class Estimator extends SimEntity{

    /**
     * @return the k
     */
    public static double getK() {
        return k;
    }

    /**
     * @return the phi
     */
    public static double getPhi() {
        return phi;
    }

    /**
     * @return the theta
     */
    public static double getTheta() {
        return theta;
    }

    /**
     * @return the sampleSize
     */
    public static int getSampleSize() {
        return sampleSize;
    }

    /**
     * @param aSampleSize the sampleSize to set
     */
    public static void setSampleSize(int aSampleSize) {
        sampleSize = aSampleSize;
    }

    /**
     * @return the cicleSize
     */
    public static double getCicleSize() {
        return cicleSize;
    }

    private int reqNumber;
    private int seasPointer;
    private int supportedVms;
    private double []at;
    private int []St;
    private int []Zt;
    private FrontEnd frontEnd;
    private static double k = 24.73;
    private static double phi = 0.08;
    private static double theta = 2.51;
    private static int sampleSize = 14;
    private double nextCicle;
    public static double cicleSize = 500;
    public static int ESTIMATE = 200;


    public Estimator(String name, int initReqNumber, FrontEnd frontEnd){
        super(name);

        setReqNumber(initReqNumber);
        setAt(new double[sampleSize]);
        setZt(new int[sampleSize]);
        setSeasPointer(0);
        setSupportedVms(5);
        setFrontEnd(frontEnd);        
    }

    @Override
    public void startEntity() {
       Log.printLine("Starting the " + getName());

      Zt[0] = 20; Zt[1] = 25; Zt[2] = 24; Zt[3] = 15; Zt[4] = 13; Zt[5] = 25;
      Zt[6] = 20; Zt[7] = 24; Zt[8] = 28; Zt[9] = 35; Zt[10] = 30; Zt[11] = 28;
      Zt[12] = 25; Zt[13] = 20;

      calculateAt();

      initSeasonalComponent();

      setNextCicle(CloudSim.clock() + getCicleSize());
    }

    @Override
    public void processEvent(SimEvent ev) {

        switch (ev.getTag()){

            case CloudSimTags.MIDDLE_CICLE_FORECAST:
                try {
                    changeDatacenterConfiguration();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Estimator.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

            default:
                try {
                    if(!frontEnd.getWaitQueueVm().isEmpty())
                            frontEnd.clearWaitQueue();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Estimator.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }

       
    }

    @Override
    public void shutdownEntity() {
       printDatacenterConfiguration();
       Log.printLine("Shutting down the " + getName());
    }

    public void processEstimator(int requestNumber){


        if(requestNumber != -1){
             try {
                printDatacenterConfiguration();
                Log.printLine("Estimating the next request number...");
                updateZt(requestNumber);
                Log.printLine("The next request number is " + forecast());
                changeDatacenterConfiguration();
                }
             catch (InterruptedException ex) {
                Logger.getLogger(Estimator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }




    protected void calculateAt(){

        at[0] = 0;
        for(int i = 1; i < getSampleSize(); i++){
            at[i] = Zt[i] - Zt[i - 1] + getTheta()*at[i - 1];
        }

    }

    protected void initSeasonalComponent(){

        setSt(new int[getSampleSize()]);

        for(int i = 0; i < getSampleSize(); i++){
            St[i] = 0;
            
        }

        peaksOfRequests();
    }

    private void peaksOfRequests(){

        St[1] = 20;
    }

    private boolean isPeak(int pointer){

        if(pointer == 1)
            return true;

        return false;
    }

    protected void updateZt(int Z){

        Zt[getSeasPointer()] = Z;

        setSeasPointer(getSeasPointer() + 1);
        setSeasPointer(getSeasPointer() % ((int) getCicleSize()));

        setNextCicle(CloudSim.clock() + getCicleSize());

        if(getSeasPointer() == getSampleSize()){
            setSeasPointer(0);
            calculateAt();           
        }

    }

    public int forecast(){

        int vms = 0;
        double forecastValue;
        
        forecastValue = getPhi()*Zt[getSeasPointer() - 1] + at[getSeasPointer()] 
                - getTheta()*at[getSeasPointer() - 1] + getK() + St[getSeasPointer()];

        vms = (int) forecastValue;

        return vms;
    }

    protected int fakeForecast(int Z){
         int vms = 0;
        double forecastValue;
        int index = getSeasPointer();
        int newIndex = getSeasPointer() + 1;

        index = index%((int) getCicleSize());
        newIndex = newIndex%((int) getCicleSize());

        forecastValue = getPhi()*Z + at[newIndex]
                - getTheta()*at[index] + getK() + St[newIndex];

        vms = (int) forecastValue;

        return vms;

    }

    public void changeDatacenterConfiguration() throws InterruptedException{

        //Primeiro passo: estimar o tempo de execução dos cloudlets

        if(!CloudSim.isPaused())
        CloudSim.pauseSimulation();
        while(true){
            if(CloudSim.isPaused())
                break;
            Thread.sleep(10);
        }

        double mipsAvailable = 0.0;
      //  int numCloudletsFinished = 0;
        int numberVms = 0;
        int fakeNumberVms = 0;
        int forecastValue = forecast();
        double totalMipsRequested = 0.0;
        double meanCaseMips = 500;
        for(Cloudlet cloudlet : getFrontEnd().getCloudletSubmittedList()){
            if(!cloudlet.isFinished()){
              //  numCloudletsFinished++;
                Vm vm = searchById(getFrontEnd().getVmsCreatedList(), cloudlet);
                if((estimateCloudletFinishTime(cloudlet, vm.getMips()) < getNextCicle())){
                    mipsAvailable += vm.getMips();
                }

            }
        }

        //Segundo Passo: Verificar a quantidade de MIPS disponíveis
        for(MoreRealisticHost host : getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList()){
            if(host.getSituation() == host.TURNED_ON && host.getTotalMips() == host.getAvailableMips()){
                mipsAvailable += host.getAvailableMips();
            }
        }

        //Terceiro passo: verificar quantas vms são possíveis de criar
        //Considerando o caso de 25% temos

        numberVms = (int) Math.round(mipsAvailable / (meanCaseMips));

        //Quarto passo: verificar quantos hosts são necessários a mudar de estado


        numberVms = forecastValue - numberVms;
        totalMipsRequested = Math.abs(numberVms)*meanCaseMips;

        fakeNumberVms = fakeForecast(forecastValue);
        int ic = forecastValue - fakeNumberVms;
        double securityValue = forecastValue*0.3;

        //Significa que a quantidade de hosts ligados até o momento não é suficiente
        if(numberVms > 0){
            int numHosts;
            int numTurnedOn = 0;
            int numberSb = 0;
            numHosts = (int) Math.round((numberVms * meanCaseMips) / (getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList().get(0).getSupportedMips()));
            int numHostsIc = (int) Math.round((Math.abs(ic) * meanCaseMips) / (getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList().get(0).getSupportedMips()));
            if(numHosts > 0){
                for(MoreRealisticHost host : getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList()){

                    //Ligando a quantidade de hosts necessária
                    if(numTurnedOn < numHosts){
                        if(host.getSituation() != host.TURNED_ON){
                            send(getFrontEnd().getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
                            double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
                            send(getFrontEnd().getLocalDatacenter().getId(), time, CloudSimTags.UNBLOCKING_HOST, host);
                            numTurnedOn++;
                        }
                    }
                    else{
                        //Caso ic percentença ao intervalo de confiança:
                        //  -deixar um host em sb
                        //  -hibernar o resto
                        if(Math.abs(ic) < securityValue && host.getTotalMips() == host.getAvailableMips()){
                            
                            if(numberSb < 1 && host.getSituation() != host.STANDBY){
                                send(getFrontEnd().getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
                                double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
                                time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.STANDBY);
                                send(getFrontEnd().getLocalDatacenter().getId(), time, CloudSimTags.UNBLOCKING_HOST, host);
                                numberSb++;
                            }
                            else
                            if(host.getSituation() != host.HIBERNATING){
                                send(getFrontEnd().getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
                                double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
                                time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.HIBERNATING);
                                send(getFrontEnd().getLocalDatacenter().getId(), time, CloudSimTags.UNBLOCKING_HOST, host);
                            }
                        }
                        else
                        //indica uma queda no número de requisições
                        //  -hibernar o resto
                        if(ic > securityValue && host.getTotalMips() == host.getAvailableMips() && host.getSituation() != host.HIBERNATING){
                                send(getFrontEnd().getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
                                double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
                                time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.HIBERNATING);
                                send(getFrontEnd().getLocalDatacenter().getId(), time, CloudSimTags.UNBLOCKING_HOST, host);
                        }
                        //Indica que as requisições irão subir
                        //  -Com base em ic,deixar x hosts em sb
                        //  -Hibernar o resto
                        else
                        if(host.getTotalMips() == host.getAvailableMips()){
                          
                            if(numberSb < numHostsIc && host.getSituation() != host.STANDBY){
                                send(getFrontEnd().getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
                                double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
                                time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.STANDBY);
                                send(getFrontEnd().getLocalDatacenter().getId(), time, CloudSimTags.UNBLOCKING_HOST, host);
                                numberSb++;
                            }
                            else
                            if(host.getSituation() != host.HIBERNATING){
                                send(getFrontEnd().getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
                                double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
                                time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.HIBERNATING);
                                send(getFrontEnd().getLocalDatacenter().getId(), time, CloudSimTags.UNBLOCKING_HOST, host);

                            }
                        }
                    }
                }
            }

        }
        else
        if(numberVms < 0){
            if(Math.abs(ic) < securityValue){
                int numHosts;
                int numberHb = 0;
                numHosts = (int) Math.round((Math.abs(numberVms) * meanCaseMips) / (getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList().get(0).getSupportedMips()));
                for(MoreRealisticHost host : getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList()){
                        
                        if(host.getTotalMips() == host.getAvailableMips() && numberHb < numHosts && host.getSituation() != host.HIBERNATING){
                            send(getFrontEnd().getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
                            double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
                            time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.HIBERNATING);
                            send(getFrontEnd().getLocalDatacenter().getId(), time, CloudSimTags.UNBLOCKING_HOST, host);
                            numberHb++;
                        }
                    }
            }
            else
            if(ic > securityValue){
                int numHosts;
                int numberHb = 0;
                int numberSbIc = 0;
                numHosts = (int) Math.round((Math.abs(numberVms) * meanCaseMips) / (getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList().get(0).getSupportedMips()));
                int numHostsIc = (int) Math.round((Math.abs(ic) * meanCaseMips) / (getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList().get(0).getSupportedMips()));
                for(MoreRealisticHost host : getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList()){
                    
                    if(host.getTotalMips() == host.getAvailableMips() && numberHb < numHosts && host.getSituation() != host.HIBERNATING){
                            send(getFrontEnd().getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
                            double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
                            time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.HIBERNATING);
                            send(getFrontEnd().getLocalDatacenter().getId(), time, CloudSimTags.UNBLOCKING_HOST, host);
                            numberHb++;
                    }
                    else
                    if(host.getTotalMips() == host.getAvailableMips() && numberSbIc < numHostsIc && numberHb >= numHosts && host.getSituation() != host.STANDBY){
                            send(getFrontEnd().getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
                            double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
                            time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.STANDBY);
                            send(getFrontEnd().getLocalDatacenter().getId(), time, CloudSimTags.UNBLOCKING_HOST, host);
                            numberSbIc++;
                    }
                }


            }
            else{
                int numHosts;
                int numberHb = 0;
                int numberSbIc = 0;
                numHosts = (int) Math.round(((Math.abs(numberVms) - Math.abs(ic)) * meanCaseMips) / (getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList().get(0).getSupportedMips()));
                int numHostsIc = (int) Math.round((Math.abs(ic) * meanCaseMips) / (getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList().get(0).getSupportedMips()));
                for(MoreRealisticHost host : getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList()){
                    
                    if(host.getTotalMips() == host.getAvailableMips() && numberSbIc < numHostsIc && host.getSituation() != host.STANDBY){
                            send(getFrontEnd().getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
                            double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
                            time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.STANDBY);
                            send(getFrontEnd().getLocalDatacenter().getId(), time, CloudSimTags.UNBLOCKING_HOST, host);
                            numberSbIc++;
                    }
                    else
                    if(host.getTotalMips() == host.getAvailableMips() && numberHb < numHosts && numberSbIc >= numHostsIc && host.getSituation() != host.HIBERNATING){
                            send(getFrontEnd().getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
                            double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
                            time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.HIBERNATING);
                            send(getFrontEnd().getLocalDatacenter().getId(), time, CloudSimTags.UNBLOCKING_HOST, host);
                            numberHb++;
                    }
                }
            }

        }
        
        //CloudSim.resumeSimulation();
//        if(numberVms > 0){
//            //ligar computadores e usar falsa previsão para ver o estado dos outros
//            int numHosts;
//            int hostsSb = 0;
//            numHosts = (int) Math.round((numberVms * meanCaseMips) / (getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList().get(0).getSupportedMips()));
//            for(MoreRealisticHost host : getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList()){
//                if(hostsSb < numHosts){
//                    if(host.getSituation() == host.STANDBY){
//                        hostsSb++;
//                    }
//                    else
//                    if(host.getSituation() == host.HIBERNATING){
//                        double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
//                        time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.STANDBY);
//                        hostsSb++;
//                    }
//                    else
//                    if(host.getSituation() == host.TURNED_OFF){
//                        double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
//                        time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.STANDBY);
//                        hostsSb++;
//                    }
//                }
//                else{
//                       if(Math.abs(ic) <= forecastValue*(0.3) && (host.getFreePesNumber() == host.getPesNumber())){
//                            double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
//                            time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.HIBERNATING);
//                       }
//                       else
//                       if(ic > forecastValue*(0.3) && isPeak(getSeasPointer() + 1)){
//                                double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
//                                time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.STANDBY);
//                       }
//                }
//            }
//        }
//        else
//        if(numberVms < 0){
//            //desligar computadores e usar falsa previsão para ver o estado dos outros
//            if(Math.abs(ic) <= forecastValue*(0.3)){
//                    for(MoreRealisticHost host : getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList()){
//                        if((host.getSituation() == host.STANDBY) || (host.getSituation() == host.TURNED_ON && host.getFreePesNumber() == host.getPesNumber())){
//                            double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
//                            time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.HIBERNATING);
//                        }
//                    }
//            }
//            else
//            if(ic > forecastValue*(0.3) && isPeak(getSeasPointer() + 1)){
//                int numHosts;
//                int hostsSb = 0;
//                numHosts = (int) Math.round((fakeNumberVms * meanCaseMips) / (getFrontEnd().getLocalDatacenter().getHostList().get(0).getTotalMips()));
//                for(MoreRealisticHost host : getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList()){
//                        if(hostsSb < numHosts){
//                            if(host.getSituation() == host.TURNED_ON && host.getFreePesNumber() == host.getPesNumber()){
//                                    double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.STANDBY);
//                                    hostsSb++;
//                            }
//                            else
//                            if(host.getSituation() == host.HIBERNATING || host.getSituation() == host.TURNED_OFF){
//                                    double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
//                                    time += getFrontEnd().getLocalDatacenter().changeStatus(host, host.STANDBY);
//                                    hostsSb++;
//                            }
//                            else
//                            if(host.getSituation() == host.STANDBY){
//                                hostsSb++;
//                            }
//
//                        }
//                        else
//                            break;
//                    }
//                }
//            else
//            if(ic > forecastValue*(0.3)){
//                int numHosts;
//                int hostsHb = 0;
//                numHosts = (int) Math.round((Math.abs(ic) * meanCaseMips) / (getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList().get(0).getSupportedMips()));
//                for(MoreRealisticHost host : getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList()){
//                    if(hostsHb < numHosts){
//                        if(host.getTotalMips() == host.getAvailableMips()){
//                          //  getFrontEnd().getLocalDatacenter().changeStatus(host, host.TURNED_ON);
//                            send(getFrontEnd().getLocalDatacenter().getId(), 0, CloudSimTags.BLOCKING_HOST, host);
//                            double time = getFrontEnd().getLocalDatacenter().changeStatus(host, host.HIBERNATING);
//                            send(getFrontEnd().getLocalDatacenter().getId(), time, CloudSimTags.UNBLOCKING_HOST, host);
//
//                            hostsHb++;
//                        }
//                    }
//                    else
//                        break;
//                }
//
//            }
//
//        }

       
//        snoop.start();
//        for(Monitor monitor : monitors){
//            monitor.getHost().setBlock(true);
//            monitor.start();
//        }
//
//    // CloudSim.resumeSimulation();
//     while(true){
//         for(int i = 0; i < monitors.size(); i++){
//             if(monitors.get(i).isFinished()){
//                 snoop.getAcks().set(i, Boolean.TRUE);
//                 monitors.get(i).getHost().setBlock(false);
//                 monitors.get(i).stop();
//             }
//         }
//         if(snoop.isFinished()){
//             snoop.stop();
//             printDatacenterConfiguration();
//             break;
//         }
//        else{
//             if(CloudSim.isPaused()){
//                 CloudSim.resumeSimulation();
//             }
//        }
//         Thread.sleep(20);
//     }
//
//     CloudSim.resumeSimulation();
    }


    protected Vm searchById(List<Vm> vmList, Cloudlet cloudlet){

        for(Vm vm : vmList){
            if(vm.getId() == cloudlet.getVmId())
                return vm;
        }

        return null;
    }

    protected double estimateCloudletFinishTime(Cloudlet cloudlet, double vmMips){
        double time;

//        time = (CloudSim.clock() - cloudlet.getCloudletFinishedSoFar())*
//                (cloudlet.getCloudletTotalLength() - cloudlet.getCloudletFinishedSoFar())/
//                (cloudlet.getCloudletFinishedSoFar());
       // double timeFinishedSoFar = CloudSim.clock() - cloudlet.getExecStartTime();
        double totalTime = (cloudlet.getCloudletLength()*2)/vmMips;
        time = CloudSim.clock() + totalTime;
        return time;
    }

    private void printDatacenterConfiguration(){

        Log.printLine("Printing the datacenter " + getFrontEnd().getLocalDatacenter().getName() + " configurationg");

        for(MoreRealisticHost host : getFrontEnd().getLocalDatacenter().<MoreRealisticHost>getHostList()){
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

            Log.printLine("The host #" + host.getId() + " is " + situation);
        }

    }




    /**
     * @return the reqNumber
     */
    public int getReqNumber() {
        return reqNumber;
    }

    /**
     * @param reqNumber the reqNumber to set
     */
    public void setReqNumber(int reqNumber) {
        this.reqNumber = reqNumber;
    }

    /**
     * @return the at
     */
    public double[] getAt() {
        return at;
    }

    /**
     * @param at the at to set
     */
    public void setAt(double[] at) {
        this.at = at;
    }

    /**
     * @return the St
     */
    public int[] getSt() {
        return St;
    }

    /**
     * @param St the St to set
     */
    public void setSt(int[] St) {
        this.St = St;
    }

    /**
     * @return the Zt
     */
    public int[] getZt() {
        return Zt;
    }

    /**
     * @param Zt the Zt to set
     */
    public void setZt(int[] Zt) {
        this.Zt = Zt;
    }

    /**
     * @return the seasPointer
     */
    public int getSeasPointer() {
        return seasPointer;
    }

    /**
     * @param seasPointer the seasPointer to set
     */
    public void setSeasPointer(int seasPointer) {
        this.seasPointer = seasPointer;
    }

    /**
     * @return the supportedVms
     */
    public int getSupportedVms() {
        return supportedVms;
    }

    /**
     * @param supportedVms the supportedVms to set
     */
    public void setSupportedVms(int supportedVms) {
        this.supportedVms = supportedVms;
    }

    /**
     * @return the frontEnd
     */
    public FrontEnd getFrontEnd() {
        return frontEnd;
    }

    /**
     * @param frontEnd the frontEnd to set
     */
    public void setFrontEnd(FrontEnd frontEnd) {
        this.frontEnd = frontEnd;
    }

    /**
     * @return the nextCicle
     */
    public double getNextCicle() {
        return nextCicle;
    }

    /**
     * @param nextCicle the nextCicle to set
     */
    public void setNextCicle(double nextCicle) {
        this.nextCicle = nextCicle;
    }

}
