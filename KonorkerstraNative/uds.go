package KonerkestraNative

import (
	"fmt"
	"net"
)

var socketPath string = "/tmp/konorkestra.sock"

// connect establishes a connection to the Unix domain socket
func connect() (net.Conn, error) {
	conn, err := net.Dial("unix", socketPath)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to socket: %v", err)
	}
	return conn, nil
}

// send sends data over the established connection
func send(conn net.Conn, data []byte) error {
	// Ensure the connection is open
	if conn == nil {
		return fmt.Errorf("no connection established")
	}
	_, err := conn.Write(data)
	if err != nil {
		return fmt.Errorf("failed to send data: %v", err)
	}
	return nil
}

// receive receives data from the established connection
func receive(conn net.Conn) ([]byte, error) {
	// Ensure the connection is open
	if conn == nil {
		return nil, fmt.Errorf("no connection established")
	}

	// Create a buffer to store the incoming data
	var buffer []byte
	tmpBuffer := make([]byte, 1024) // Use a temporary buffer of a reasonable size

	// Read in chunks from the socket
	for {
		n, err := conn.Read(tmpBuffer)
		if err != nil {
			if err.Error() == "EOF" {
				// Gracefully handle EOF (end of communication)
				return buffer, nil
			}
			return nil, fmt.Errorf("failed to receive data: %v", err)
		}

		// Append the data to our buffer
		buffer = append(buffer, tmpBuffer[:n]...)

		// If we don't expect more data, break out of the loop (depending on your protocol)
		if n < len(tmpBuffer) {
			break
		}
	}

	return buffer, nil
}

// closeConnection closes the connection to the Unix domain socket
func closeConnection(conn net.Conn) error {
	// Ensure the connection is open
	if conn != nil {
		err := conn.Close()
		if err != nil {
			return fmt.Errorf("failed to close connection: %v", err)
		}
	}
	return nil
}
