package Server;

import Common.Board;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    static Board board = new Board();
    static String[][] shipsBoard;
    static String[][] enemyBoard;

    //BufferReader is used to read the input from the console
    static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        //The port number is set to 8080
        int port = 8080;
        //The ServerSocket is initialized
        ServerSocket serverSocket = null;

        shipsBoard = new String[][]{{"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}};
        enemyBoard = new String[][]{{"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}};

        board.createBoard(shipsBoard);
        board.createEnemyBoard(enemyBoard);

        board.placeRandomShips(shipsBoard, 1, 2);
        board.placeRandomShips(shipsBoard, 1, 3);

        Board.viewBoards(shipsBoard, enemyBoard, "Your Board", "Opponent's Board");

        try {
            //The serverSocket is initialized with the port number
            serverSocket = new ServerSocket(port);
            //The serverSocket waits for a client to connect
            Socket socket = serverSocket.accept();
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            while (true) {
                opponentTurn(dataOutputStream, dataInputStream);
                playerTurn(dataOutputStream, dataInputStream);
            }
        } catch (IOException e) {
            System.out.println("ERROR IN THE CLIENT");
        }
    }

    private static void opponentTurn(DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws IOException {
        String attackX, attackY;
        boolean attackAgain;

        do {
            //The server waits for the client to send the coordinates of the attack
            String coordinate = dataInputStream.readUTF();
            coordinate = coordinate.toUpperCase();
            attackY = coordinate.substring(0, 1);
            attackX = coordinate.substring(1);

            int coordinateX = Integer.parseInt(attackX) - 1;
            attackX = Integer.toString(coordinateX);

            String hit = Board.viewEnemyAttack(shipsBoard, attackY + attackX, dataOutputStream);
            //The server sends the result of the attack to the client
            dataOutputStream.writeUTF(hit);

            Board.viewBoards(shipsBoard, enemyBoard, "Your Board", "Opponent's Board");
            //The server checks if the client has lost
            String gameResult = dataInputStream.readUTF();

            if (gameResult.equals("You have lost")) {
                System.out.println(gameResult);
                System.exit(-1);
            }

            if (hit.equals("water")) {
                attackAgain = false;
            } else {
                attackAgain = true;
                System.out.println("You have been detected");
            }
        } while (attackAgain);
    }

    private static void playerTurn(DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws IOException {
        String result;
        boolean attackAgain;

        do {
            System.out.print("Where do you want to attack (example: A3): ");
            //The server reads the coordinates of the attack from the console
            String coordinate = bufferedReader.readLine();
            coordinate = coordinate.toUpperCase();
            //The server sends the coordinates of the attack to the client
            dataOutputStream.writeUTF(coordinate);

            result = dataInputStream.readUTF();
            System.out.println("You hit at: " + result);
            Board.viewAttackResult(enemyBoard, result, coordinate);

            Board.viewBoards(shipsBoard, enemyBoard, "Your Board", "Opponent's Board");

            Board.checkVictory(enemyBoard, dataOutputStream);

            if (result.equals("water")) {
                attackAgain = false;
            } else {
                attackAgain = true;
                System.out.println("Attack again");
            }
            //The server flushes the output stream
            dataOutputStream.flush();
        } while (attackAgain);
    }
}