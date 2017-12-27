import json
import RPi.GPIO as GPIO
import time

from conf import configuration
from python_common_tools.tools import get_send_rpi_stats
from python_common_tools.tools import send_event_to_st
from python_common_tools.tools import get_st_ip


def main():
    ST_IP = get_st_ip()

    if not ST_IP:
        print "Smartthings Hub is not UP"
        exit(1)
    configuration["ST_IP"] = ST_IP
    with open('conf.py', 'w') as f:
        f.write("configuration = {}".format(configuration))
    time.sleep(1)

    GPIO.setmode(GPIO.BCM)
    #channels = [12, 16, 20, 21]  # pins 32, 36, 38, 40
    #channel_names = ['zone01', 'zone02', 'zone03', 'zone04']
    channels = [12, 16, 21]  # pins 32, 36, 40
    channel_names = ['zone01', 'zone02', 'zone04']

    global channel_def
    channel_def = dict(zip(channels, channel_names))

    global status_names
    status_names = ['close', 'open']  # 0-> close; 1-> open

    GPIO.setup(channels, GPIO.IN, GPIO.PUD_UP)
    for pin in channels:
        GPIO.add_event_detect(pin, GPIO.BOTH, callback=my_callback, bouncetime=300)

    global alarm
    alarm = {}
    for pin in channels:
        alarm.update({channel_def.get(pin): status_names[GPIO.input(pin)]})

    print "Initial State: {}".format(alarm)
    for key, value in alarm.iteritems():
        send_event_to_st(json.dumps({
            'type': 'alarm_status',
            'body': {
                'sensor_name': key,
                'sensor_status': value
            }
        }), configuration["ST_IP"])
        time.sleep(1)

    try:
        while True:
            event = get_send_rpi_stats()
            send_event_to_st(json.dumps(event), configuration["ST_IP"])
            time.sleep(configuration["SLEEP_TIME"])

    except (Exception, KeyboardInterrupt, SystemExit):
        print "\nEnding Loop"
        print "Cleaning GPIO"
        GPIO.cleanup()           # clean up GPIO on normal exit


def my_callback(pin):
    pin_status = GPIO.input(pin)
    sensor_name = channel_def.get(pin)
    sensor_status = alarm.get(sensor_name)
    new_sensor_status = status_names[pin_status]

    #if sensor_name != 'motion_sensor':
    if new_sensor_status != sensor_status:
        notify_hub(sensor_name, new_sensor_status)


def notify_hub(sensor_name, sensor_status):
    alarm[sensor_name] = sensor_status
    send_event_to_st(json.dumps({
        'type': 'alarm_status',
        'body': {
            'sensor_name': sensor_name,
            'sensor_status': sensor_status
        }
    }), configuration["ST_IP"])


#ef send_event(event):
#   i = 0
#   send_flag = True
#   #print "event: ", event
#
#   while send_flag and i < 5:
#       send_flag = send_evt(event)
#       if send_flag:
#           i += 1
#           print "{} send".format(i)
#
#
#ef send_evt(event):
#   error_status = False
#   url = "http://{}:{}".format(conf.ST_IP, conf.ST_PORT)
#   headers = {
#       'content-type': "application/json",
#   }
#   try:
#       r = requests.post(url, data=event, headers=headers)
#   except requests.exceptions.RequestException as e:
#       print e
#       error_status = True
#   else:
#       if r.status_code != 202:
#           print "Post Error Code: {}, Post Error Message: {}".format(r.status_code, r.text)
#           error_status = True
#   return error_status


if __name__ == "__main__":
    main()


