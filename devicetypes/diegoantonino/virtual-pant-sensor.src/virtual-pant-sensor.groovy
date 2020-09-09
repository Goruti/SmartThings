/**
 *  Virtual Plant Sensor
 *
 *  Copyright 2016 Diego Antonino
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
metadata {
  definition (name: "Virtual Pant Sensor", namespace: "DiegoAntonino", author: "Diego Antonino") {
    capability "switch"
    capability "temperatureMeasurement"

  }

  simulator {
    // TODO: define status and reply messages here
  }

  tiles {
		standardTile("switch", "device.switch", inactiveLabel: true,  width: 6, height: 4) {
          state "off", label: '${name}', action: "on", icon: "st.Electronics.electronics18", backgroundColor: "#ffffff", nextState:"turningOn"
          state "on", label: '${name}', action: "off", icon: "st.Electronics.electronics18", backgroundColor: "#00a0dc", nextState:"turningOff"
          state "turningOn", label:'Turning on', icon:"st.Electronics.electronics18", backgroundColor:"#00a0dc", nextState: "on"
    	  state "turningOff", label:'Turning off', icon:"st.Electronics.electronics18", backgroundColor:"#ffffff", nextState: "off"
        }
        valueTile("moisture", "device.temperature", decoration: "flat", width: 1, height: 1) {
		  state  "value", label:'Moisture Value\n\n${currentValue}'
	    }

		main "moisture"
		details(["moisture", "switch"])
	}
}

// parse events into attributes
def parse(String description) {
  log.debug "Virtual Switch Parsing '${description}'"
    // initialize to closed state
    if (description == "updated") {
      sendEvent(name: "switch", value: "off")
    }
}

def on() {
  sendEvent(name: "switch", value: "on")
}

def off() {
  sendEvent(name: "switch", value: "off")
}