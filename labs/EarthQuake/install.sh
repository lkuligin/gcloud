sudo apt-get update
sudo apt-get --yes install python-pip python-dev build-essential 
sudo apt-get --yes install python3-pip
sudo apt-get --yes install python3-tk
sudo apt-get --fix-missing --yes install python3-mpltoolkits.basemap
sudo apt-get build-dep --yes python3-matplotlib

sudo /usr/bin/easy_install virtualenv
mkdir py3
virtualenv --system-site-packages -p python3 py3
echo 'alias py3="source /home/lkulighin/py3/bin/activate"' >>~/.bash_profile
source ~/.bash_profile

py3
python calculate.py3
gsutil cp earthquakes.png gs://test-1113
