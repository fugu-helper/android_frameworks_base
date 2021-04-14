/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.mtp;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;

import java.io.IOException;

/**
 * Invisible activity to receive intents.
 * To show Files app for the UsbManager.ACTION_USB_DEVICE_ATTACHED intent, the intent should be
 * received by activity. The activity has NoDisplay theme and immediately terminate after routing
 * intent to DocumentsUI.
 */
public class ReceiverActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String actionInten =  getIntent().getAction();
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(actionInten) || MtpDocumentsProvider.ACTION_OPEN_DP_FROM_NTF.equals(actionInten)) {
            try {
                int deviceId;
                final MtpDocumentsProvider provider = MtpDocumentsProvider.getInstance();
                if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(actionInten))
                {
                    final UsbDevice device = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    deviceId =  device.getDeviceId();
                    provider.openDevice(deviceId);
                }
                else
                {
                    deviceId =  getIntent().getIntExtra(MtpDocumentsProvider.EXTRA_USB_DEVICE_ID, 0);
                }
                final String deviceRootId = provider.getDeviceDocumentId(deviceId);
                final Uri uri = DocumentsContract.buildRootUri(
                        MtpDocumentsProvider.AUTHORITY, deviceRootId);

                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, DocumentsContract.Root.MIME_TYPE_ITEM);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                this.startActivity(intent);
            } catch (IOException exception) {
                Log.e(MtpDocumentsProvider.TAG, "Failed to open device", exception);
            }
        }
        finish();
    }
}
