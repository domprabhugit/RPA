package com.rpa.configuration;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.rpa.constants.RPAConstants;
import com.rpa.util.UtilityFile;

@Component
public class DefaultFolderStartup implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger logger = LoggerFactory.getLogger(DefaultFolderStartup.class.getName());

	@Override
	public void onApplicationEvent(final ApplicationReadyEvent event) {
		try {
			createDefaultFolderFor64VBUpload();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createDefaultFolderFor64VBUpload() throws Exception {

		String[] bankNames = new String[] {RPAConstants.VB64_HDFC_FOLDER_BANK_FILE, RPAConstants.VB64_HSBC_FOLDER_BANK_FILE, 
				RPAConstants.VB64_CITI_FOLDER_BANK_FILE, RPAConstants.VB64_SCB_FOLDER_BANK_FILE, RPAConstants.VB64_AXIS_FOLDER_BANK_FILE};

		String codeBasePath = UtilityFile.getCodeBasePath();

		for (String bankPath: bankNames) {           
			String directoryName = codeBasePath + UtilityFile.getUploadProperty(bankPath);
			File directory = new File(directoryName);
			if (!directory.exists()){
				logger.info("createDefaultFolderFor64VBUpload()::Folder Created::"+directoryName);
				directory.mkdirs();
			} else {
				logger.info("createDefaultFolderFor64VBUpload()::Folder Already Exists::"+directoryName);
			}
		} 
	}
}
