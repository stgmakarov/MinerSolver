import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Integer.parseInt;

class MineSweeper {
    final static boolean PRINTDEBUG = false;
    public static final int MINE = -1;
    public static final int WHAT = -2;
    public static final int MAXATTEMOT = 5;
    int[][] myIntBoard;
    static int myRows;
    static int myCols;

    Board board;

    public MineSweeper(final String board, final int nMines) {
        // Your code here...

        myIntBoard = getMap(board);
        this.board = new Board(myRows,myCols,myIntBoard);
        this.board.printInputBoard(PRINTDEBUG);
        int i = 0;
        do{
            this.board.process(nMines, PRINTDEBUG);
            if(i++==MAXATTEMOT)break;
        }while (this.board.finalAttempt(nMines));
    }

    public static int [][] getMap(String map){
        String [] rows = map.split("\n");
        String [] colsTmp = rows[0].split(" ");
        myRows = rows.length;
        myCols = colsTmp.length;
        int [][] answerMap = new int[myRows][myCols];
        for(int i=0;i<myRows;i++){
            String [] cols = rows[i].split(" ");
            for(int j = 0;j< myCols;j++){
                answerMap[i][j] = switch (cols[j]){
                    case "x": yield MINE;
                    case "?": yield WHAT;
                    default: yield parseInt(cols[j]);
                };
            }
        }
        return answerMap;
    }

    public static int initMap(String map, int[][] answerMap) {
        answerMap = getMap(map);

        int rez = 0;
        for (int[] ints : answerMap) {
            for (int anInt : ints) {
                rez += (anInt == MINE) ? 1 : 0;
            }
        }
        return rez;
    }

    public String solve() {
        // Your code here...
        return board.result();
    }
}

class Cell{
    int id;
    int row;
    int col;
    Board board;
    boolean markedAsMine;
    boolean markedAsWhat;
    private int value;
    boolean markedAsFlag = false;
    float perc = 100;

    public void setValue(int value){
        this.value = value;
        switch (value) {
            case MineSweeper.MINE -> markedAsMine = true;
            case MineSweeper.WHAT -> markedAsWhat = true;
            default -> {
                markedAsMine = false;
                markedAsWhat = false;
            }
        }
    }

    public boolean equals(Object o){
        return this.id == ((Cell)(o)).id;
    }

    public int getValue(){
        return value;
    }

    boolean checkCell(){
        AtomicBoolean res = new AtomicBoolean(true);
        getneighbors(false).forEach(cell1 -> {
            if(cell1.getValue()>=0){
                if(cell1.getValue() != cell1.getKnownMineNearCount()){
                    res.set(false);
                }
            }
        });
        return res.get();
    }

    public Cell(int row, int col, Board board, int value){
        this.row = row;
        this.col = col;
        this.board = board;
        this.id = this.row * this.board.cols + this.col;
        setValue(value);
    }

    private int mined(){
        return markedAsMine?1:0;
    }

    public boolean open(){
        if(markedAsWhat){
            setValue(Game.open(row,col));
            return true;
        }else return false;
    }

    ArrayList<Cell> getneighbors(boolean onlyWhats){
        ArrayList<Cell> result = new ArrayList<>();
        for(int i=-1;i<=1;i++){
            for(int j=-1;j<=1;j++){
                if(i==0&j==0)continue;
                int curRow = row + i;
                int curCol = col + j;
                if(curRow>=0&curRow<board.rows&curCol>=0&curCol<board.cols){
                    if(!onlyWhats|(board.getCellByPos(curRow,curCol).markedAsWhat)){
                        result.add(board.getCellByPos(curRow,curCol));
                    }
                }
            }
        }
        return result;
    }

    public int getKnownMineNearCount(){
        AtomicReference<Integer> result = new AtomicReference<>(0);
        getneighbors(false).forEach(cell -> {
            result.updateAndGet(v -> v + cell.mined());
        });
        return result.get();
    }

    public void setAsMine() {
        this.markedAsWhat = false;
        this.markedAsMine = true;
        this.value = MineSweeper.MINE;
    }

    public void setAsWhat() {
        this.markedAsWhat = true;
        this.markedAsMine = false;
        this.value = MineSweeper.WHAT;
    }
}

class Board{
    final int rows;
    final int cols;

    ArrayList<Board> possSolves = new ArrayList<>();

    ArrayList<Cell> cells = new ArrayList<>();
    ArrayList<Group> groups = new ArrayList<>();

    public Board(int rows, int cols, int[][] myIntBoard){
        this.rows = rows;
        this.cols = cols;
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                Cell newCell = new Cell(i,j,this, myIntBoard[i][j]);
                cells.add(newCell);
            }
        }
    }

    public Board(int rows, int cols, ArrayList<Cell> cells){
        this.rows = rows;
        this.cols = cols;
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                Cell oldCell = cells.get(getIndByPos(i,j));
                Cell newCell = new Cell(i,j,this, oldCell.getValue());
                newCell.markedAsFlag = oldCell.markedAsFlag;
                this.cells.add(newCell);
            }
        }
    }

    public boolean haveWhat(){
        AtomicBoolean res = new AtomicBoolean(false);
        cells.forEach(cell -> {
            if(cell.markedAsWhat)res.set(true);
        });
        return res.get();
    }

    public String result(){
        if(haveWhat())return "?";else return this.toString();
    }

    public String toString(){
/*        return  "1 x 1 1 x 1\n" +
                  "2 2 2 1 2 2\n" +
                  "2 x 2 0 1 x\n" +
                  "2 x 2 1 2 2\n" +
                  "1 1 1 1 x 1\n" +
                  "0 0 0 1 1 1";*/
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                Cell cell = cells.get(getIndByPos(i,j));
                if(cell.markedAsMine) {
                    sb.append('x');
                } else if (cell.markedAsWhat){
                    sb.append('?');
                } else sb.append(cells.get(getIndByPos(i,j)).getValue());
                if(j<cols-1)sb.append(" ");
            }
            if(i<rows-1)sb.append("\n");
        }

        return sb.toString();
    }

    public void process(int nMines, boolean print) {
        boolean res;


        do{
            res = openZeros();
        }while (res);

        do {
            int whatCount = getWhatCount();
            getGroups();
            do {
                res = processGroups();
            } while (res);
            markMines();
            groups.forEach(Group::open);

            if(print){
                System.out.println(this);
                System.out.println("=============================");
                System.out.println("Need to open"+(nMines-getMinesCount()));
            }
            if(whatCount == getWhatCount()|getWhatCount()==0)break;
        }while (true);
        if(!haveWhat()){
            if(print){
                System.out.println("Solved");
            }
        }
    }

    public void printInputBoard(boolean print) {
        if(print) {
            System.out.println("Input:");
            System.out.println(this);
            System.out.println("=============================");
        }
    }

    public void markMines(){
        groups.forEach(group -> {
            if(group.needToMark()){
                group.cells.forEach(Cell::setAsMine);
            }
        });
    }

    public boolean isAllMines(Group group){
        if(group.needToMark()){
            group.cells.forEach(Cell::setAsMine);
            groups.remove(group);
            return true;
        }else return false;
    }

    public int getWhatCount(){
        AtomicInteger res = new AtomicInteger();
        cells.forEach(cell -> {
            if(cell.markedAsWhat) res.addAndGet(1);
        });
        return res.get();
    }

    public int getMinesCount(){
        AtomicInteger res = new AtomicInteger();
        cells.forEach(cell -> {
            if(cell.markedAsMine) res.addAndGet(1);
        });
        return res.get();
    }

    public boolean processGroups(){
        if(groups.size()<=1)return false;
        boolean res = false;
        int i = 0;
        while (i<groups.size()-1){
            if(isAllMines(groups.get(i)))continue;
            int j = i+1;
            while (j<groups.size()){
                if(isAllMines(groups.get(j)))continue;
                int compareResult = groups.get(i).compareTo(groups.get(j));
                switch (compareResult) {
                    case Group.EQ -> {
                        groups.remove(j);
                        res = true;
                        continue;
                    }
                    case Group.THISCONSOTH -> {
                        groups.get(i).substractGroup(groups.get(j));
                        res = true;
                    }
                    case Group.OTHCONSTHIS -> {
                        groups.get(j).substractGroup(groups.get(i));
                        res = true;
                    }
                    case Group.MIXED -> {
                    }
                }
                j++;
            }
            i++;
        }
        return res;
    }

    public Cell getCellByPos(int row, int col){
        return cells.get(getIndByPos(row,col));
    }

    public int getIndByPos(int row, int col){
        return row*cols+col;
    }

    public boolean openZeros(){
        AtomicReference<Boolean> result = new AtomicReference<>(false);
        cells.forEach(cell -> {
            if(cell.getValue()==0){
                cell.getneighbors(true).forEach(neibCell ->{
                    if(neibCell.open()){
                        result.set(true);
                    }
                });

            }
        });

        return result.get();
    }

    public void getGroups(){
        groups.clear();
        cells.forEach(cell -> {
            if(!cell.markedAsWhat & !cell.markedAsMine & cell.getValue()>0 & cell.getneighbors(true).size()>0){
                groups.add(new Group(cell));
            }
        });
    }

    public boolean finalAttempt(final int nMines) {
        if(!haveWhat()|getWhatCells().size()<(nMines-getMinesCount()))return false;
        if(getMinesCount()==nMines){
            cells.forEach(cell -> {
                if(cell.markedAsWhat)cell.setValue(cell.getKnownMineNearCount());
            });
            return false;
        }else
        if(getMinesCount()<nMines){
            //try to find
            int deltaMines = nMines-getMinesCount();
            if(deltaMines>20)return false;
            tryToFill(deltaMines, this);
            switch (possSolves.size()){
                case 0:{
                    //no solve
                    return false;
                }
                case 1: {
                    //one solve
                    cells = possSolves.get(0).cells;
                    return false;
                }
                default:{
                    //some solve. Need to find always mined cells
                    AtomicBoolean res = new AtomicBoolean(false);
                    ArrayList<Cell> minedCells;
                    minedCells = getWhatCells(); //what cells in main board
                    minedCells.forEach(cell -> {
                        cell.markedAsFlag = true;
                        cell.perc = 100;
                    });
                    possSolves.forEach(board -> {
                        minedCells.forEach(cell -> {
                            if(!board.cells.get(cell.id).markedAsMine){
                                cell.markedAsFlag = false;
                                cell.perc -= (100/possSolves.size());
                            }
                        });
                    });
                    minedCells.forEach(cell -> {
                        if(cell.markedAsFlag)
                        {
                            cell.setAsMine();
                            res.set(true);
                        }
                    });
                    if(!res.get()){
                        int maxPercId = -1;
                        float maxPerc = 0;
                        for(int i =0;i<minedCells.size();i++){
                            if(minedCells.get(i).perc > maxPerc){
                                maxPerc = minedCells.get(i).perc;
                                maxPercId = minedCells.get(i).id;
                            }
                        }
                        if(maxPerc>50&maxPercId>=0){
                            cells.get(maxPercId).setAsMine();
                            possSolves.clear();
                            return true;
                        }else return false;
                    }else{
                        possSolves.clear();
                        return res.get();
                    }
                }
            }
        }
        return false;
    }

    private void tryToFill(int deltaMines, Board realBoard){
        AtomicBoolean res = new AtomicBoolean(true);
        if(deltaMines==0){
            //check
            getWhatCells().forEach(cell -> {
                cell.setValue(cell.getKnownMineNearCount());
            });
            cells.forEach(cell -> {
                if(!cell.checkCell()){
                    res.set(false);
                }
            });
            if(res.get()){
                realBoard.possSolves.add(this);
            }
        }else{
            //fill
            ArrayList<Cell> whatCells = getWhatCells();
            int whatCnt = getWhatCount();
            for(int i=0;i<whatCnt;i++){
                if(whatCells.get(i).markedAsFlag)continue;
                whatCells.get(i).markedAsFlag = true;
                whatCells.get(i).setAsMine();
                Board tmpBoard = new Board(rows, cols, cells);
                tmpBoard.tryToFill(deltaMines - 1, realBoard);
                whatCells.get(i).setAsWhat();
            }
        }
    }

    private ArrayList<Cell> getWhatCells() {
        ArrayList<Cell> res = new ArrayList<>();
        cells.forEach(cell -> {
            if(cell.markedAsWhat)res.add(cell);
        });
        return res;
    }
}

class Group{
    /*0 - different
  1 - this consist other
  2 - other consist this
  3 - mixed*/
    final static int DEFFERENT = 0;
    final static int THISCONSOTH = 1;
    final static int OTHCONSTHIS = 2;
    final static int EQ = 3;
    final static int MIXED = 4;

    public ArrayList<Cell> cells = new ArrayList<>();
    int mines = 0;

    public Group(Cell cell){
        int nearMines = cell.getKnownMineNearCount();
        cells.addAll(cell.getneighbors(true));
        mines = cell.getValue() - nearMines;
        if(cells.size()==0){
            System.err.println("!!!Empty group!!!");
        }
    }

    public Group(ArrayList<Cell> cellsArr){
        this.cells.addAll(cellsArr);
        mines = 0;
    }

    public boolean open(){
        if(mines==0){
            cells.forEach(Cell::open);
            return true;
        }
        return false;
    }

    public ArrayList<Cell> getMixedCells(Group otherGroup){
        ArrayList<Cell> result = new ArrayList<>();
        cells.forEach(cell -> {
            if(otherGroup.isCellInGroup(cell))result.add(cell);
        });
        return result;
    }

    boolean isCellInGroup(Cell cell){
        AtomicBoolean res = new AtomicBoolean(false);
        cells.forEach(cell1 -> {
            if(cell1.equals(cell)) res.set(true);
        });
        return res.get();
    }

    public void substractGroup(Group otherGroup){
        //this minus other
        cells.removeAll(otherGroup.cells);
        this.mines -= otherGroup.mines;
    }

    public boolean needToMark(){
        return mines == cells.size();
    }

    public boolean consistsCell(Cell cell){
        AtomicBoolean res = new AtomicBoolean(false);
        cells.forEach(cell1 -> {
            if(cell.equals(cell1)) res.set(true);
        });
        return res.get();
    }

    public boolean consistsAllCells(Group otherGroup){
        AtomicBoolean result = new AtomicBoolean(true);
        otherGroup.cells.forEach(otherCell -> {
            result.set(result.get() & consistsCell(otherCell));
        });
        return result.get();
    }

    public boolean consistsAnyCells(Group otherGroup){
        AtomicBoolean result = new AtomicBoolean(false);
        otherGroup.cells.forEach(otherCell -> {
            result.set(result.get() | consistsCell(otherCell));
        });
        return result.get();
    }

    public int compareTo(Group otherGroup){
        int res = DEFFERENT;
        if(consistsAllCells(otherGroup))res = THISCONSOTH;
        if(otherGroup.consistsAllCells(this)){
            if(res == DEFFERENT) {
                res = OTHCONSTHIS;
            }else if(res == THISCONSOTH){
                res = EQ;
            }
        }
        if(res == DEFFERENT & consistsAnyCells(otherGroup))res=MIXED;
        return res;
    }
}