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
		input("pycom_ip", "string", title: "PyCom IP", required: true, displayDuringSetup: true)
}

metadata {
  definition (name: "Illuminance Sensor", namespace: "DiegoAntonino", author: "Diego Antonino") {
    capability "illuminanceMeasurement"
    capability "Sensor"
    capability "Health Check"
    
	attribute "illuminance", "number"
  }

  simulator {
    // TODO: define status and reply messages here
  }

  tiles(scale: 2) {    
	valueTile("lux", "device.illuminance", decoration: "flat", width: 6, height: 1) {
		state  "value", label:'${currentValue} lux'
	}

      main('lux')
      //details("lux")
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

def parse(String description){
    def msg = parseLanMessage(description)
    def body = msg.json
        
    if (body) {
    	log.debug("${body.lux}")
        return createEvent(name: "illuminance", value: "${body.lux}")
    }
    else {
    	log.debug("ERROR - description: ${description}")
        log.debug("ERROR - Msg: ${msg}")
    }
    
}

// ------------------------------------------------------------------
// Helper methods
// ------------------------------------------------------------------

private setDeviceNetworkId(mac){
    device.deviceNetworkId = mac
    log.debug "<Device Handler> Device Network Id set to ${mac}"
}