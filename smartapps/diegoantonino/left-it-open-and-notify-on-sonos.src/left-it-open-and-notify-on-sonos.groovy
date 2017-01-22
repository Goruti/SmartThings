/**
 *  Copyright 2015 SmartThings
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
 *  Left It Open
 *
 *  Author: SmartThings
 *  Date: 2013-05-09
 */
definition(
    name: "Left It Open and notify on SONOS",
    namespace: "DiegoAntonino",
    author: "SmartThings",
    description: "Notifies on SONOS you when you have left a door or window open longer that a specified amount of time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage%402x.png"
)

preferences {
	page(name: "mainPage", title: "Play message on your speaker when a door left open", install: true, uninstall: true)
	page(name: "chooseTrack", title: "Select a song or station")
}

def mainPage() {
    dynamicPage(name: "mainPage") {
        section("Play message when") {
            input "contact", "capability.contactSensor", multiple: false
        }
        section("And notify me if it's open for more than this many minutes (default 5)") {
            input "openThreshold", "number", description: "Number of minutes", required: false
        }
        section("Delay between notifications (default 5 minutes") {
            input "frequency", "number", description: "Number of minutes", required: false
        }
        section {
            input "sonos", "capability.musicPlayer", title: "On this Speaker player", required: true
        }
        section("More options", hideable: true, hidden: true) {
            input "resumePlaying", "bool", title: "Resume currently playing music after notification", required: false, defaultValue: true
            href "chooseTrack", title: "Or play this music or radio station", description: song ? state.selectedSong?.station : "Tap to set", state: song ? "complete" : "incomplete"
            input "volume", "number", title: "Temporarily change volume", description: "0-100%", required: false
        }
        section() {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)", required: false
        }
    }
}

def chooseTrack() {
    dynamicPage(name: "chooseTrack") {
        section{
            input "song","enum",title:"Play this track", required:true, multiple: false, options: songOptions()
        }
    }
}


def installed() {
    log.debug "Installed with settings: ${settings}"
	subscribe()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe()
}

def subscribe() {
    subscribe(app, appTouchHandler)
	subscribe(contact, "contact.open", doorOpen)

    if (song) {
        saveSelectedSong()
    }

}

def appTouchHandler(evt){

    log.debug "evt = ${evt}"
    msg = "Test message for Sonos"
    log.debug "msg = ${msg}"
    state.sound = textToSpeech(msg)
    sonos.playTrack(state.sound.uri)

}

def doorOpen(evt)
{
	log.trace "doorOpen($evt.name: $evt.value)"
	def delay = (openThreshold != null && openThreshold != "") ? openThreshold * 60 : 300
	runIn(delay, doorOpenTooLong)
}


def doorOpenTooLong() {
	def contactState = contact.currentState("contact")

    def freq = (frequency != null && frequency != "") ? frequency * 60 : 300

	if (contactState.value == "open") {
		def elapsed = now() - contactState.rawDateCreated.time
		def threshold = ((openThreshold != null && openThreshold != "") ? openThreshold * 60000 : 30000) - 1000
		if (elapsed >= threshold) {
			log.debug "Contact has stayed open long enough since last check ($elapsed ms):  calling sendMessage()"
            sendMessage()
            runIn(freq, doorOpenTooLong, [overwrite: false])
		} else {
			log.debug "Contact has not stayed open long enough since last check ($elapsed ms):  doing nothing"
		}
	} else {
		log.warn "doorOpenTooLong() called but contact is closed:  doing nothing"
	}
}

void sendMessage()
{
    loadText()

    if (song) {
        sonos.playSoundAndTrack(state.sound.uri, state.sound.duration, state.selectedSong, volume)
    }
    else if (resumePlaying){
        sonos.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
    }
    else if (volume) {
        sonos.playTrackAtVolume(state.sound.uri, volume)
    }
    else {
        sonos.playTrack(state.sound.uri)
    }

}

private loadText(){

    def minutes = (openThreshold != null && openThreshold != "") ? openThreshold : 5
    def msg = "${contact.displayName} has been left open for ${minutes} minutes."

    log.debug "msg = ${msg}"
    state.sound = textToSpeech(msg) //This generate the mp3 on \
    // StateSound = [uri:https://s3.amazonaws.com/smartapp-media/tts/0e5d6b9432d2dff1717dd1c2b6faf059f99edbad.mp3, duration:3]

}

private songOptions() {

    // Make sure current selection is in the set

    def options = new LinkedHashSet()
    if (state.selectedSong?.station) {
        options << state.selectedSong.station
    }
    else if (state.selectedSong?.description) {
        // TODO - Remove eventually? 'description' for backward compatibility
        options << state.selectedSong.description
    }

    // Query for recent tracks
    def states = sonos.statesSince("trackData", new Date(0), [max:30])
    def dataMaps = states.collect{it.jsonValue}
    options.addAll(dataMaps.collect{it.station})

    log.trace "${options.size()} songs in list"
    options.take(20) as List
}

private saveSelectedSong() {
    try {
        def thisSong = song
        log.info "Looking for $thisSong"
        def songs = sonos.statesSince("trackData", new Date(0), [max:30]).collect{it.jsonValue}
        log.info "Searching ${songs.size()} records"

        def data = songs.find {s -> s.station == thisSong}
        log.info "Found ${data?.station}"
        if (data) {
            state.selectedSong = data
            log.debug "Selected song = $state.selectedSong"
        }
        else if (song == state.selectedSong?.station) {
            log.debug "Selected existing entry '$song', which is no longer in the last 20 list"
        }
        else {
            log.warn "Selected song '$song' not found"
        }
    }
    catch (Throwable t) {
        log.error t
    }
}