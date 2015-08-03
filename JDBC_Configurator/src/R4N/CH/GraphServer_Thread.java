/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.CH;

import static R4N.CH.GraphServer.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author bluemoon
 */

/*
 contacto
 - CH vai repetindo pedidos a uma dada address
 - atraves de DNS, essa address vai apontar para o master (eventualmente)
 - o CH vai receber um orderNumber e, qd o tiver, usa-o sempre q fizer pedidos
 - CH vai repetindo pedidos ate vir true ou false
 DONE
 //- o true é enviado assim que o pedido é propagado pa todos os slaves
 //- o false é qd deadlock e detetado
 //- se o true foi enviado, o lock é feito e o cliente bloqueia ou n

 add ad-hoc
 - ?


 crash do slave
 - who cares? master reconhece e para d o ter na sua lista
 DONE

 crash do master
 - implica nova escolha de master
 DONE

 escolha do master
 - ? processo
 - todos os recursos pedidos levam o semaforo down (ja foram pedidos e sabemos q n ha deadlocks)
 - os clientes agr voltam a ligar-se e mostram o numero ao qual foram atribuidos
 - tem d haver waits para os processos sem numero, para q novos processos n façam lock as merdas
 - os clientes com numero vao fazendo os locks nos mms URIs por ordem de numero atribuido
 - se o numero for o numero mais baixo dos pedidos de um dado recurso, o lock nao é feito (sem ja foi locked); else, faz lock e bloqueia
 - todos os pedidos sao forwarded (pq nem todos os slaves podem saber o q s passa)
 - os processos sem numero podem começar a ser atendidos

 sincronizaçao
 - eventualmente sabemos s e adicionada nova linha a grafo ou n
 - se for, antes d lock, synch{escolhemos numero, enviamos numero ao cliente, propagamos a decisao}
 - os outros guardam a decisao e o numero incremental
 - se o numero enviado for abaixo do numero atual (pq o master crashou), ent ignore (n adicionar decisoes duplicadas)
 */
public class GraphServer_Thread extends Thread {

    PrintWriter out;
    BufferedReader in;
    Socket s;
    GraphClient g;

    public GraphServer_Thread(Socket s, GraphClient p) throws IOException {
        System.out.println("Creating thread " + this.getId());
        this.s = s;
        out = new PrintWriter(s.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        g = p;
    }

    @Override
    public void run() {
        while (!s.isClosed()) {
            try {
                String reply = "ok", arg1, arg2;
                String command = in.readLine();
                if (command == null) {
                    break;
                }
                //System.out.println("Thread " + this.getId() + " got " + getOp(command.charAt(0)) + " : " + command);
                switch (command.charAt(0)) {
                    case addNode:
                        arg1 = in.readLine();
                        arg2 = in.readLine();
                        g.addNode(arg1, arg2);
                        break;
                    case addConnection:
                        arg1 = in.readLine();
                        arg2 = in.readLine();
                        g.addConnection(arg1, arg2);
                        break;
                    case removeConnection:
                        arg1 = in.readLine();
                        arg2 = in.readLine();
                        g.removeConnection(arg1, arg2);
                        break;
                    case removeConnectionFromClient:
                        arg1 = in.readLine();
                        g.removeConnectionFromClient(arg1);
                        break;
                    case checkForCycles:
                        arg1 = in.readLine();
                        arg2 = in.readLine();
                        reply = "" + g.checkForCycles(arg1, arg2);
                        break;
                    case getWriteLock:
                        arg1 = in.readLine();
                        arg2 = in.readLine();
                        //System.out.println("About to enter synched zone");
                        reply = "" + g.getWriteLock(arg1, arg2);
                        break;
                    case dropWriteLock:
                        arg1 = in.readLine();
                        arg2 = in.readLine();
                        g.dropWriteLock(arg1, arg2);
                        break;
                    case getGraph:
                        g.getGraph(out, in);
                        break;
                    case endThread:
                        return;
                    default:
                        System.out.println("Unknown command");
                        break;
                }

                out.println(reply);
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    s.close();
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                }
            }
        }

    }
}
