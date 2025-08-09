#!/bin/bash

# Test script to download and start a vanilla Minecraft server
# Automatically detects Minecraft version from gradle.properties

set -e

SERVER_DIR="test-server"
SERVER_JAR="server.jar"
EULA_FILE="$SERVER_DIR/eula.txt"
PROPERTIES_FILE="$SERVER_DIR/server.properties"

echo "Setting up vanilla Minecraft server for testing..."

# Detect Minecraft version from gradle.properties
if [ ! -f "gradle.properties" ]; then
    echo "Error: gradle.properties not found. Run this script from the mod root directory."
    exit 1
fi

MC_VERSION=$(grep "^minecraft_version=" gradle.properties | cut -d'=' -f2)
if [ -z "$MC_VERSION" ]; then
    echo "Error: Could not detect Minecraft version from gradle.properties"
    exit 1
fi

echo "Detected Minecraft version: $MC_VERSION"

# Create server directory
mkdir -p "$SERVER_DIR"
cd "$SERVER_DIR"

# Download server jar if it doesn't exist
if [ ! -f "$SERVER_JAR" ]; then
    echo "Downloading Minecraft server $MC_VERSION..."
    
    # Get the version manifest
    VERSION_MANIFEST_URL="https://launchermeta.mojang.com/mc/game/version_manifest.json"
    echo "Fetching version manifest..."
    
    # Get version URL using Python for reliable JSON parsing
    VERSION_URL=$(curl -s "$VERSION_MANIFEST_URL" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    for v in data['versions']:
        if v['id'] == '$MC_VERSION':
            print(v['url'])
            break
    else:
        print('VERSION_NOT_FOUND', file=sys.stderr)
        sys.exit(1)
except Exception as e:
    print(f'JSON_PARSE_ERROR: {e}', file=sys.stderr)
    sys.exit(1)
")
    
    if [ $? -ne 0 ] || [ -z "$VERSION_URL" ]; then
        echo "Error: Could not find version $MC_VERSION in manifest"
        exit 1
    fi
    
    echo "Getting server download URL..."
    # Get the server download URL
    SERVER_URL=$(curl -s "$VERSION_URL" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    if 'downloads' in data and 'server' in data['downloads']:
        print(data['downloads']['server']['url'])
    else:
        print('SERVER_NOT_FOUND', file=sys.stderr)
        sys.exit(1)
except Exception as e:
    print(f'JSON_PARSE_ERROR: {e}', file=sys.stderr)
    sys.exit(1)
")
    
    if [ $? -ne 0 ] || [ -z "$SERVER_URL" ]; then
        echo "Error: Could not find server download URL for version $MC_VERSION"
        exit 1
    fi
    
    # Download the server jar
    echo "Downloading server jar..."
    curl -L -o "$SERVER_JAR" "$SERVER_URL"
    echo "Server jar downloaded successfully!"
else
    echo "Server jar already exists, skipping download."
fi

# Prompt user to accept EULA
if [ ! -f "eula.txt" ] || ! grep -q "eula=true" "eula.txt" 2>/dev/null; then
    echo ""
    echo "Minecraft End User License Agreement (EULA)"
    echo "============================================="
    echo ""
    echo "By running this server, you are indicating your agreement to the EULA."
    echo "You can read the full EULA at: https://minecraft.net/eula"
    echo ""
    echo "Key points:"
    echo "- This server is for testing purposes"
    echo "- You must own a legitimate copy of Minecraft"
    echo "- Commercial use requires additional licensing"
    echo ""
    
    while true; do
        read -p "Do you agree to the Minecraft EULA? (yes/no): " response
        case $response in
            [Yy]* | [Yy][Ee][Ss]* )
                echo "eula=true" > "eula.txt"
                echo "EULA accepted. Created eula.txt file."
                break
                ;;
            [Nn]* | [Nn][Oo]* )
                echo "EULA not accepted. Cannot start server without EULA agreement."
                exit 1
                ;;
            * )
                echo "Please answer yes or no."
                ;;
        esac
    done
else
    echo "EULA already accepted (eula.txt exists)."
fi

# Create basic server properties
cat > "server.properties" << EOF
# Minecraft server properties for testing
server-port=25565
level-name=world
gamemode=creative
difficulty=peaceful
spawn-protection=0
max-players=10
online-mode=false
white-list=false
motd=Test Server for Fake Sand Mod v$MC_VERSION
EOF

echo "Server properties configured."

# Start the server
echo "Starting Minecraft server v$MC_VERSION..."
echo "Press Ctrl+C to stop the server"
echo ""

java -Xmx2G -Xms1G -jar "$SERVER_JAR" nogui