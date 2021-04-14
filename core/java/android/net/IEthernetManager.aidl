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

import android.net.IpConfiguration;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.IEthernetServiceListener;

/**
 * Interface that answers queries about, and allows changing
 * ethernet configuration.
 */
/** {@hide} */
interface IEthernetManager
{
    IpConfiguration getConfiguration();
    void setConfiguration(in IpConfiguration config);
    boolean isAvailable();
    void addListener(in IEthernetServiceListener listener);
    void removeListener(in IEthernetServiceListener listener);

    /* Dual Internet Changes start */
    void reconnect();
    void teardown();

    LinkProperties getEthernetLinkProperties();
    NetworkInfo getEthernetNetworkInfo();

    boolean isPluggedInEthAvailable();
    void addPluggedinEthListener(in IEthernetServiceListener listener);
    void removePluggedinEthListener(in IEthernetServiceListener listener);
    void connectPluggedinEth();
    void teardownPluggedinEth();

    LinkProperties getPluggedinLinkProperties();
    NetworkInfo getPluggedinNetworkInfo();

    IpConfiguration getPluggedInEthernetConfiguration();
    void setPluggedInEthernetConfiguration(in IpConfiguration config);
    /* Dual Internet Changes end */
}
