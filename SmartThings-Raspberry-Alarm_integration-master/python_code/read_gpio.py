import json
import RPi.GPIO as GPIO
import time

from conf import configuration
import tools


def main():
    print "GETTING Smartthings IP"
    tools.get_smartthing_ip()

    print "Going to the main process"
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
        tools.send_event_to_st(json.dumps({
            'type': 'alarm_status',
            'body': {
                'sensor_name': key,
                'sensor_status': value
            }
        }), configuration["ST_IP"])
        time.sleep(1)

    try:
        while True:
            event = tools.get_send_rpi_stats()
            tools.send_event_to_st(json.dumps(event), configuration["ST_IP"])
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
    tools.send_event_to_st(json.dumps({
        'type': 'alarm_status',
        'body': {
            'sensor_name': sensor_name,
            'sensor_status': sensor_status
        }
    }), configuration["ST_IP"])


if __name__ == "__main__":
    main()


