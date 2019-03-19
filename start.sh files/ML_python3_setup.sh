#!/bin/bash


### We will do a setup using Anaconda and Python 3

### Update repositories
sudo apt-get -y update

## Download the Anaconda Bash Script
cd /tmp
curl -O https://repo.anaconda.com/archive/Anaconda3-5.2.0-Linux-x86_64.sh

## Verify the Data Integrity of the Installer
sha256sum Anaconda3-5.2.0-Linux-x86_64.sh

## Run the Anaconda Script
bash Anaconda3-5.2.0-Linux-x86_64.sh

## Activate Installation
source ~/.bashrc

## Test Installation
conda list

### Create Virtual Environment
read -p "Enter Name of Virtual Environment you want to create: "  virEnv
conda create -n $virEnv python=3.5 anaconda
activate $virEnv

## Install theano, mingw and libpython
conda install theano
conda install mingw libpython

## Install tensorflow and keras
read -p "Do you want to install tensorflow-gpu (y/n)? If yes, then you should have Cuda configured by following this: https://www.tensorflow.org/install/gpu" gpu
if [$gpu == "y"]; then
	pip install tensorflow-gpu
else
	pip install tensorflow
fi
pip install keras

## Update conda
conda update --all

function scikit-learn() {
  # Install scikit-learn
  sudo pip install scikit-learn

  # Check scikit-learn install
  if [ $? = 0 ]; then
    echo "Installation Successfully"
  else
    echo "Problems to install scikit-learn, check this!"
    exit 1
  fi
}

function essential(){
  ### Install essential packages
  sudo apt-get install -y python3-pip python-dev build-essential

  # Check Essential packages install
  if [ $? = 0 ]; then
    echo "Installation Successfully"
  else
    echo "Problems to install Essential packages, check this!"
    exit 1
  fi
}

function numpy() {
  # Install numpy
  sudo pip3 install numpy

  # Check numpy install
  if [ $? = 0 ]; then
    echo "Installation Successfully"
  else
    echo "Problems to install Numpy, check this!"
    exit 1
  fi
}

function scipy() {
  # Install scipy
  sudo pip3 install scipy

  # Check scipy install
  if [ $? = 0 ]; then
    echo "Installation Successfully"
  else
    echo "Problems to install Scipy, check this!"
    exit 1
  fi
}

function matplotlib() {
  # Install matplotlib
  sudo pip3 install matplotlib

  # Check matplotlib install
  if [ $? = 0 ]; then
    echo "Installation Successfully"
  else
    echo "Problems to install Matplotlib, check this!"
    exit 1
  fi
}

function jupyter() {
  # Install jupyter
  sudo pip3 install jupyter

  # Check jupyter install
  if [ $? = 0 ]; then
    echo "Installation Successfully"
  else
    echo "Problems to install jupyter, check this!"
    exit 1
  fi
}

function pandas() {
  # Install pandas
  sudo pip3 install pandas

  # Check pandas install
  if [ $? = 0 ]; then
    echo "Installation Successfully"
  else
    echo "Problems to install pandas, check this!"
    exit 1
  fi
}

function seaborn() {
  # Install seaborn
  sudo pip3 install seaborn

  # Check seaborn install
  if [ $? = 0 ]; then
    echo "Installation Successfully"
  else
    echo "Problems to install seaborn, check this!"
    exit 1
  fi
}

function scikit-learn() {
  # Install scikit-learn
  sudo pip3 install scikit-learn

  # Check scikit-learn install
  if [ $? = 0 ]; then
    echo "Installation Successfully"
  else
    echo "Problems to install scikit-learn, check this!"
    exit 1
  fi
}

case $1 in

  essential)
    essential
    ;;

  numpy)
    numpy
    ;;

  scipy)
    scipy
    ;;

  matplotlib)
    matplotlib
    ;;

  jupyter)
    jupyter
    ;;

  pandas)
    pandas
    ;;

  seaborn)
    seaborn
    ;;

  scikit-learn)
    scikit-learn
    ;;

  all)
    essential
    numpy
    scipy
    matplotlib
    jupyter
    pandas
    seaborn
    scikit-learn
    ;;

    *)
    echo -e '''
      Usage: ML_python3_setup.sh + Option
      ML_python3_setup.sh all -> this will install all packages
      You can install individually packages using: ML_python3_setup.sh pandas
      or: ML_python3_setup.sh seaborn
    '''
    ;;

esac

## References
### https://github.com/prashant2018/MLSetup/blob/master/MLSetup_python3.sh
### https://www.digitalocean.com/community/tutorials/how-to-install-anaconda-on-ubuntu-18-04-quickstart