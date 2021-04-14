/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.net;

import android.content.Context;
import android.net.IEthernetManager;
import android.net.IEthernetServiceListener;
import android.net.IpConfiguration;
import android.os.Handler;
import android.os.Message;
import android.os.Binder;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.RemoteException;
/* Dual Ethernet Changes start */
import android.os.Binder;
import android.os.IBinder;
import android.os.ServiceManager;
/* Dual Ethernet Changes end */
import android.os.RemoteException;
/* Dual Ethernet Changes start */
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.os.INetworkManagementService;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.NetworkInfo;
import android.net.InterfaceConfiguration;
import android.content.*;
import android.util.Log;

import java.util.ArrayList;

/**
 * A class representing the IP configuration of the Ethernet network.
 *
 * @hide
 */
public class EthernetManager {
    private static final String TAG = "EthernetManager";
    private static final int MSG_AVAILABILITY_CHANGED = 1000;
    /* Dual Ethernet Changes start */
    private static final int MSG_AVAILABILITY_CHANGED_ETH1 = 1001;
    /* Dual Ethernet Changes end */
    private final Context mContext;
    private final IEthernetManager mService;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_AVAILABILITY_CHANGED) {
                boolean isAvailable = (msg.arg1 == 1);
                for (Listener listener : mListeners) {
                    Log.d(TAG, "ETH0 onAvailabilityChanged isAvailable" + isAvailable);
                    listener.onAvailabilityChanged(isAvailable);
                }
            }

            /* Dual Ethernet Changes Start */
            if(msg.what == MSG_AVAILABILITY_CHANGED_ETH1) {
                boolean isAvailable = (msg.arg1 == 1);
                for (Listener listener : mEth1Listeners) {
                    Log.d(TAG, "ETH1 onAvailabilityChanged isAvailable" + isAvailable);
                    listener.onAvailabilityChanged(isAvailable);
                }
            }
            /* Dual Ethernet Changes end */
        }
    };
    private final ArrayList<Listener> mListeners = new ArrayList<Listener>();
    private final IEthernetServiceListener.Stub mServiceListener =
            new IEthernetServiceListener.Stub() {
                @Override
                public void onAvailabilityChanged(boolean isAvailable) {
                    Log.d(TAG, "ETH0 MSG_AVAILABILITY_CHANGED isAvailable" + isAvailable);
                    mHandler.obtainMessage(
                            MSG_AVAILABILITY_CHANGED, isAvailable ? 1 : 0, 0, null).sendToTarget();
                }
            };

    /* Dual Ethernet Changes start */
    private final ArrayList<Listener> mEth1Listeners = new ArrayList<Listener>();

    private final IEthernetServiceListener.Stub mEth1ServiceListener =
            new IEthernetServiceListener.Stub() {
                @Override
                public void onAvailabilityChanged(boolean isAvailable) {
                    Log.d(TAG, "ETH1 MSG_AVAILABILITY_CHANGED_ETH1 isAvailable" + isAvailable);
                    mHandler.obtainMessage(
                            MSG_AVAILABILITY_CHANGED_ETH1, isAvailable ? 1 : 0, 0, null).sendToTarget();
                }
            };
    /* Dual Ethernet Changes end */

    private ConnectivityManager mConnectivityManager;
    private INetworkManagementService mNMService;

    /**
     * A listener interface to receive notification on changes in Ethernet.
     */
    public interface Listener {
        /**
         * Called when Ethernet port's availability is changed.
         * @param isAvailable {@code true} if one or more Ethernet port exists.
         */
        public void onAvailabilityChanged(boolean isAvailable);
    }

    /**
     * Create a new EthernetManager instance.
     * Applications will almost always want to use
     * {@link android.content.Context#getSystemService Context.getSystemService()} to retrieve
     * the standard {@link android.content.Context#ETHERNET_SERVICE Context.ETHERNET_SERVICE}.
     */
    public EthernetManager(Context context, IEthernetManager service) {
        mContext = context;
        mService = service;

        mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
        mNMService = INetworkManagementService.Stub.asInterface(b);
    }

    /**
     * Get Ethernet configuration.
     * @return the Ethernet Configuration, contained in {@link IpConfiguration}.
     */
    public IpConfiguration getConfiguration() {
        try {
            return mService.getConfiguration();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Set Ethernet configuration.
     */
    public void setConfiguration(IpConfiguration config) {
        try {
            mService.setConfiguration(config);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Indicates whether the system currently has one or more
     * Ethernet interfaces.
     */
    public boolean isAvailable() {
        try {
            return mService.isAvailable();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Adds a listener.
     * @param listener A {@link Listener} to add.
     * @throws IllegalArgumentException If the listener is null.
     */
    public void addListener(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        mListeners.add(listener);
        if (mListeners.size() == 1) {
            try {
                mService.addListener(mServiceListener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    /**
     * Removes a listener.
     * @param listener A {@link Listener} to remove.
     * @throws IllegalArgumentException If the listener is null.
     */
    public void removeListener(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        mListeners.remove(listener);
        if (mListeners.isEmpty()) {
            try {
                mService.removeListener(mServiceListener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    /* Dual Ethernet Changes start */
    /**
     * Get Current EthernetInfo.
     */
    public EthernetInfo getEthernetInfo() {
        EthernetInfo mEthernetInfo = new EthernetInfo();
        NetworkInfo mNetworkInfo = getEthernetNetworkInfo();
        //mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        LinkProperties mLinkProperties = getEthernetLinkProperties();
             //mConnectivityManager.getLinkProperties(ConnectivityManager.TYPE_ETHERNET);

	if (mLinkProperties != null) {
	     String hwAddr = null;
	     String iface = mLinkProperties.getInterfaceName();

	     if (iface != null) {
		try {
		    InterfaceConfiguration config = mNMService.getInterfaceConfig(iface);
		    hwAddr = config.getHardwareAddress();

		    if (hwAddr == null) {
			Log.e(TAG, "Failed to get hardware adddress for " + iface);
		    }
		    else {
			mEthernetInfo.setHwAddress(hwAddr);
		    }
		} catch (NullPointerException | RemoteException e) {
			Log.e(TAG, "Failed to get InterfaceConfiguration");
		}
	     } else {
		     Log.e(TAG, "Failed to get iface");
	     }
	}

        IpConfiguration mIpConfig = getConfiguration();
        mEthernetInfo.setNetworkInfo(mNetworkInfo);
        mEthernetInfo.setLinkProperties(mLinkProperties);
        mEthernetInfo.setIpConfiguration(mIpConfig);

        if (isAvailable())
            mEthernetInfo.setInterfaceStatus(EthernetInfo.InterfaceStatus.ENABLED);
        else
            mEthernetInfo.setInterfaceStatus(EthernetInfo.InterfaceStatus.DISABLED);

        return mEthernetInfo;
    }

        /**
     * Get Current EthernetInfo.
     */
    public EthernetInfo getPluggedInEthernetInfo() {
        EthernetInfo mPluggedinEthernetInfo = new EthernetInfo();

        NetworkInfo mNetworkInfo = getPluggedinNetworkInfo();
        LinkProperties mLinkProperties = getPluggedinLinkProperties();
        IpConfiguration mIpConfig = getPluggedInEthernetConfiguration();

        Log.d(TAG, "mNetworkInfo: " + mNetworkInfo.toString());
        Log.d(TAG, "mLinkProperties " + mLinkProperties.toString());
        Log.d(TAG, "mIpConfig " + mIpConfig.toString());

        if (mLinkProperties != null) {
             String hwAddr = null;
             String iface = mLinkProperties.getInterfaceName();

            if (iface != null) {
                try {
                    InterfaceConfiguration config = mNMService.getInterfaceConfig(iface);
                    hwAddr = config.getHardwareAddress();
                    if (hwAddr == null) {
                        Log.e(TAG, "Failed to get hardware adddress for " + iface);
                    } else {
                        mPluggedinEthernetInfo.setHwAddress(hwAddr);
                    }
                } catch (NullPointerException | RemoteException e) {
                    Log.e(TAG, "Failed to get InterfaceConfiguration");
                }
            } else {
                Log.e(TAG, "Failed to get iface");
            }
        }

        mPluggedinEthernetInfo.setIpConfiguration(mIpConfig);
        mPluggedinEthernetInfo.setNetworkInfo(mNetworkInfo);
        mPluggedinEthernetInfo.setLinkProperties(mLinkProperties);


        if (isPluggedInEthAvailable())
            mPluggedinEthernetInfo.setInterfaceStatus(EthernetInfo.InterfaceStatus.ENABLED);
        else
            mPluggedinEthernetInfo.setInterfaceStatus(EthernetInfo.InterfaceStatus.DISABLED);

        return mPluggedinEthernetInfo;
    }

    /**
     * Reconnect ethernet
     */
    public void reconnect() {
        try {
            mService.reconnect();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to communicate with EthernetService: " +
            e.getMessage());
        }
    }

    /**
     * Teardown ethernet
     */
    public void teardown() {
        try {
            mService.teardown();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to communicate with EthernetService: " +
            e.getMessage());
        }
    }

    public LinkProperties getEthernetLinkProperties() {
        try {
            return mService.getEthernetLinkProperties();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to communicate with EthernetService: " +
            e.getMessage());
        }
        return null;
    }

    public NetworkInfo getEthernetNetworkInfo() {
        try {
            return mService.getEthernetNetworkInfo();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to communicate with EthernetService: " +
            e.getMessage());
        }
        return null;
    }

    /**
     * Adds a listener.
     * @param listener A {@link Listener} to add.
     * @throws IllegalArgumentException If the listener is null.
     */
    public void addPluggedinEthListener(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        mEth1Listeners.add(listener);
        if (mEth1Listeners.size() == 1) {
            try {
                mService.addPluggedinEthListener(mEth1ServiceListener);
            } catch (NullPointerException | RemoteException e) {
            }
        }
    }

    /**
     * Removes a listener.
     * @param listener A {@link Listener} to remove.
     * @throws IllegalArgumentException If the listener is null.
     */
    public void removePluggedinEthListener(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        mEth1Listeners.remove(listener);
        if (mEth1Listeners.isEmpty()) {
            try {
                mService.removePluggedinEthListener(mEth1ServiceListener);
            } catch (NullPointerException | RemoteException e) {
            }
        }
    }

    /**
     * Indicates whether PluggedIn Ethernet is connected.
     */
    public boolean isPluggedInEthAvailable() {
        try {
            return mService.isPluggedInEthAvailable();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Connect to PluggedIn Ethernet.interface (ETH1)
     * @throws RemoteException if service is not reachable.
     */
    public void connectPluggedinEth() {
        try {
            mService.connectPluggedinEth();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to communicate with EthernetService: " +
            e.getMessage());
        }
    }

    /**
     * Teardown PluggedIn Ethernet.interface (ETH1)
     * @throws RemoteException if service is not reachable.
     */
    public void teardownPluggedinEth() {
        try {
            mService.teardownPluggedinEth();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to communicate with EthernetService: " +
            e.getMessage());
        }
    }

    public LinkProperties getPluggedinLinkProperties() {
        try {
            return mService.getPluggedinLinkProperties();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to communicate with EthernetService: " +
            e.getMessage());
        }
        return null;
    }

    public NetworkInfo getPluggedinNetworkInfo() {
        try {
            return mService.getPluggedinNetworkInfo();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to communicate with EthernetService: " +
            e.getMessage());
        }
        return null;
    }

    /**
     * Get PluggedInEthernet configuration.
     * @return the Ethernet Configuration, contained in {@link IpConfiguration}.
     */
    public IpConfiguration getPluggedInEthernetConfiguration() {
        try {
            return mService.getPluggedInEthernetConfiguration();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Set PluggedInEthernet configuration.
     */
    public void setPluggedInEthernetConfiguration(IpConfiguration config) {
        try {
            mService.setPluggedInEthernetConfiguration(config);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
    /* Dual Ethernet Changes end */
}
