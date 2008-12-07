package de.happtick.test;

import java.util.Date;
import java.util.List;
import java.util.Set;

import de.happtick.application.client.ApplicationTimeOut;
import de.happtick.configuration.ApplicationConfiguration;
import de.happtick.configuration.ChainConfiguration;
import de.happtick.configuration.ChainLink;
import de.happtick.configuration.EventConfiguration;
import de.happtick.information.client.HapptickInformationClient;
import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.configuration.client.ConfigurationClient;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notIOC.configuration.ConfigurationManager;

public class TestHapptick {

    public static void main(String... args) {

        try {
            // Umgebungsvariablen sind durch Server-Start-Einstellungen bekannt
            NotEOFConfiguration conf1 = new ConfigurationClient("localhost", 3000, null);
            conf1.addConfigurationFile("c:/Projekte/workspace/happtick/conf/happtick_appl.xml");

            List<String> serviceTypes = conf1.getAttributeList("serviceTypes", "simpleName");
            if (null != serviceTypes) {
                for (String type : serviceTypes) {
                    System.out.println("conf: serviceType = " + type);
                }
            }

            // Für lokale Konfiguration Umgebungsvariablen festlegen
            NotEOFConfiguration conf2 = new LocalConfiguration();
            ConfigurationManager.setInitialEnvironment("NOTEOF_HOME", "conf", "noteof_master.xml");
            conf2.addConfigurationFile("c:/Projekte/workspace/happtick/conf/happtick_appl.xml");
            System.out.println("bla... " + ConfigurationManager.getApplicationHome());
            serviceTypes = conf2.getAttributeList("serviceTypes", "simpleName");
            if (null != serviceTypes) {
                for (String type : serviceTypes) {
                    System.out.println("conf: serviceType = " + type);
                }
            }
            conf1.close();
            conf2.close();

            HapptickInformationClient hConfClient = new HapptickInformationClient("localhost", 3000, new ApplicationTimeOut());
            List<ApplicationConfiguration> list = hConfClient.getApplicationConfigurations();
            if (null != list && list.size() > 0) {
                for (ApplicationConfiguration appConf : list) {
                    System.out.println("appConf... Name: " + appConf.getNodeNameApplication());
                    System.out.println("appConf... Id:   " + appConf.getApplicationId());

                    for (Integer integer : ApplicationConfiguration.transformTimePlanSeconds(appConf.getTimePlanSeconds())) {
                        System.out.println("Sekunden... :" + integer);
                    }
                    for (Integer integer : ApplicationConfiguration.transformTimePlanMinutes(appConf.getTimePlanMinutes())) {
                        System.out.println("Minuten... :" + integer);
                    }
                    for (Integer integer : ApplicationConfiguration.transformTimePlanMonthDays(appConf.getTimePlanMonthdays())) {
                        System.out.println("MonthDays... :" + integer);
                    }
                    for (Integer integer : ApplicationConfiguration.transformTimePlanWeekDays(appConf.getTimePlanWeekdays())) {
                        System.out.println("WeekDays... :" + integer);
                    }

                    Set<String> keys = appConf.getEnvironment().keySet();
                    for (String key : keys) {
                        System.out.println("Environment ... : key = " + key + "; value = " + appConf.getEnvironment().get(key));
                    }

                }
            }

            List<ChainConfiguration> chainList = hConfClient.getChainConfigurations();
            if (null != chainList && chainList.size() > 0) {
                System.out.println("--------------------------------------");
                for (ChainConfiguration chainConf : chainList) {
                    System.out.println("chainConf... Depends: " + chainConf.isDepends());
                    System.out.println("chainConf... Loop:    " + chainConf.isLoop());
                    System.out.println("chainConf... Id:      " + chainConf.getChainId());

                    for (ChainLink link : chainConf.getChainLinkList()) {
                        System.out.println("--- chainLink... Id:     " + link.getLinkId());
                        System.out.println("--- chainLink... Type:   " + link.getAddresseeType());
                        System.out.println("--- chainLink... Skip:   " + link.isSkip());
                        System.out.println("--- chainLink... CEvent: " + link.getConditionEventId());
                        Thread.sleep(500);
                        System.out.println("--- chainLink... PEvent: " + link.getPreventEventId());
                    }
                }
            }

            List<EventConfiguration> eventList = hConfClient.getEventConfigurations();
            if (null != eventList && eventList.size() > 0) {
                System.out.println("--------------------------------------");
                System.out.println("Events: " + eventList.size());
                for (EventConfiguration conf : eventList) {
                    System.out.println("events... EventId:   " + conf.getEventId());
                    System.out.println("events... EventConf: " + conf.getEventClassName());
                }
            }

            long stop = new Date().getTime() + 60000;
            while (stop > new Date().getTime()) {
                Thread.sleep(500);
            }
            hConfClient.close();
            System.exit(0);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
