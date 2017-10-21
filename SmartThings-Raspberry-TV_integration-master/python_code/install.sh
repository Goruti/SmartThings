#!/bin/bash

sudo apt-get install -y cec-utils
sudo pip2 install flask_httpauth

sudo sed -i -e '$i python /home/pi/git/SmartThings/SmartThings-Raspberry-TV_integration-master/python_code/flask_restful_server.py  2>&1 | logger &\n' /etc/rc.local
sudo sed -i -e '$i sleep 20; /bin/ping 192.168.1.252 > /dev/null 2>&1 &\n' /etc/rc.local

echo "Please set you passwords in \
'/home/pi/git/SmartThings/SmartThings-Raspberry-TV_integration-master/python_code/conf.py' file"

echo "Starting Flask Server"
python /home/pi/git/SmartThings/SmartThings-Raspberry-TV_integration-master/python_code/flask_restful_server.py  2>&1 | logger &
echo "Starting ping to ST hub"
/bin/ping 192.168.1.252 > /dev/null 2>&1 &


