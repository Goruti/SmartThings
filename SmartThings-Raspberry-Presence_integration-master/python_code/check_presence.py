import requests
import json
import time
import os
import sys
import blescan
import bluetooth._bluetooth as bluez
import check_presence_conf
from datetime import datetime
import threading


def main():
    PHONES_STATUS = check_presence_conf.PHONES
    NUMBER_OF_CHECK = 2
    SLEEP_TIME = 5

    dev_id = 0
    try:
        sock = bluez.hci_open_dev(dev_id)
        print "ble thread started"
    except Exception as e:
        print "error accessing bluetooth device. Error: {}".format(e)
        sys.exit(1)

    blescan.hci_le_set_scan_parameters(sock)
    blescan.hci_enable_le_scan(sock)

    threads = []
    try:
        for key, info in PHONES_STATUS:
            t = threading.Thread(target=worker, args=(key, info, sock))
            threads.append(t)
            t.start()
    except Exception as e:
        print e


def worker(key, info, sock):

    while True:
        returnedList = blescan.parse_events(sock, 10)


        for beacon in returnedList:
            if beacon.split(",")[1] in PHONES_STATUS.keys():
                print PHONES_STATUS[beacon[1]].get("name")



def notify_hub(name, status):
    send_event(json.dumps({
        'person': name,
        'status': status}))


def send_event(event):
    i = 0
    send_flag = True

    while send_flag and i < 5:
        send_flag = send_evt(event)
        if send_flag:
            i += 1
            print "{} send".format(i)
    
            
def send_evt(event):
    error_status = False
    url = "{}{}{}".format("http://", check_presence_conf.ST_IP, ":39500")
    headers = {
        'content-type': "application/json",
    }
    try:
        r = requests.post(url, data=event, headers=headers)
    except requests.exceptions.RequestException as e:
        print e
        error_status = True
    else:
        if r.status_code != 202:
            print "Post Error Code: {}, Post Error Message: {}".format(r.status_code, r.text)
            error_status = True
    return error_status


def get_status(device_ip):
    return 'not present' if os.system("{} {} {}".format("ping -W 1 -w 2 -c 2 -q", device_ip, "> /dev/null 2>&1")) else 'present'


if __name__ == "__main__":
    main()


