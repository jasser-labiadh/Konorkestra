package main

import (
	"bufio"
	"fmt"
	"log"
	"net"
	"os"
)

// FetchConfig connects to the UDS server, sends a config name, and retrieves the config data
func FetchConfig(udsPath, configName string) (string, error) {
	// Connect to the UDS server
	conn, err := net.Dial("unix", udsPath)
	if err != nil {
		return "", fmt.Errorf("failed to connect to UDS server: %v", err)
	}
	defer conn.Close()

	// Send the configuration name to the UDS server
	_, err = conn.Write([]byte(configName + "\n"))
	if err != nil {
		return "", fmt.Errorf("failed to send config name: %v", err)
	}

	// Read the response (config data) from the UDS server
	reader := bufio.NewReader(conn)
	configData, err := reader.ReadString('\n')
	if err != nil {
		return "", fmt.Errorf("failed to read config data: %v", err)
	}

	// Return the config data
	return configData, nil
}

func main() {
	// Define UDS socket path and config name
	udsPath := "/tmp/config_daemon.sock" // Example UDS socket path
	configName := "example_config_name"  // Example config name

	// Fetch the configuration from the UDS server
	configData, err := FetchConfig(udsPath, configName)
	if err != nil {
		log.Fatalf("Error: %v", err)
		os.Exit(1)
	}

	// Print the fetched configuration
	fmt.Printf("Fetched Config: %s", configData)
}
