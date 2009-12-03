/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.massycat.appreviewsfinder.apptree.entries;

import java.util.Comparator;

/**
 *
 * @author ben
 */
public class AppEntryNameComparator implements Comparator<AppEntry> {

    public static int compareApps(AppEntry app1, AppEntry app2) {
        return app1.mName.compareToIgnoreCase(app2.mName);
    }

    public int compare(AppEntry o1,
            AppEntry o2) {
        return compareApps(o1, o2);
    }
}
