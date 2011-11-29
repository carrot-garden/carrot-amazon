
sudo ls -las /root

sudo apt-get --assume-yes update
sudo apt-get --assume-yes upgrade
sudo apt-get --assume-yes install mc tar wget zip unzip

sudo mkdir --parents /opt/java32
cd /opt/java32; sudo wget  --timestamping http://download.oracle.com/otn-pub/java/jdk/7u1-b08/jdk-7u1-linux-i586.tar.gz
cd /opt/java32; sudo tar --extract --gzip --keep-newer-files --totals --file jdk-7u1-linux-i586.tar.gz

sudo update-alternatives --remove-all java
sudo update-alternatives --install /usr/bin/java java /opt/java32/jdk1.7.0_01/bin/java 10

java -version 2>&1

sudo addgroup --system karaf
sudo adduser --system --ingroup karaf --home /var/karaf karaf
sudo adduser ubuntu karaf

sudo chown --changes --recursive ubuntu:karaf /var/karaf
sudo chmod --changes --recursive o-rwx,g+rw,ugo-s /var/karaf
sudo find /var/karaf -type d -exec chmod --changes g+s {} \;
