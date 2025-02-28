package com.apkscanner.gui.action;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;

import com.android.ddmlib.IDevice;
import com.apkscanner.gui.MessageBoxPool;
import com.apkscanner.gui.easymode.contents.EasyGuiDeviceToolPanel;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.swing.ApkActionEventHandler;
import com.apkspectrum.tool.adb.PackageInfo;
import com.apkspectrum.tool.adb.PackageManager;
import com.apkspectrum.util.Log;

public class DeviceClearAppDataAction extends AbstractDeviceAction
{
	private static final long serialVersionUID = -7583791561158425804L;

	public static final String ACTION_COMMAND = "ACT_CMD_CLEAR_APP_DATA";

	public DeviceClearAppDataAction(ApkActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		IDevice device = null;
		if(e.getSource() instanceof EasyGuiDeviceToolPanel) {
			device = ((EasyGuiDeviceToolPanel) e.getSource()).getSelecteddevice();
		}

		evtClearData(getWindow(e), device);
	}

	private void evtClearData(final Window owner, final IDevice target) {
		final ApkInfo apkInfo = getApkInfo();
		if(apkInfo == null) return;

		final String packageName = apkInfo.manifest.packageName;

		Thread thread = new Thread(new Runnable() {
			public void run() {
				IDevice[] devices = null;
				if(target == null) {
					devices = getInstalledDevice(packageName);
				} else {
					if(getPackageInfo(target, packageName) != null) {
						devices =  new IDevice[] { target };
					}
				}

				if(devices == null || devices.length == 0) {
					Log.i("No such device of a package installed.");
					MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
					return;
				}

				for(IDevice device: devices) {
					Log.v("clear data on " + device.getSerialNumber());

					PackageInfo packageInfo = getPackageInfo(device, packageName);

					String errMessage = PackageManager.clearData(packageInfo);

					if(errMessage != null && !errMessage.isEmpty()) {
						final String errMsg = errMessage;
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								Log.e(errMsg);
								MessageBoxPool.show(owner, MessageBoxPool.MSG_FAILURE_CLEAR_DATA, errMsg);
							}
						});
					} else {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								MessageBoxPool.show(owner, MessageBoxPool.MSG_SUCCESS_CLEAR_DATA);
							}
						});
					}
				}
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}
}
