package org.nustaq.kluster.processes;

import com.beust.jcommander.JCommander;
import org.nustaq.kontraktor.remoting.tcp.TCPConnectable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ruedi on 17.04.16.
 */
public class StarterClient {

    public static void main(String[] args) throws IOException, InterruptedException {
        final StarterClientArgs options = new StarterClientArgs();
        final JCommander jCommander = new JCommander(options);
        jCommander.parse(args);

        if ( options.isHelp() ) {
            jCommander.usage();
            System.exit(0);
        }

        ProcessStarter starter = (ProcessStarter) new TCPConnectable(ProcessStarter.class, options.getHost(), options.getPort())
            .connect(
                (x, y) -> System.out.println("client disc " + x),
                act -> {
                    System.out.println("act " + act);
                }
            ).await();

        if (options.isResync())
            starter.resyncProcesses();

        if ( options.isList()) {
            List<ProcessInfo> pis = starter.getProcesses().await();
            System.out.println("listing "+pis.size()+" processes:");
            pis.stream()
                .sorted( (a,b) -> a.getCmdLine()[0].compareTo(b.getCmdLine()[0]))
                .forEach( pi -> System.out.println(pi));
        }
        if ( options.isListSiblings() ) {
            List<StarterDesc> await = starter.getSiblings().await();
            System.out.println("listing "+await.size()+" siblings");
            await.forEach( sd -> System.out.println(sd) );
        }
        if ( options.getKillPid() != null ) {
            System.out.println("killing "+options.getKillPid());
            Object await = starter.terminateProcess(options.getKillPid(), true, 10).await();
            System.out.println("killed "+await);
        }
        String killMatching = options.getKillMatching();
        if ( killMatching != null ) {
            boolean hasCaps = false;
            for ( int i = 0; i < killMatching.length(); i++ ) {
                if ( Character.isUpperCase(killMatching.charAt(i)) ) {
                    hasCaps = true;
                    break;
                }
            }
            System.out.println("kill matching "+ killMatching);
            List<ProcessInfo> pis = starter.getProcesses().await();
            final boolean finalHasCaps = hasCaps;
            pis.stream()
                .forEach(pi -> {
                    String[] cmdLine = pi.getCmdLine();
                    boolean match = false;
                    for (int i = 0; i < cmdLine.length; i++) {
                        if (finalHasCaps) {
                            if (cmdLine[i].indexOf(killMatching) >= 0) {
                                match = true;
                                break;
                            }
                        } else {
                            if (cmdLine[i].toLowerCase().indexOf(killMatching) >= 0) {
                                match = true;
                                break;
                            }
                        }
                    }
                    if (match||"*".equals(killMatching)) {
                        System.out.print("killing " + pi + " .. ");
                        try {
                            Object await = starter.terminateProcess(pi.getId(), true, 10).await();
                            System.out.println(await + " ");
                        } catch (Exception e) {
                            System.out.println(""+e.getMessage());
                        }
                    }
                });
        }
        String[] cmd = new String[options.getParameters().size()];
        options.getParameters().toArray(cmd);
        if ( cmd.length > 0 ) {
            System.out.println("running "+ Arrays.toString(cmd));
            ProcessInfo await = starter.startProcess(options.getId(), options.getName(), new File(".").getCanonicalPath(), new HashMap<>(), cmd).await();
//        ProcessInfo await = starter.startProcess("/tmp", Collections.emptyMap(), "gnome-weather").await();
            System.out.println("started "+await);
        }
        starter.ping().await();
//        starter.terminateProcess(await.getId(),true,15).await();
//        System.out.println("killed "+await.getId());
        System.exit(0);
    }

}