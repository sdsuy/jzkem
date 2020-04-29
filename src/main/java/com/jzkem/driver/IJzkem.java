package com.jzkem.driver;

import java.util.List;
import java.util.Map;

public interface IJzkem {

	void init();
	
	boolean connect(String address,int port, int pin, int iMachineNumber) throws Exception;
	
	void disconnect();
	
	List<Map<String, Object>> getGeneralLogData(int iMachineNumber);
	
	List<Map<String, Object>> downloadTmp(int iMachineNumber);

}
