package KonorkestraCLI

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/jessevdk/go-flags"
	"io/ioutil"
	"net/http"
	"os"
)

// Function to read the configuration file
func readConfigFile(filePath string) (map[string]interface{}, error) {
	if _, err := os.Stat(filePath); os.IsNotExist(err) {
		return nil, fmt.Errorf("Error: The file %s does not exist.", filePath)
	}

	// Read and parse the JSON configuration file
	fileData, err := ioutil.ReadFile(filePath)
	if err != nil {
		return nil, fmt.Errorf("Error: Failed to read file. %s", err)
	}

	var configData map[string]interface{}
	if err := json.Unmarshal(fileData, &configData); err != nil {
		return nil, fmt.Errorf("Error: Failed to parse JSON file. %s", err)
	}

	return configData, nil
}

// Function to upload configuration file to the server
func uploadConfigFile(configData map[string]interface{}, url string) {
	// Convert configData to JSON
	configJSON, err := json.Marshal(configData)
	if err != nil {
		fmt.Printf("Error: Failed to convert config data to JSON. %s\n", err)
		return
	}

	// Send the configuration data to the server
	resp, err := http.Post(fmt.Sprintf("%s/upload-config", url), "application/json", bytes.NewBuffer(configJSON))
	if err != nil {
		fmt.Printf("Error: Failed to send request to the server. %s\n", err)
		return
	}
	defer resp.Body.Close()

	if resp.StatusCode == 200 {
		fmt.Println("Configurations uploaded successfully.")
	} else {
		fmt.Printf("Failed to upload configurations: %d %s\n", resp.StatusCode, resp.Status)
	}
}

type Args struct {
	UploadConfigs string `short:"u" long:"upload-configs" description:"Path to the configuration file to upload." required:"true"`
	URL           string `short:"r" long:"url" description:"URL of the server (e.g., http://localhost:5000)." required:"true"`
}

// Main function for the CLI
func main() {
	var args Args
	_, err := flags.Parse(&args)
	if err != nil {
		if errors.Is(err, flags.ErrHelp) {
			return
		}
		fmt.Println("Error:", err)
		return
	}

	// Read the configuration file
	configData, err := readConfigFile(args.UploadConfigs)
	if err != nil {
		fmt.Println(err)
		return
	}

	// Upload the configuration file to the server
	uploadConfigFile(configData, args.URL)
}
