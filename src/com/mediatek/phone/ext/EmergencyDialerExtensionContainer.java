/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.phone.ext;

import android.app.Activity;
import android.util.Log;

import java.util.Iterator;
import java.util.LinkedList;

public class EmergencyDialerExtensionContainer extends EmergencyDialerExtension {

    private static final String LOG_TAG = "EmergencyDialerExtensionContainer";

    private LinkedList<EmergencyDialerExtension> mSubExtensionList;

    /**
     * @param extension
     */
    public void add(EmergencyDialerExtension extension) {
        if (null == mSubExtensionList) {
            log("create sub extension list");
            mSubExtensionList = new LinkedList<EmergencyDialerExtension>();
        }
        log("add extension, extension is " + extension);
        mSubExtensionList.add(extension);
    }

    /**
     * 
     * @param extension 
     */
    public void remove(EmergencyDialerExtension extension) {
        if (null == mSubExtensionList) {
            log("remove extension, sub extension list is null, just return");
            return;
        }
        log("remove extension, extension is " + extension);
        mSubExtensionList.remove(extension);
    }

    public void onCreate(Activity activity, IEmergencyDialer emergencyDialer) {
        if (null == mSubExtensionList) {
            log("onCreate(), sub extension list is null, just return");
            return;
        }
        log("onCreate(), activity is " + activity + ", emergencyDialer = " + emergencyDialer);
        Iterator<EmergencyDialerExtension> iterator = mSubExtensionList.iterator();
        while (iterator.hasNext()) {
            iterator.next().onCreate(activity, emergencyDialer);
        }
    }

    public boolean updateDialAndDeleteButtonStateEnabledAttr() {
        if (null == mSubExtensionList) {
            log("updateDialAndDeleteButtonStateEnabledAttr(), sub extension list is null, just return");
            return false;
        }
        log("updateDialAndDeleteButtonStateEnabledAttr()");
        Iterator<EmergencyDialerExtension> iterator = mSubExtensionList.iterator();
        while (iterator.hasNext()) {
            EmergencyDialerExtension extension = iterator.next();
            if (extension.updateDialAndDeleteButtonStateEnabledAttr()) {
                return true;
            }
        }
        return false;
    }

    public boolean placeCall(String lastNumber) {
        if (null == mSubExtensionList) {
            log("placeCall(), sub extension list is null, just return");
            return false;
        }
        log("placeCall(), lastNumber = " + lastNumber);
        Iterator<EmergencyDialerExtension> iterator = mSubExtensionList.iterator();
        while (iterator.hasNext()) {
            EmergencyDialerExtension extension = iterator.next();
            if (extension.placeCall(lastNumber)) {
                return true;
            }
        }
        return false;
    }

    public void onDestroy() {
        if (null == mSubExtensionList) {
            log("onDestroy(), sub extension list is null, just return");
            return;
        }
        log("onDestroy()");
        Iterator<EmergencyDialerExtension> iterator = mSubExtensionList.iterator();
        while (iterator.hasNext()) {
            iterator.next().onDestroy();
        }
    }

    /**
     * 
     * @param msg 
     */
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
