package com.jzkem.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class Jzkem implements IJzkem {
	
	private ActiveXComponent zkem;
	
	/**
	 * Init
	 *
	 */
	public void init() {
		this.zkem = new ActiveXComponent("zkemkeeper.ZKEM.1");
        System.out.println(this.zkem.getProgramId());
	}
	
	/**
	 * Connect
	 * If your device supports the TCP/IP communications, you can refer to this.
	 * when you are using the tcp/ip communication,you can distinguish different devices by their IP address.
	 */
	public boolean connect(String address,int port, int pin, int iMachineNumber) throws Exception{
		
		int error = 0;
		Variant idwErrorCode = new Variant(error, true);
		// Set the password(PC terminal) for communication.
		// Only PC terminal' password is the same as device terminal's,you can connect the device.
		// You can set device terminal's communication password by the function "SetDeviceCommPwd"
		if(this.zkem.invoke("SetCommPassword", pin).getBoolean()) {
			this.zkem.invoke("RefreshData", iMachineNumber);
			System.out.println("Successfully set computer's commKey!");
		} else {
			this.zkem.invoke("GetLastError", idwErrorCode);
			System.out.println("Operation failed, ErrorCode=" + idwErrorCode.getIntRef());
		}
		boolean result = this.zkem.invoke("Connect_NET", address, port).getBoolean();
		
		if(result){
			try {
				System.out.println("Current State: Connected");
				this.zkem.invoke("RegEvent", iMachineNumber, 65535); //Here you can register the realtime events that you want to be triggered(the parameters 65535 means registering all)
			} catch (Exception e) {
				throw new Exception("Unable to connect the device");
			} 
			
		} else {
			this.zkem.invoke("GetLastError", idwErrorCode);
			System.out.println("Unable to connect the device, ErrorCode=" + idwErrorCode.getIntRef());
		}
		return result;
	}
	
	/**
	 * Disconnect
	 *
	 */
	public void disconnect(){
		this.zkem.invoke("Disconnect");
		System.out.println("Current State: Disconnected");
	}
	
	/**
	 * GetGeneralLogData
	 * Download the attendance records from the device(For both Black&White and TFT screen devices).
	 */
	public List<Map<String, Object>> getGeneralLogData(int iMachineNumber){
		
		Variant sdwEnrollNumber = new Variant("", true);
//        Variant idwTMachineNumber = new Variant(0, true);
//        Variant idwEMachineNumber = new Variant(0, true);
        Variant idwVerifyMode = new Variant(0, true);
        Variant idwInOutMode = new Variant(0, true);
        Variant idwYear = new Variant(0, true);
        Variant idwMonth = new Variant(0, true);
        Variant idwDay = new Variant(0, true);
        Variant idwHour = new Variant(0, true);
        Variant idwMinute = new Variant(0, true);
        Variant idwSecond = new Variant(0, true);
        Variant idwWorkcode = new Variant(0, true);
        
        Variant idwErrorCode = new Variant(0, true);
        
        List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
        
        Dispatch.call(this.zkem, "EnableDevice", iMachineNumber, false); //disable the device
        boolean result = this.zkem.invoke("ReadGeneralLogData", iMachineNumber).getBoolean(); //read all the attendance records to the memory
        if(result) {
        	while(Dispatch.call(this.zkem, "SSR_GetGeneralLogData", iMachineNumber, sdwEnrollNumber, idwVerifyMode, idwInOutMode, idwYear, idwMonth, idwDay, idwHour, idwMinute, idwSecond, idwWorkcode).getBoolean()) { //get records from the memory
//        		String enrollNumber = sdwEnrollNumber.getStringRef();
        		Map<String,Object> item = new HashMap<String, Object>();
        		item.put("EnrollNumber", sdwEnrollNumber.getStringRef());
        		item.put("DateTime", idwYear.getIntRef() + "-" + idwMonth.getIntRef() + "-" + idwDay.getIntRef() + " " + idwHour.getIntRef() + ":" + idwMinute.getIntRef() + ":" + idwSecond.getIntRef());
        		item.put("VerifyMode", idwVerifyMode.getIntRef());
        		item.put("InOutMode", idwInOutMode.getIntRef());
				items.add(item);
        	}
        } else {
        	this.zkem.invoke("GetLastError", idwErrorCode);
        	if(idwErrorCode.getIntRef() != 0) {
        		System.out.println("Reading data from terminal failed, ErrorCode: " + idwErrorCode.getIntRef());
        	} else {
        		System.out.println("No data from terminal returns!");
        	}
        }
        Dispatch.call(this.zkem, "EnableDevice", iMachineNumber, true); //enable the device
        return items;
	}

	/**
	 * DownloadTmp
	 * Download user's 9.0 or 10.0 arithmetic fingerprint templates(in strings)
	 * Only TFT screen devices with firmware version Ver 6.60 version later support function "GetUserTmpExStr" and "GetUserTmpEx".
	 * While you are using 9.0 fingerprint arithmetic and your device's firmware version is under ver6.60,you should use the functions "SSR_GetUserTmp" or
	 * "SSR_GetUserTmpStr" instead of "GetUserTmpExStr" or "GetUserTmpEx" in order to download the fingerprint templates.
	 */
	public List<Map<String, Object>> downloadTmp(int iMachineNumber) {
		
		Variant sdwEnrollNumber = new Variant("", true);
		Variant sName = new Variant("", true);
		Variant sPassword = new Variant("", true);
        Variant iPrivilege = new Variant(0, true);
        Variant bEnabled = new Variant(false, true);
        
        Variant sTmpData = new Variant("", true);
        Variant iTmpLength = new Variant(0, true);
        Variant iFlag = new Variant(0, true);
        
        Variant sCardnumber = new Variant("", true);
        
        List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
        
        Dispatch.call(this.zkem, "EnableDevice", iMachineNumber, false); //disable the device
        this.zkem.invoke("ReadAllUserID", iMachineNumber); //read all the user information to the memory
        this.zkem.invoke("ReadAllTemplate", iMachineNumber); //read all the users' fingerprint templates to the memory
        while(Dispatch.call(this.zkem, "SSR_GetAllUserInfo", iMachineNumber, sdwEnrollNumber, sName, sPassword, iPrivilege, bEnabled).getBoolean()) { //get all the users' information from the memory
        	Map<String,Object> item = new HashMap<String, Object>();
    		item.put("EnrollNumber", sdwEnrollNumber.getStringRef());
    		item.put("Name", sName.getStringRef());
    		item.put("Password", sPassword.getStringRef());
    		item.put("Privilege", iPrivilege.getIntRef());
    		item.put("Enabled", bEnabled.getBooleanRef());
    		if(this.zkem.invoke("GetStrCardNumber", sCardnumber).getBoolean()) {
    			item.put("CardNumber", sCardnumber.getStringRef());
    		}
    		List<Map<String,Object>> tmps = new ArrayList<Map<String,Object>>();
        	for(int idwFingerIndex=0; idwFingerIndex<10; idwFingerIndex++) {
        		if(Dispatch.call(this.zkem, "GetUserTmpExStr", iMachineNumber, sdwEnrollNumber, idwFingerIndex, iFlag, sTmpData, iTmpLength).getBoolean()) {
        			Map<String,Object> tmp = new HashMap<String,Object>();
        			tmp.put("FingerIndex", idwFingerIndex);
        			tmp.put("TmpData", sTmpData.getStringRef());
        			tmp.put("Flag", iFlag.getIntRef());
    				tmps.add(tmp);
        		}
        	}
        	item.put("Templates", tmps);
        	items.add(item);
        }
        Dispatch.call(this.zkem, "EnableDevice", iMachineNumber, true); //enable the device
		return items;
	}

}
