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
    capability "Relative Humidity Measurement"
	capability "Switch"
    capability "Actuator"
    capability "Sensor"

  }

  simulator {
    // TODO: define status and reply messages here
  }

  tiles {
		standardTile("switch", "device.switch", width: 6, height: 4) {
          state "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff"
          state "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#FFFFFF", nextState:"turningOn", defaultState: true
          state "turningOn", label:'Turning On', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOn"
          state "turningOff", label:'Turning Off', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#FFFFFF", nextState:"turningOff"
        }
        
        valueTile("humidity", "device.humidity", decoration: "flat", width: 1, height: 1) {
		  state  "value", label:'Moisture Value\n\n${currentValue}'
	    }

		main "humidity"
		details(["humidity", "switch"])
	}
}
def installed() {
    updated()
}

def updated() {
	off()
    sendEvent(name: "humidity", value: "40", isStateChange: true)
}
// parse events into attributes
def parse(String description) {
  // This is a simulated device. No incoming data to parse.
}

def on() {
	log.debug "Executing 'on'"
    sendEvent(name: "switch", value: "on", isStateChange: true)
	// TODO: handle 'open' command
}

def off() {
	log.debug "Executing 'off'"
    sendEvent(name: "switch", value: "off", isStateChange: true)
	// TODO: handle 'close' command
}