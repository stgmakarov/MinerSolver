public class Game {

    private static int[][] answerMap;
    private static int minesN;

    public static void newGame(String map) {
        //answerMap = new int[map.split("\n").length][];
        answerMap = MineSweeper.getMap(map);
        minesN = MineSweeper.initMap(map, answerMap);
    }

    public static void read(String map) {
        int[][] initMap = new int[map.split("\n").length][];
        MineSweeper.initMap(map, initMap);
    }

    public static int getMinesN() {
        return minesN;
    }

    public static int open(int x, int y) {
        if (answerMap[x][y] == MineSweeper.MINE) {
            System.err.println("BOOM!!!!" + x + ' ' + y);
        }
        return answerMap[x][y];
    }
}