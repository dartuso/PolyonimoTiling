/*
 * @author: Daniel Artuso
 *          dartu261@mtroyal.ca
 *
 * Comp 3649 - Assigment 1 - Polyomino Tiling
 * This program's purpose is to solve polyomino tiling problems based on given input files.
 *
 * Note: the board is represented by an integer array, and empty spaces are represented by [0] cell
 * Placed polyomino are represent by their order in the input file - starting at 1
 *
 *
 * Testing has been completed with various input files
 * Overall the program has sufficent speed with moderately sized problems (around 15-30seconds)
 * Large problem are much slower but this is expected, e.g. the 3x20 file take many hours.
 *
 * Possible optimization ideas:
 * Multithreading
 * Checking for symmetry in polyomino and reduction # of rotations/flips
 */

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class tiling {
    private board polyboard;
    private polyominoList polyominoArrayList;

    public static void main(String[] args) throws IOException {
        new tiling().start();
    }

    private void start() throws IOException {
        loadData();

        System.out.println("Solving started.");
        long start = System.currentTimeMillis();                        //Log time for timer

        boolean success = solve();
        if (success) {
            System.out.println("Success! Solution found.");
            polyboard.print();
        } else {
            System.out.println("Failure! No solution has been found.");
        }

        long time = System.currentTimeMillis() - start;                 //Calculate time taken and print it out
        System.out.println("Time taken: " + time / 1000.0 + "s");
    }


    private void loadData () throws  IOException{
        Scanner scanner = new Scanner(System.in);

        int boardWidth = scanner.nextInt();
        int boardHeight = scanner.nextInt();
        polyboard = new board(boardWidth, boardHeight);
        System.out.println(boardWidth + " by " + boardHeight + " board loaded.");
        polyboard.print();

        int numberpoly = scanner.nextInt();
        polyominoArrayList = new polyominoList(numberpoly);             //Load each polyomino and add to a list
        for (int i = 1; i < (numberpoly + 1); i++) {                    //Also assign each a number starting at 1
            int blocknumber = scanner.nextInt();
            polyomino poly = new polyomino(i, blocknumber);
            for (int j = 0; j < blocknumber; j++) {
                int x = scanner.nextInt();
                int y = scanner.nextInt();
                poly.coordinates[j][0] = x;
                poly.coordinates[j][1] = y;
            }
            polyominoArrayList.add(poly);
        }
        System.out.println(polyominoArrayList.size() + " polyominos loaded.");
    }

    private boolean solve() {
        polyomino poly = polyominoArrayList.getNextAvailable();         //getNext unplaced polyomino
        if (poly == null) {                                             //no remaining polyomino left -> done
            return true;
        } else {
            for (int i = 0; i < polyboard.height; i++) {                //for each height
                for (int j = 0; j < polyboard.width; j++) {             //for each width
                    for (int k = 0; k < 8; k++) {                       //for each orientation
                        boolean placed = polyboard.place(poly, i, j);   //try to place
                        if (placed) {
                            boolean solved = solve();                   //if placed then continue to next piece
                            if (solved) {
                                return true;
                            } else {
                                polyboard.remove(poly);                 //if adding didn't solve then remove it
                            }
                        }
                        poly.rotate();                                  //rotate and flip for other 4 variations
                        if (k == 3) {
                            poly.flip();
                        }
                    }
                }
            }
        }
        return false;
    }


    class board {
        int width;
        int height;
        int[][] board;

        board(int width, int height) {
            this.width = width;
            this.height = height;
            this.board = new int[height][width];
        }

        void print() {
            for (int i = (height - 1); i >= 0; i--) {
                for (int j = 0; j < width; j++) {
                    System.out.print("[" + board[i][j] + "]");
                }
                System.out.println();
            }
            System.out.println();
        }

        boolean place(polyomino poly, int x, int y) {
            if (checkValid(poly, x, y)) {
                for (int i = 0; i < poly.numberCoordinate; i++) {
                    int xCoord = poly.coordinates[i][0] + x;
                    int yCoord = poly.coordinates[i][1] + y;
                    board[xCoord][yCoord] = poly.numberPoly;
                }
            } else {
                return false;
            }
            poly.isPlaced = true;
            return true;
        }

        private boolean checkValid(polyomino poly, int x, int y) {
            if (poly.isPlaced) {
                return false;
            }
            for (int i = 0; i < poly.numberCoordinate; i++) {
                int xCoord = poly.coordinates[i][0] + x;
                int yCoord = poly.coordinates[i][1] + y;
                if (xCoord < 0 || yCoord < 0) {
                    return false;
                } else if (xCoord >= this.height || yCoord >= this.width) {
                    return false;
                } else if (board[xCoord][yCoord] != 0) {
                    return false;
                }
            }
            return true;
        }

        void remove(polyomino poly) {
            poly.isPlaced = false;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (board[i][j] == poly.numberPoly) {
                        board[i][j] = 0;
                    }
                }
            }
        }
    }


    class polyomino {
        boolean isPlaced = false;
        int numberPoly;
        int numberCoordinate;
        int[][] coordinates;

        public polyomino(int num, int coords) {
            this.numberCoordinate = coords;
            this.numberPoly = num;
            this.coordinates = new int[coords][2];
        }

        void rotate() {                                                 //height becomes width
            for (int i = 0; i < numberCoordinate; i++) {                //width becomes -height
                int oldx = coordinates[i][0];
                int x = coordinates[i][1];
                x = -x;
                coordinates[i][0] = x;
                coordinates[i][1] = oldx;
            }
        }

        void flip() {                                                //height becomes -height
            for (int i = 0; i < numberCoordinate; i++) {
                int oldy = coordinates[i][1];
                int y = -oldy;
                coordinates[i][1] = y;
            }
        }
    }


    class polyominoList extends ArrayList<polyomino> {               //basically just an ArrayList but I wanted to add
        polyominoList(int initialCapacity) {                         //a function to it.
            super(initialCapacity);
        }

        polyomino getNextAvailable() {
            for (tiling.polyomino polyomino : this) {
                if (!polyomino.isPlaced) {
                    return polyomino;
                }
            }
            return null;
        }
    }

}