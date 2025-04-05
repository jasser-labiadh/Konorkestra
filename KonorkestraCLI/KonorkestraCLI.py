import argparse
import json
import requests
import os

# Function to read the configuration file
def read_config_file(file_path):
    if not os.path.exists(file_path):
        print(f"Error: The file {file_path} does not exist.")
        return None

    # Read and parse the JSON configuration file
    try:
        with open(file_path, 'r') as file:
            config_data = json.load(file)
            return config_data
    except json.JSONDecodeError as e:
        print(f"Error: Failed to parse JSON file. {e}")
        return None

# Function to upload configuration file to the server
def upload_config_file(config_data, url):
    try:
        # Send the configuration data to the server
        response = requests.post(f'{url}/upload-config', json=config_data)

        if response.status_code == 200:
            print("Configurations uploaded successfully.")
        else:
            print(f"Failed to upload configurations: {response.status_code} {response.text}")
    except requests.exceptions.RequestException as e:
        print(f"Error: Failed to send request to the server. {e}")

# Main function for the CLI
def main():
    # Set up the argument parser
    parser = argparse.ArgumentParser(description="Admin CLI for Konorkestra")
    parser.add_argument('--upload-configs', type=str, help="Path to the configuration file to upload.")
    parser.add_argument('--url', type=str, required=True, help="URL of the server (e.g., http://localhost:5000).")

    args = parser.parse_args()

    # Ensure the user provided the config file path
    if not args.upload_configs:
        print("Error: You must provide a configuration file path using '--upload-configs'.")
        return

    # Read the configuration file
    config_data = read_config_file(args.upload_configs)
    if config_data is None:
        return  # If there was an error reading the file, stop further execution

    # Upload the configuration file to the server
    upload_config_file(config_data, args.url)

if __name__ == "__main__":
    main()