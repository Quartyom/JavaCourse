package org.example;

public class SessionsThread extends Thread{

    private SessionsHolder holder;
    public SessionsThread(SessionsHolder holder) {
        this.holder = holder;
    }

    @Override
    public void run(){
        while (true){
            holder.update();
            try {
                sleep(5000);            // !!!
            } catch (InterruptedException e) {
                holder.clear();
                return;
            }
        }
    }


}
