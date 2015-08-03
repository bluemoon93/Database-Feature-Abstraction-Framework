/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author bluemoon
 */
public class ReadWriteLocks {
    public static void main (String [] args) throws InterruptedException{
        Test2 testasd = new Test2();
        LockThread lt1 = new LockThread(testasd);
        LockThread lt2 = new LockThread(testasd);
        //testasd.testM();
        lt1.start();
        lt2.start();
    }
    
    public static class Test2{
        ReadWriteLock rwLock = new ReentrantReadWriteLock();
        public synchronized void testM() throws InterruptedException{
            System.out.println("lol");
            this.wait();
        }
    }
}
