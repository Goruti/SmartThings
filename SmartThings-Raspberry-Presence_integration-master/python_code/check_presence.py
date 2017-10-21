import requests
import json
import time
import os
import check_presence_conf
from datetime import datetime


def main():
    PHONES_STATUS = check_presence_conf.PHONES

    try:
        while True:
            for key, value in PHONES_STATUS.iteritems():
                count = 0
                status = get_status(value.get("ip"))
                while status != value.get("status") and count < 5:
                    status = get_status(value.get("ip"))
                    count += 1

                if count == 5:
                    print "{} - {}: {}".format(datetime.now(), key, status)
                    PHONES_STATUS[key]["status"] = status
                    #notify_hub(key, status)

            time.sleep(5)

    except (KeyboardInterrupt, SystemExit):
        print "\nEnding Loop"
    except Exception as e:
        print e


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
    return 'not present' if os.system("{} {} {}".format("ping -W 1 -w 3 -c 3 -q", device_ip, "> /dev/null 2>&1")) else 'present'


if __name__ == "__main__":
    main()


