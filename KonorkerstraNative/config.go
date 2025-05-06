package KonerkestraNative

import (
	"encoding/json"
	"fmt"
	"os"
	"time"
)

type PipeRegistration struct {
	PipePath   string   `json:"pipe_path"`
	ConfigKeys []string `json:"config_keys"`
	Status     string   `json:"status"`
	Error      string   `json:"error,omitempty"`
}

func subscribe(data []byte) []byte {
	connection, err := connect()
	if err != nil {
		return []byte("")
	}
	pipePath, err := generateUniqueFilePath()
	if err != nil {
		return []byte("")
	}
	jsonData := map[string]string{}
	json.Unmarshal(data, &jsonData)
	configList := []string{}
	for k, _ := range jsonData {
		configList = append(configList, k)
	}
	var pipeRegistration *PipeRegistration = &PipeRegistration{
		PipePath:   pipePath,
		ConfigKeys: configList,
		Status:     "ok",
		Error:      "",
	}
	data, _ = json.Marshal(pipeRegistration)
	connection.Write(data)
	return data
}
func getConfig(request []byte) (result []byte) {
	// just getting configuration without subscribing to change
}
func generateUniqueFilePath() (string, error) {
	dir := "/temp/konorkstraNative/"
	pid := os.Getpid()
	timestamp := time.Now().UnixNano()
	filePath := fmt.Sprintf("%s/%d-%d", dir, pid, timestamp)
	return filePath, nil
}
