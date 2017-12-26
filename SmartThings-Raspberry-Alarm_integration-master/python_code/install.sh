#!/bin/bash

sudo apt-get install -y python-pip
sudo pip install psutil requests

sudo sed -i -e '$i python /home/pi/git/SmartThings/SmartThings-Raspberry-Alarm_integration-master/python_code/read_gpio.py  2>&1 | logger &\n' /etc/rc.local
sudo sed -i -e '$i sleep 20; /bin/ping 192.168.1.13 > /dev/null 2>&1 &\n' /etc/rc.local

echo "Starting Read GPIO Server"
python /home/pi/git/SmartThings/SmartThings-Raspberry-Alarm_integration-master/python_code/read_gpio.py  2>&1 | logger &
echo "Starting ping to ST hub"
/bin/ping 192.168.1.13 > /dev/null 2>&1 &
