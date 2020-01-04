/**
 *  Illuminance Sensor TSL2561 connected to a PyCom
 *
 *  Copyright 2017 Diego Antonino
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */


preferences {
		input("pycom_mac", "string", title:"PyCom MAC", required: true, displayDuringSetup: true)
}

metadata {
  definition (name: "Illuminance Sensor", namespace: "DiegoAntonino", author: "Diego Antonino") {
    capability "illuminanceMeasurement"
    capability "Sensor"
    capability "Health Check"
    
	attribute "illuminance", "number"
    attribute "illuminance_ts", "string"
    attribute "check_in_at", "string"
  }

  simulator {
    // TODO: define status and reply messages here
  }

  tiles(scale: 1) {    
	
    valueTile("lux_ts", "device.illuminance_ts", decoration: "flat", width: 2, height: 1) {
		state  "value", label:'Last lux reported was at:\n\n${currentValue}'
	}
    valueTile("lux", "device.illuminance", decoration: "flat", width: 1, height: 1) {
		state  "value", label:'Lux Value\n\n${currentValue}'
	}
    standardTile("check_in_at", "device.check_in_at", inactiveLabel: true, decoration: "flat", width: 3, height: 1) {
		state  "default", label:'Last Check-in was at:\n\n${currentValue}'
	}

      main("lux")
      details(["lux_ts", "lux", "check_in_at"])
  }
}

def installed() {
	updateSettings()
}

def updated() {
	updateSettings()
}

// NOP implementation of ping as health check only calls this for tracked devices
// But as capability defines this method it's implemented to avoid MissingMethodException
def ping() {
    log.info("unexpected ping call from health check")
}

// ------------------------------------------------------------------
def updateSettings(){
    setDeviceNetworkId(pycom_mac)
    //Device send events every 5 minutes at most, this interval allows us to miss 2 events before marking offline.
    //checkInterval is in seconds
	log.debug "Configured health checkInterval: ${(5 + 5)*60} seconds"
	sendEvent(name: "checkInterval", value: (5 + 5)*60, displayed: false)

}

// MAIN FUNTON TO HANDLE MESSAGES FROM THE DEVICE
def parse(String description){
    def msg = parseLanMessage(description)
    def body = msg.json
        
    if (body) {
    	if (body.lux) {
            log.debug("illuminance: ${body.lux}")
            def received_ts = getReceivedTs()
            def evt_lux_1 = createEvent(name: "illuminance", value: "${body.lux}")
            def evt_lux_2 = createEvent(name: "illuminance_ts", value: received_ts)
            def evt_checkIn = createEvent(name: "check_in_at", value: received_ts)
            
            return [evt_lux_1, evt_lux_2, evt_checkIn]
        }
        if (body.check_in_at) {
            log.debug("check_in_at: ${body.check_in_at}")
            return createEvent(name: "check_in_at", value: "${getReceivedTs()}")
        }
    	
    }
    else {
    	log.debug("ERROR - description: ${description}")
        log.debug("ERROR - Msg: ${msg}")
    }
    
}

// ------------------------------------------------------------------
// Helper methods
// ------------------------------------------------------------------
def getReceivedTs() {
	def df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    df.setTimeZone(location.timeZone)
    
    return df.format(new Date())
}

private setDeviceNetworkId(mac){
    device.deviceNetworkId = mac
    log.debug "<Device Handler> Device Network Id set to ${mac}"
}