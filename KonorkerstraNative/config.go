package KonerkestraNative

import (
	"fmt"
	"os"
	"syscall"
)

var configToPipe = make(map[string][]PipeInfo) // Map from config key to list of pipes

type PipeInfo struct {
	PipePath string `json:"pipe_path"`
	Status   string `json:"status"`
	Error    string `json:"error,omitempty"`
}

var pipeInfo *PipeInfo

// subscribe registers a process to listen for changes on a given configuration key
func subscribe(configKey string) (string, error) {
	// Create a new pipe for the subscribing process
	if pipeInfo == nil {
		path, err := generateUniqueFilePath()
		if err != nil {
			return "", err
		}

		err = syscall.Mkfifo(path, 0666)
		if err != nil {
			return "", fmt.Errorf("failed to create named pipe: %w", err)
		}

		pipeInfo = &PipeInfo{
			PipePath: path,
			Status:   "open",
			Error:    "",
		}
	}

	// Register the pipe for the given configuration key
	configToPipe[configKey] = append(configToPipe[configKey], *pipeInfo)

	// Return the pipe path so the subscribing process can use it
	return pipeInfo.PipePath, nil
}

// handleConfigChange notifies all subscribed processes about a configuration change
func handleConfigChange(configKey string, newData []byte) {
	// Iterate over all pipes that are subscribed to this configuration
	for _, pipe := range configToPipe[configKey] {
		// Open the pipe and send the updated configuration data
		f, err := os.OpenFile(pipe.PipePath, os.O_WRONLY, os.ModeNamedPipe)
		if err != nil {
			fmt.Println("Error opening pipe:", err)
			continue
		}

		// Send the updated configuration as JSON
		_, err = f.Write(newData)
		if err != nil {
			fmt.Println("Error writing to pipe:", err)
		}
		f.Close()
	}
}

// generateUniqueFilePath generates a unique file path for the pipe based on the current process ID
func generateUniqueFilePath() (string, error) {
	dir := "/temp/konorkstraNative/"
	pid := os.Getpid()
	filePath := fmt.Sprintf("%s/%d.pid", dir, pid)
	return filePath, nil
}
