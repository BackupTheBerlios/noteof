package de.notEOF.core.server;

import java.util.Collection;
import java.util.Map;

import de.notEOF.core.interfaces.Service;

public class ServiceGarbage implements Runnable {

    private boolean stopped = false;
    private Server server;

    protected ServiceGarbage(Server server) {
        this.server = server;
    }

    public void stop() {
        stopped = true;
    }

    public void run() {
        while (!stopped) {
            try {
                Thread.sleep(2000);

                Map<String, Map<String, Service>> allServiceMaps = server.getAllServiceMaps();
                if (null != allServiceMaps) {
                    System.out.println("ServiceMaps: " + allServiceMaps.size());
                    Collection<Map<String, Service>> serviceMaps = allServiceMaps.values();
                    for (Map<String, Service> map : serviceMaps) {
                        Collection<Service> services = map.values();
                        if (null != services) {
                            System.out.println("Services: " + services.size());
                            for (Service service : services) {
                                if (!service.isRunning() || //
                                        (null != service.getThread() && //
                                        !service.getThread().isAlive())) {
                                    services.remove(service);
                                    break;
                                }

                            }
                        }
                    }
                }

            } catch (Exception ex) {
                // nothing to do
            }
        }
    }
}
