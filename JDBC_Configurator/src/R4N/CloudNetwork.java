package R4N;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

public class CloudNetwork extends Thread {

    private InetAddress group, master;
    private MulticastSocket socket;

    public static final int STARTUP = 0, NOMASTER = 1, MASTER = 2, SLAVE = 3, CANDIDATE = 4, ACCEPTs = 5, CONFLICTs = 6, CONSISTENCY = 7;
    public static final String RESOLVE = "RESOLVE", MASTERACK = "MASTERACK", QUIT = "QUIT", MASTERUP = "MASTERUP", SLAVEUP = "SLAVEUP",
            MASTERREQ = "MASTERREQ", CONFLICT = "CONFLICT", ELECTION = "ELECTION", ACCEPT = "ACCEPT", REFUSE = "REFUSE";
    private int state, waitTime = 5000, keepAliveTime = 4000, port, startUpWaitTime = 1000;
    private ArrayList<Object[]> slaves;

    private CloudNetworkListener lcl;

    public CloudNetwork(int port, CloudNetworkListener lcl) {
        this.port = port;
        this.slaves = new ArrayList();
        this.waitTime += (int) (Math.random() * 1000);
        this.lcl = lcl;

        try {
            this.group = InetAddress.getByName("225.0.0.0");
            this.socket = new MulticastSocket(port);
            this.socket.joinGroup(group);
            this.socket.setLoopbackMode(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.state = STARTUP;
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("@" + getLeaderState());
            switch (state) {
                case STARTUP:
                    startup();
                    break;
                case NOMASTER:
                    nomaster();
                    break;
                case MASTER:
                    master();
                    break;
                case SLAVE:
                    slave();
                    break;
                case CANDIDATE:
                    candidate();
                    break;
                case ACCEPTs:
                    accept();
                    break;
                case CONFLICTs:
                    conflict();
                    break;
                case CONSISTENCY:
                    consistency();
                    break;
                default:
                    System.out.println("Unknown state! " + state);
                    return;
            }
        }
    }

    private void broadcast(String text) {
        byte[] buf = text.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
        try {
            socket.send(packet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean contains(ArrayList<Object[]> l, InetAddress o, int port) {
        for (Object[] b : l) {
            if (b[0].equals(o) && b[1].equals(port)) {
                return true;
            }
        }

        return false;
    }

    private DatagramPacket listen() throws Exception {
        socket.setSoTimeout(0);
        byte[] buf = new byte[10];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        return packet;
    }

    private DatagramPacket listen(int timeout) throws Exception {
        if (timeout <= 0) {
            throw new Exception();
        }
        socket.setSoTimeout(timeout);
        byte[] buf = new byte[10];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        return packet;
    }

    private void reply(InetAddress ip, int port, String text) {
        byte[] buf = text.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, port);
        try {
            socket.send(packet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getLeaderState() {
        switch (state) {
            case STARTUP:
                return "STARTUP";
            case NOMASTER:
                return "NOMASTER";
            case MASTER:
                return "MASTER";
            case SLAVE:
                return "SLAVE";
            case CANDIDATE:
                return "CANDIDATE";
            case ACCEPTs:
                return "ACCEPT";
            case CONFLICTs:
                return "CONFLICT";
            case CONSISTENCY:
                return "CONSISTENCY";
        }
        return "";
    }

    private void conflict() {
        lcl.conflict();
        broadcast(RESOLVE);

        long timeStarted = System.currentTimeMillis(), timeEnded = timeStarted + keepAliveTime / 2, currTime;

        while ((currTime = System.currentTimeMillis()) < timeEnded) {
            try {
                DatagramPacket data = listen((int) (timeEnded - currTime));
                System.out.println("@" + getLeaderState() + " - Message: " + new String(data.getData()).trim() + " from " + data.getAddress());

                switch (new String(data.getData()).trim()) {
                    case MASTERACK:
                        reply(data.getAddress(), data.getPort(), QUIT);
                        break;
                    default:
                        System.out.println("@" + getLeaderState() + " - Unprocessed Message: " + new String(data.getData()));
                        break;
                }
            } catch (Exception ex) {
                //time out
            }
        }

        state = MASTER;
    }

    private void master() {
        lcl.master();
        broadcast(MASTERUP);

        while (state == MASTER) {
            for (Object[] s : slaves) {
                System.out.println("@" + getLeaderState() + " - Sending MASTERUP to " + (InetAddress) s[0]);
                reply((InetAddress) s[0], (int) s[1], MASTERUP);
            }

            long timeStarted = System.currentTimeMillis(), timeEnded = timeStarted + keepAliveTime, currTime;

            while ((currTime = System.currentTimeMillis()) < timeEnded) {
                try {
                    DatagramPacket data = listen((int) (timeEnded - currTime));
                    System.out.println("@" + getLeaderState() + " - Message: " + new String(data.getData()).trim() + " from " + data.getAddress());

                    switch ((new String(data.getData())).trim()) {
                        case SLAVEUP:
                            if (!contains(slaves, data.getAddress(), data.getPort())) {
                                slaves.add(new Object[]{data.getAddress(), data.getPort()});
                            }
                            break;
                        case MASTERREQ:
                            reply(data.getAddress(), data.getPort(), MASTERACK);
                            if (!contains(slaves, data.getAddress(), data.getPort())) {
                                slaves.add(new Object[]{data.getAddress(), data.getPort()});
                            }
                            break;
                        case CONFLICT:
                            state = CONFLICTs;
                            return;
                        case RESOLVE:
                            reply(data.getAddress(), data.getPort(), MASTERACK);
                            break;
                        case QUIT:
                            state = SLAVE;
                            return;
                        case ELECTION:
                            reply(data.getAddress(), data.getPort(), QUIT);
                            if (!contains(slaves, data.getAddress(), data.getPort())) {
                                slaves.add(new Object[]{data.getAddress(), data.getPort()});
                            }
                            break;
                        default:
                            System.out.println("@" + getLeaderState() + " - Unprocessed Message: " + new String(data.getData()));
                            break;
                    }
                } catch (Exception ex) {
                    //time out
                }
            }
        }
        System.out.println("Leaving state MASTER because -> " + getLeaderState());
    }

    private void startup() {
        lcl.startup();

        broadcast(MASTERREQ);
        try {
            DatagramPacket data = listen(startUpWaitTime);
            System.out.println("@" + getLeaderState() + " - Message: " + new String(data.getData()).trim() + " from " + data.getAddress());

            switch (new String(data.getData()).trim()) {
                case MASTERACK:
                    state = CONSISTENCY;
                    return;
                default:
                    System.out.println("@" + getLeaderState() + " - Unprocessed Message: " + new String(data.getData()));
                    break;
            }
        } catch (Exception ex) {
            //timeout
        }
        state = NOMASTER;
    }

    private void nomaster() {
        lcl.nomaster();

        try {
            DatagramPacket data = listen(waitTime);
            System.out.println("@" + getLeaderState() + " - Message: " + new String(data.getData()).trim() + " from " + data.getAddress());

            switch (new String(data.getData()).trim()) {
                case MASTERUP:
                case MASTERREQ:
                case ELECTION:
                    state = SLAVE;
                    return;
                default:
                    System.out.println("@" + getLeaderState() + " - Unprocessed Message: " + new String(data.getData()));
                    break;
            }
        } catch (Exception ex) {
            //timeout
        }

        state = MASTER;
    }

    private void candidate() {
        lcl.candidate();

        slaves = new ArrayList();
        broadcast(ELECTION);
        long timeStarted = System.currentTimeMillis();
        long timeEnded = timeStarted + keepAliveTime, currTime;

        while ((currTime = System.currentTimeMillis()) < timeEnded) {
            try {
                DatagramPacket data = listen((int) (timeEnded - currTime));
                System.out.println("@" + getLeaderState() + " - Message: " + new String(data.getData()).trim() + " from " + data.getAddress());

                switch (new String(data.getData()).trim()) {
                    case ACCEPT:
                        slaves.add(new Object[]{data.getAddress(), data.getPort()});
                        break;
                    case REFUSE:
                        state = SLAVE;
                        waitTime = 5000 + (int) (Math.random() * 1000);
                        return;
                    case ELECTION:
                        reply(data.getAddress(), data.getPort(), REFUSE);
                        break;
                    case QUIT:
                    case MASTERREQ:
                        state = SLAVE;
                        return;
                    default:
                        System.out.println("@" + getLeaderState() + " - Unprocessed Message: " + new String(data.getData()));
                        break;
                }
            } catch (Exception ex) {
                //time out
            }
        }

        state = MASTER;
    }

    private void consistency() {
        lcl.consistency();

        long timeStarted = System.currentTimeMillis(), timeEnded = timeStarted + keepAliveTime, currTime;

        while ((currTime = System.currentTimeMillis()) < timeEnded) {
            try {
                DatagramPacket data = listen((int) (timeEnded - currTime));
                System.out.println("@" + getLeaderState() + " - Message: " + new String(data.getData()).trim() + " from " + data.getAddress());

                switch (new String(data.getData()).trim()) {
                    case MASTERACK:
                        reply(data.getAddress(), data.getPort(), CONFLICT);
                        break;
                    default:
                        System.out.println("@" + getLeaderState() + " - Unprocessed Message: " + new String(data.getData()));
                        break;
                }
            } catch (Exception ex) {
                //time out
            }
        }

        state = SLAVE;
    }

    private void accept() {
        lcl.accept();

        long timeStarted = System.currentTimeMillis(), timeEnded = timeStarted + keepAliveTime, currTime;

        while ((currTime = System.currentTimeMillis()) < timeEnded) {
            try {
                DatagramPacket data = listen((int) (timeEnded - currTime));
                System.out.println("@" + getLeaderState() + " - Message: " + new String(data.getData()).trim() + " from " + data.getAddress());

                switch (new String(data.getData()).trim()) {
                    case MASTERUP:
                        reply(data.getAddress(), data.getPort(), SLAVEUP);
                        break;
                    case ELECTION:
                        reply(data.getAddress(), data.getPort(), REFUSE);
                        break;
                    default:
                        System.out.println("@" + getLeaderState() + " - Unprocessed Message: " + new String(data.getData()));
                        break;
                }
            } catch (Exception ex) {
                //time out
            }
        }

        state = SLAVE;
    }

    private void slave() {
        lcl.slave();

        while (state == SLAVE) {
            try {
                DatagramPacket data = listen(waitTime);
                System.out.println("@" + getLeaderState() + " - Message: " + new String(data.getData()).trim() + " from " + data.getAddress());

                switch (new String(data.getData()).trim()) {
                    case MASTERUP:
                        // master alive, everything is fine
                        reply(data.getAddress(), data.getPort(), SLAVEUP);
                        if (!data.getAddress().equals(master)) {
                            master = data.getAddress();
                            lcl.setMaster(master);
                        }
                        break;
                    case ELECTION:
                        // new master
                        reply(data.getAddress(), data.getPort(), ACCEPT);
                        state = ACCEPTs;
                        return;
                    default:
                        System.out.println("@" + getLeaderState() + " - Unprocessed Message: " + new String(data.getData()));
                        break;
                }
            } catch (Exception ex) {
                //timeout exception!
                state = CANDIDATE;
            }
        }
    }
}
