package com.del.delcontainer.managers;


import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.del.delcontainer.R;
import com.del.delcontainer.ui.fragments.DelAppContainerFragment;
import com.del.delcontainer.utils.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * DEL application manager responsible for app fragments
 * running inside the container
 *
 * Keep track of the following:
 *      What apps are running
 *      Which ones are hidden and which is visible
 *      Fragment to webview (app) mapping
 */
public class DelAppManager {

    private static final String TAG = "DelAppManager";
    
    // Singleton app manager object
    private static DelAppManager delAppManager = null;
    private FragmentManager fragmentManager = null;
    private HashMap<String, Fragment> appCache = new HashMap<>();

    private DelAppManager() {
        ;
    }

    public static DelAppManager getInstance() {
        if(null == delAppManager) {
            delAppManager = new DelAppManager();
        }

        return delAppManager;
    }

    /**
     * Check if the fragment manager already exists and if not,
     * set it.
     *
     * @param fragmentManager
     */
    public void setFragmentManager(FragmentManager fragmentManager) {

        if(null == this.fragmentManager) {
            this.fragmentManager = fragmentManager;
            Log.d(TAG, "setFragmentManager: set new FragmentManager in app manager");
        }
    }


    /**
     * Housekeeping methods for managing app states.
     * Launch app can be used for bringing running apps to the foreground
     * or launching a new instance of an app.
     */
    public void launchApp(UUID appId, String appName) {

        Log.d(TAG, "launchApp: Launching : " + appName);

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Create new fragment instances only if the app is being launched for the first time
        // Check in the app manager cache
        if(null == appCache.get(appName)) {

            Log.d(TAG, "launchApp: Creating new app instance : " + appName);
            // TODO: replace appName with AppId later when implemented
            DelAppContainerFragment delAppContainerFragment = new DelAppContainerFragment(appId, appName);
            appCache.put(appName, delAppContainerFragment);

            Log.d(TAG, "launchApp: Adding to transaction");
            Log.d("MainActivity", "launchApp: Del APP fragment ID : " + delAppContainerFragment.getId());
            transaction.add(R.id.nav_host_fragment, appCache.get(appName), appName); // last parameter is the app tag
        }

        if(appCache.get(appName).isHidden()) {
            Log.d(TAG, "launchApp: Showing app : " + appName);
            transaction.show(appCache.get(appName));

        } else if(appCache.get(appName).isVisible()) {
            Log.d(TAG, "launchApp: App visible : " + appName);
        }

        transaction.hide(fragmentManager.findFragmentByTag(Constants.HOST_VIEW));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    /**
     * Terminate application
     *
     * @param appId
     * @param appName
     */
    public void terminateApp(UUID appId, String appName) {

        if(null == appCache.get(appName)) {
            Log.d(TAG, "terminateApp: Invalid termination request. App doesn't exist.");
            return;
        }

        // App exists - hide, detach and remove from map
        // Has the potential to show a blank screen -> Maybe have an observer to check on open apps
        // and if closed, show the services view
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if(appCache.get(appName).isVisible()) {
            transaction.hide(appCache.get(appName));
            transaction.detach(appCache.get(appName));
        }

        transaction.remove(appCache.get(appName));
        appCache.remove(appName);
        transaction.commit();
    }

    /**
     * Hide any apps that may be visible on the interface
     *
     */
    public void hideAllApps() {

        // Check passes only when no app has been launched
        if(null == fragmentManager) {
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        for(Map.Entry<String, Fragment> app : appCache.entrySet()) {
            Log.d(TAG, "hideAllApps: Checking : " + app.getKey());
            if(app.getValue().isVisible()) {
                Log.d(TAG, "hideAllApps: Hiding : " + app.getKey());
                transaction.hide(app.getValue());
            }
        }

        Log.d(TAG, "hideAllApps: Hiding all running apps.");
        transaction.commit();
    }
}