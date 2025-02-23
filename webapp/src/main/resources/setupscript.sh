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

# Update and upgrade package lists
log "Updating and upgrading package lists..."
apt-get update && apt-get upgrade -y
status "Package update and upgrade"

# Check and install necessary packages
log "Checking and installing necessary packages..."
installIfReq unzip openjdk-21-jdk mysql-server maven wget



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

# Create environment variable file
log "Creating environment variable file..."
cat > /etc/environment.d/csye6225.conf <<EOF
database=jdbc:mysql://localhost:3306/csye6225
username=$2
password=$3
EOF
status "Environment variable file creation"

# Move JAR file to application directory
if [ -z "$1" ]; then
    log "ERROR: Please provide the path to the application JAR file"
    exit 1
fi
log "Moving application JAR file..."
mv "$1" $APP_DIR/application.jar
status "Application JAR file moved"

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

# Create systemd service file for Spring Boot application
log "Creating systemd service file for Spring Boot application..."
cat > /etc/systemd/system/csye6225.service <<EOF
[Unit]
Description=Spring Boot Application
After=network.target

[Service]
User=$APP_USER
WorkingDirectory=$APP_DIR
ExecStart=/usr/bin/java -jar $APP_DIR/application.jar
EnvironmentFile=/etc/environment.d/csye6225.conf
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
status "Spring Boot systemd service file created"

# Reload systemd and start the Spring Boot application
systemctl daemon-reload
systemctl start csye6225
systemctl enable csye6225
status "Spring Boot application setup completed"

log "Setup completed successfully!"