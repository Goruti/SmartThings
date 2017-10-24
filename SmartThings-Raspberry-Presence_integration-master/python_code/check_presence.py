import requests
import json
import time
import os
import sys
import blescan
import bluetooth._bluetooth as bluez
import check_presence_conf
from datetime import datetime


def main():
    PHONES_STATUS = check_presence_conf.PHONES
    NUMBER_OF_CHECK = 12
    SLEEP_TIME = 5
    COUNT = 0

    dev_id = 0
    try:
        sock = bluez.hci_open_dev(dev_id)
        print "ble thread started"
    except Exception as e:
        print "error accessing bluetooth device. Error: {}".foramt(e)
        sys.exit(1)

    blescan.hci_le_set_scan_parameters(sock)
    blescan.hci_enable_le_scan(sock)

    while True:
        returnedList = blescan.parse_events(sock)
        uuids = [x.split(",")[1] for x in returnedList]

        result = set(uuids) & set([x.get('uuid') for x in PHONES_STATUS.values()])

        for key, value in PHONES_STATUS.iteritems():
            if value.get('uuid') in result:
                status = "present"

                if status != value.get('status'):
                    PHONES_STATUS[key]['status'] = status
                    PHONES_STATUS[key]['count'] = 0
                    print "{} - {} {}".format(datetime.now(), key, status)
                elif PHONES_STATUS[key]['count'] != 0:
                    PHONES_STATUS[key]['count'] = 0

            else:
                status = "not present"
                if status != value.get('status'):

                    if PHONES_STATUS[key]['count'] >= NUMBER_OF_CHECK:
                        PHONES_STATUS[key]['status'] = status
                        PHONES_STATUS[key]['count'] = 0
                        print "{} - {} {}".format(datetime.now(), key, status)
                    else:
                        PHONES_STATUS[key]['count'] += 1

            time.sleep(SLEEP_TIME)


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


if __name__ == "__main__":
    main()


