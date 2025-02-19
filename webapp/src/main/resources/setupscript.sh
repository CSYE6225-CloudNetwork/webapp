#!/bin/bash

# Exit on any error
set -e

# Function to log messages with timestamps
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Function to check if command executed successfully
status() {
    if [ $? -eq 0 ]; then
        log "SUCCESS: $1"
    else
        log "ERROR: $1"
        exit 1
    fi
}

# Function to check and install packages
installIfReq() {
    for pkg in "$@"; do
        if ! command -v "$pkg" &> /dev/null; then
            log "$pkg is not installed. Installing..."
            apt-get install -y "$pkg"
            status "$pkg installation"
        else
            log "$pkg is already installed."
        fi
    done
}

# Check and install necessary packages needed for spirngboot and application to run
log "Checking and installing necessary packages..."
installIfReq unzip openjdk-23-jdk mysql-server maven wget

# Update package lists
log "Update Packages"
apt-get update
status "Update package Completed"

# Upgrade packages
log "Upgrade Package"
apt-get upgrade -y
status "Upgrade Package Completed"

# Install MySQL
log "Install MySQL..."
MYSQL_ROOT_PASSWORD=$3
echo "mysql-server mysql-server/root_password password $MYSQL_ROOT_PASSWORD" | debconf-set-selections
echo "mysql-server mysql-server/root_password_again password $MYSQL_ROOT_PASSWORD" | debconf-set-selections
apt-get install -y mysql-server
status "Install MySQL Completed"

# Start MySQL service
log "Start MySQL service..."
systemctl start mysql
systemctl enable mysql
status "MySQL service start Completed"

# Configure creds for mySQL
log "Configure MySQL authentication..."
mysql -u "$2" -p"$MYSQL_ROOT_PASSWORD" <<EOF
ALTER USER '$2'@'localhost' IDENTIFIED WITH mysql_native_password BY '$2';
FLUSH PRIVILEGES;
EOF
status "MySQL authentication configuration Completed"

# Create database and user
DB_NAME="csye6225"
log "Create database"
mysql -u "$2" -p"$MYSQL_ROOT_PASSWORD" <<EOF
CREATE DATABASE IF NOT EXISTS $DB_NAME;
EOF
status "Database creation Completed"

# Create application group
APP_GROUP="csye6225"
log "Create application group: $APP_GROUP..."
groupadd -f $APP_GROUP
status "Application group creation Completed"

# Create application user
APP_USER="csye6225"
log "Create application user: $APP_USER..."
useradd -m -g $APP_GROUP $APP_USER
status "Application user creation Completed"

# Create application directory to save our application
APP_DIR="/opt/csye6225"
log "Creating application directory: $APP_DIR..."
mkdir -p $APP_DIR
status "Application directory creation Completde"

# Install Tomcat - for springboot
log "Install Tomcat..."
wget https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.16/bin/apache-tomcat-10.1.16.tar.gz
tar -xzvf apache-tomcat-10.1.16.tar.gz -C /opt/
mv /opt/apache-tomcat-10.1.16 /opt/tomcat
chown -R $APP_USER:$APP_GROUP /opt/tomcat
chmod -R u+x /opt/tomcat/bin
status "Tomcat installation Completed"

# Configure Tomcat port
log "Configuring Tomcat port..."
sed -i 's/port="8080"/port="5000"/' /opt/tomcat/conf/server.xml
status "Tomcat port configuration"

# Create environment variable file  : This will take username and pwd send while starting application : passed as args
log "Create environment variable file..."
cat > /etc/environment.d/csye6225.conf <<EOF
database=jdbc:mysql://localhost:3306/csye6225
username=$2
password=$3
EOF
status "Environment variable file creation"

# Create Tomcat service file : this will execute the tomcat services which will help to run the application
log "Creating Tomcat service file..."
cat > /etc/systemd/system/tomcat.service <<EOF
[Unit]
Description=Apache Tomcat Web Application Container
After=network.target

[Service]
Type=forking
Environment=JAVA_HOME=/usr/lib/jvm/java-23-openjdk-amd64
Environment=CATALINA_PID=/opt/tomcat/temp/tomcat.pid
Environment=CATALINA_HOME=/opt/tomcat
Environment=CATALINA_BASE=/opt/tomcat
EnvironmentFile=/etc/environment.d/csye6225.conf

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

User=$APP_USER
Group=$APP_GROUP

[Install]
WantedBy=multi-user.target
EOF
status "Tomcat service file creation"

# Reload systemd and start Tomcat
systemctl daemon-reload
systemctl start tomcat
systemctl enable tomcat
status "Tomcat service setup"

# Assuming the application zip file is provided as an argument
if [ -z "$1" ]; then
    log "ERROR: Path of application.zip file not provided"
    exit 1
fi

# Unzip application
log "Unzipping application..."
unzip -o "$1" -d $APP_DIR
status "Application unzip Completed"

# Update permissions
log "Updating permissions..."
chown -R $APP_USER:$APP_GROUP $APP_DIR
chmod -R 750 $APP_DIR
chmod 640 /etc/environment.d/csye6225.conf
status "Permission update"

# Configure MySQL to allow application connection
log "Configuring MySQL..."
cat >> /etc/mysql/mysql.conf.d/mysqld.cnf <<EOF

# Application-specific configurations
bind-address = 0.0.0.0
port = 3306
EOF

# Restart MySQL to apply changes
systemctl restart mysql
status "MySQL configuration"


# Restart Tomcat to apply all changes
log "Restarting Tomcat..."
systemctl restart tomcat
status "Tomcat restart"

# Verify Tomcat port
log "Verifying Tomcat port..."
sleep 25  # Give Tomcat time to start
if netstat -tulpn | grep :8081 > /dev/null; then
    log "SUCCESS: Tomcat is listening on port 8081"
else
    log "ERROR: Tomcat not recieving any connections on port"
    exit 1
fi

log "Setup completed successfully!"