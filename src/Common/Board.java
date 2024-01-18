package Common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Board {

    public static final String SHIP = "O";

    private String[][] board;

    public Board() {
        this.board = new String[4][4];
        createBoard(board);
    }

    public void createBoard(String[][] board) {
        for (String[] strings : board) {
            Arrays.fill(strings, "~");
        }
    }

    public void createEnemyBoard(String[][] board) {
        for (String[] strings : board) {
            Arrays.fill(strings, "▓");
        }
    }

    public static void viewBoards(String[][] board1, String[][] board2, String player1, String player2) {
        System.out.println("   " + player1 + "      " + player2);
        System.out.print("  ");
        for (int c = 1; c < board1[0].length + 1; c++) {
            System.out.print("  " + c);
        }
        System.out.print("        ");
        for (int c = 1; c < board1[0].length + 1; c++) {
            System.out.print("  " + c);
        }

        System.out.println();
        char letter = 'A';
        for (int i = 0; i < board1.length; i++) {
            System.out.print(letter + "|");
            for (int z = 0; z < board1[i].length; z++) {
                System.out.print("  " + board1[i][z]);
            }
            System.out.print("      ");
            System.out.print(letter + "|");
            for (int z = 0; z < board2[i].length; z++) {
                System.out.print("  " + board2[i][z]);
            }
            System.out.println();
            letter++;
        }
    }

    public void placeRandomShips(String[][] board, int numberOfShips, int shipLength) {
        for (int i = 0; i < numberOfShips; i++) {
            placeRandomShip(board, shipLength);
        }
    }

    private void placeRandomShip(String[][] board, int length) {
        Random random = new Random();
        int orientation = random.nextBoolean() ? 0 : 1;
        int row, col;

        if (orientation == 0) {
            row = random.nextInt(board.length);
            col = random.nextInt(board[0].length - length + 1);
        } else {
            row = random.nextInt(board.length - length + 1);
            col = random.nextInt(board[0].length);
        }
        if (canPlaceShip(board, row, col, length, orientation)) {
            createShip(board, row, col, length, orientation);
        } else {
            placeRandomShip(board, length);
        }
    }

    private boolean canPlaceShip(String[][] board, int row, int col, int length, int orientation) {
        if (orientation == 0) {
            for (int i = col; i < col + length; i++) {
                if (board[row][i].equals(SHIP)) {
                    return false;
                }
            }
        } else {
            for (int i = row; i < row + length; i++) {
                if (board[i][col].equals(SHIP)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void createShip(String[][] board, int row, int col, int length, int orientation) {
        if (orientation == 0) {
            for (int i = col; i < col + length; i++) {
                board[row][i] = SHIP;
            }
        } else {
            for (int i = row; i < row + length; i++) {
                board[i][col] = SHIP;
            }
        }
    }

    public static void viewAttackResult(String[][] board, String result, String coordinate) {
        char letter = coordinate.charAt(0);
        int row = letter - 'A';
        int col = Integer.parseInt(coordinate.substring(1)) - 1;

        if (row >= 0 && row < board.length && col >= 0 && col < board[0].length) {
            if (result.equals("water")) {
                board[row][col] = "~";
            } else {
                board[row][col] = "X";
            }
        } else {
            System.out.println("Coordinates out of range. Play again with correct coordinates.");
        }
    }

    public static String viewEnemyAttack(String[][] board, String coordinate, DataOutputStream dataOutputStream) throws IOException {
        System.out.println("You are being attacked by the enemy");

        char letter = coordinate.charAt(0);
        int posY = letter - 'A';
        int posX = Integer.parseInt(coordinate.substring(1));
        System.out.println("Attack received at: " + letter + (posX + 1));

        if (posY >= 0 && posY < board.length && posX >= 0 && posX < board[0].length) {
            if (board[posY][posX].equals("~")) {
                System.out.println("The enemy has hit water");
                board[posY][posX] = "~";
                return "water";
            } else if (board[posY][posX].equals("O")) {
                System.out.println("The enemy has hit your ship");
                board[posY][posX] = "X";
                return "ship";
            } else if (board[posY][posX].equals("X")) {
                System.out.println("The enemy has hit your ship again!");
                return "ship";
            } else {
                System.out.println("Unknown result");
                return "unknown";
            }
        } else {
            System.out.println("Coordinates out of range");
        }
        return coordinate;
    }

    public static void checkVictory(String[][] board, DataOutputStream dataOutputStream) throws IOException {
        int shipsFound = 0;

        for (int i = 0; i < board.length; i++) {
            for (int z = 0; z < board[i].length; z++) {
                if (board[i][z].equals("X")) {
                    shipsFound++;
                }
            }
        }

        if (shipsFound == 5) {
            System.out.println("You have won");
            dataOutputStream.writeUTF("You have lost");
            System.exit(0);
        } else {
            dataOutputStream.writeUTF("Continue the game");
        }
    }
}