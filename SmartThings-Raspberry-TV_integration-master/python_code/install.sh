#!/bin/bash

sudo apt-get install -y cec-utils python-pip nmap
sudo pip2 install flask_httpauth python-nmap
sudo pip install netifaces

echo "GETTING ST IP"
net=`ip addr show | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*/[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*/[0-9]*' | grep -v '127.0.0.1'`
ST_IP=`sudo nmap -n $net -p39500 --open | grep  -Eo '([0-9]*\.){3}[0-9]*'`

if [ "$ST_IP" ]; then
    sudo sed -i -e '$i echo "Starting ping to ST hub"' /etc/rc.local
    sudo sed -i -e "\$i sleep 20; /bin/ping $ST_IP > /dev/null 2>&1 &\n" /etc/rc.local
    echo "Starting ping to ST hub at $ST_IP"
    /bin/ping $ST_IP > /dev/null 2>&1 &
else
    echo "Smartthings Hub is DOWN"
fi

sudo sed -i -e '$i echo "Starting Flask Server"' /etc/rc.local
sudo sed -i -e '$i python /home/pi/git/SmartThings/SmartThings-Raspberry-TV_integration-master/python_code/flask_restful_server.py  2>&1 | logger &\n' /etc/rc.local

echo "*********************************************************"
echo "Please set you passwords in \
'/home/pi/git/SmartThings/SmartThings-Raspberry-TV_integration-master/python_code/conf.py' file"
echo "*********************************************************"

echo "Starting Flask Server"
python /home/pi/git/SmartThings/SmartThings-Raspberry-TV_integration-master/python_code/flask_restful_server.py  2>&1 | logger &
