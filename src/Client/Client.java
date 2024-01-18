package Client;

import Common.Board;

import java.io.*;
import java.net.Socket;

public class Client {

    static Board board = new Board();
    static String[][] shipsBoard;
    static String[][] enemyBoard;
    static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String args[]) {

        String url = "localhost";
        //The port number is set to 8080
        int port = 8080;

        shipsBoard = new String[][]{{"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}};
        enemyBoard = new String[][]{{"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}};

        board.createBoard(shipsBoard);
        board.createEnemyBoard(enemyBoard);

        board.placeRandomShips(shipsBoard, 1, 2);
        board.placeRandomShips(shipsBoard, 1, 3);

        Board.viewBoards(shipsBoard, enemyBoard, "Your board", "Opponent's board");

        try {
            //The socket is initialized with the url and the port number
            Socket socket = new Socket(url, port);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            while (true) {
                playerTurn(dataOutputStream, dataInputStream);
                opponentTurn(dataOutputStream, dataInputStream);
            }
        } catch (IOException e) {
            System.out.println("ERROR: SERVER NOT FOUND");
        }
    }

    private static void playerTurn(DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws IOException {
        String result;
        boolean attackAgain;

        do {
            System.out.print("Where do you want to attack (example: A3): ");
            //The player enters the coordinates of the attack
            String coordinate = bufferedReader.readLine();
            coordinate = coordinate.toUpperCase();
            // Adjust X coordinate to range from 1 to 4
            dataOutputStream.writeUTF(coordinate);
            // Compare this snippet from src/Client/Client.java:
            result = dataInputStream.readUTF();
            System.out.println("You hit at: " + result);
            board.viewAttackResult(enemyBoard, result, coordinate);

            board.viewBoards(shipsBoard, enemyBoard, "Your board", "Opponent's board");

            board.checkVictory(enemyBoard, dataOutputStream);

            if (result.equals("water")) {
                attackAgain = false;
            } else {
                attackAgain = true;
                System.out.println("Attack again");
            }
            dataOutputStream.flush();
        } while (attackAgain);
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

            board.viewBoards(shipsBoard, enemyBoard, "Your board", "Opponent's board");
            // Compare this snippet from src/Server/Server.java:
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
}