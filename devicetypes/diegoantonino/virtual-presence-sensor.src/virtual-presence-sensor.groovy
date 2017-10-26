/**
 *  Virtual Contact Sensor
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
  definition (name: "Virtual Presence Sensor", namespace: "DiegoAntonino", author: "Diego Antonino") {
      capability "presenceSensor"
      capability "sensor"

      command "present"
      command "away"

      attribute "presence", "enum", ["present", "not present"]



  }

  simulator {
    // TODO: define status and reply messages here
  }

  tiles {
		standardTile("presence", "device.presence", width: 3, height: 2) {
			state("present", label:'${name}', canChangeIcon: false, canChangeBackground: true)
			state("not present", label:'${name}', canChangeIcon: false, canChangeBackground: true)
		}
		main "presence"
		details "presence"
	}
}

// parse events into attributes
def parse(String description) {
  log.debug "Virtual Contact Parsing '${description}'"
    // initialize to present state
    if (description == "updated") {
      sendEvent(name: "presence", value: "present")
    }
}

def present() {
  sendEvent(name: "presence", value: "present")
}

def away() {
  sendEvent(name: "presence", value: "not present")
}