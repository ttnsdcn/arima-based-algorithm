/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cloudsim;

/**
 *
 * @author Marcio
 */
public class Timer extends Thread{

    private TimerListener listener;
    private int request;
    private long time;
 
    public Timer(TimerListener listener){
        this.listener = listener;

        setRequest(0);
    }

    public void run(){
        listener.update();
    }

    /**
     * @return the request
     */
    public synchronized int getRequest() {
        return request;
    }

    /**
     * @param request the request to set
     */
    public synchronized void setRequest(int request) {
        this.request = request;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

}
