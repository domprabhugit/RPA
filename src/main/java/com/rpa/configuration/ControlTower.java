/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.configuration;

import com.rpa.util.UtilityFile;

import io.hawt.embedded.Main;

public class ControlTower {

	public void configureHawtio() throws Exception {
		Main hawtioMain=new Main(); 
		String hawtioWar = UtilityFile.getCodeBasePath() + "/rpa_control_tower/hawtio.war";
		hawtioMain.setPort(8181);
		hawtioMain.setWar(hawtioWar); 
		hawtioMain.run();
	}
}