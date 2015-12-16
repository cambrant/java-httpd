import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;

public class HTTPd {
    public HTTPd()
    {
        try {
            System.out.println("Starting server...");

            ServerSocket serverSocket = new ServerSocket(9000);

			boolean running = true;
            while(running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected!");

                ClientThread cThread = new ClientThread(clientSocket);
                cThread.start();
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Couldn't bind address.");
        }
    }

    class ClientThread extends Thread
    {
        Socket clientSocket;
		boolean runThread = true;

        public ClientThread()
        {
            super();
        }

        ClientThread(Socket s)
        {
            clientSocket = s;
        }

        public void start()
        {
			BufferedReader in = null;
			PrintWriter out = null;
			String url = "";
			boolean found = false;

			try {
				// Open Readers and Writers to the client socket.
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

				while(runThread) {
					String clientCommand = in.readLine();
					String fileName = parseFilenameFromURL(clientCommand);

					System.out.println("Client command: " + clientCommand);
					System.out.println("File name: " + fileName);

					String fileContents = readFile(fileName);
					if(! fileContents.equals("404")) {
						found = true;
						out.print("HTTP/1.0 200 OK\r\n");
					} else {
						out.print("HTTP/1.0 404 Not Found\r\n");
					}

					out.print("Server: java-httpd 0.01\r\n");
					out.print("Content-Type: text/html; charset=UTF-8\r\n");
					out.print("\r\n");
					out.flush();

					if(found) {
						out.print(fileContents);
					} else {
						out.print("404 Not Found\r\n");
					}

					out.flush();
					out.close();

					runThread = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

		// Takes a request string and returns the requested file name.
		private String parseFilenameFromURL(String getRequest)
		{
			String fileName = getRequest.split(" ")[1];

            // Strip the first / in the GET path if it exists.
			if(fileName.charAt(0) == '/') {
				fileName = fileName.substring(1);
			}

            // If no specific file is requested, serve index.html
			if(fileName.equals("")) {
				fileName = "index.html";
			}

			return fileName;
		}

		// Reads the requested file from disk.
		private String readFile(String fileName)
		{
			StringBuilder contents = new StringBuilder();
			BufferedReader input;

			try {
				FileReader fileReader = new FileReader(fileName);
				input = new BufferedReader(fileReader);
			} catch (FileNotFoundException e) {
				// e.printStackTrace();
				System.out.println("404 Not Found: " + fileName);
				return "404";
			}

			String line = null;
			try {
				while((line = input.readLine()) != null) {
					contents.append(line);
					contents.append("\r\n");
				}

				input.close();
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}

			return contents.toString();
		}
    }

    public static void main(String[] args)
	{
        new HTTPd();
    }
}
