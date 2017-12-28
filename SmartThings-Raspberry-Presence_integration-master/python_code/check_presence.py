import requests
import json
import time
import sys
import blescan
import bluetooth._bluetooth as bluez
import conf
from datetime import datetime
import threading


def main():
    PHONES_STATUS = conf.PHONES

    dev_id = 0
    try:
        sock = bluez.hci_open_dev(dev_id)
        print "{} - ble thread started".format(datetime.now())
    except Exception as e:
        print "{} - error accessing bluetooth device. Error: {}".format(datetime.now(), e)
        sys.exit(1)

    blescan.hci_le_set_scan_parameters(sock)
    blescan.hci_enable_le_scan(sock)

    threads = []
    try:
        for key, value in PHONES_STATUS.iteritems():
            t = threading.Thread(target=worker, args=(key, value, sock))
            threads.append(t)
            t.start()

    except Exception as e:
        print e


def worker(key, value, sock):
    print "Starting Thread {}".format(key)
    NUMBER_OF_CHECK = 50
    SLEEP_TIME = 1
    COUNT = 0

    while True:
        returnedList = blescan.parse_events(sock, 10)
        uuids = [x.split(",")[1] for x in returnedList]

        if value.get('uuid') in uuids:
            status = "present"

            if status != value.get('status'):
                value['status'] = status
                COUNT = 0
                print "{} - {} {}".format(datetime.now(), key, status)
                send_event(json.dumps({
                    "type": "presence_status",
                    "body": {
                        'person': key,
                        'status': status
                    }
                }))

            elif COUNT != 0:
                COUNT = 0

        else:
            status = "not present"
            if status != value.get('status'):

                if COUNT >= NUMBER_OF_CHECK:
                    value['status'] = status
                    COUNT = 0
                    print "{} - {} {}".format(datetime.now(), key, status)
                    send_event(json.dumps({
                        "type": "presence_status",
                        "body": {
                            'person': key,
                            'status': status
                        }
                    }))
                else:
                    COUNT += 1

        time.sleep(SLEEP_TIME)


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
    url = "{}{}{}".format("http://", conf.ST_IP, ":39500")
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


