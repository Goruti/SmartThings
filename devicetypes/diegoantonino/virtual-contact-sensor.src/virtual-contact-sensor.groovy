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
  definition (name: "Virtual Contact Sensor", namespace: "DiegoAntonino", author: "Diego Antonino") {
    capability "Contact Sensor"
    capability "Sensor"
    
    command "open"
    command "close"

  }

  simulator {
    // TODO: define status and reply messages here
  }

  tiles {
		standardTile("contact", "device.contact", width: 3, height: 2) {
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00a0dc")
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13")
		}
		main "contact"
		details "contact"
	}
}

// parse events into attributes
def parse(String description) {
  log.debug "Virtual Contact Parsing '${description}'"
    // initialize to closed state
    if (description == "updated") {
      sendEvent(name: "contact", value: "closed")
    }
}

def open() {
  sendEvent(name: "contact", value: "open")
}

def close() {
  sendEvent(name: "contact", value: "closed")
}